package com.joshuacerdenia.android.nicefeed.ui.fragment

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ScrollView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import com.joshuacerdenia.android.nicefeed.R
import com.joshuacerdenia.android.nicefeed.data.local.NiceFeedPreferences
import com.joshuacerdenia.android.nicefeed.ui.OnToolbarInflated
import com.joshuacerdenia.android.nicefeed.ui.dialog.AboutFragment
import com.joshuacerdenia.android.nicefeed.util.Utils
import com.joshuacerdenia.android.nicefeed.util.work.BackgroundSyncWorker
import com.joshuacerdenia.android.nicefeed.util.work.NewEntriesWorker

class SettingsFragment: VisibleFragment(), AboutFragment.Callback {

    interface Callbacks: OnToolbarInflated

    private lateinit var toolbar: Toolbar
    private lateinit var scrollView: ScrollView
    private lateinit var autoUpdateSwitch: SwitchCompat
    private lateinit var browserSwitch: SwitchCompat
    private lateinit var notificationSwitch: SwitchCompat
    private lateinit var bannerSwitch: SwitchCompat
    private lateinit var syncSwitch: SwitchCompat
    private lateinit var keepEntriesSwitch: SwitchCompat
    private lateinit var themeSpinner: Spinner
    private lateinit var sortFeedsSpinner: Spinner
    private lateinit var sortEntriesSpinner: Spinner
    private lateinit var fontSpinner: Spinner

    private val fragment = this@SettingsFragment
    private var callbacks: Callbacks? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        toolbar = view.findViewById(R.id.toolbar)
        scrollView = view.findViewById(R.id.scroll_view)
        autoUpdateSwitch = view.findViewById(R.id.auto_update_switch)
        browserSwitch = view.findViewById(R.id.browser_switch)
        notificationSwitch = view.findViewById(R.id.notification_switch)
        bannerSwitch = view.findViewById(R.id.banner_switch)
        syncSwitch = view.findViewById(R.id.sync_switch)
        keepEntriesSwitch = view.findViewById(R.id.keep_entries_switch)
        themeSpinner = view.findViewById(R.id.theme_spinner)
        sortFeedsSpinner = view.findViewById(R.id.sort_feeds_spinner)
        sortEntriesSpinner = view.findViewById(R.id.sort_entries_spinner)
        fontSpinner = view.findViewById(R.id.font_spinner)
        setupToolbar()
        setHasOptionsMenu(true)
        return view
    }

    private fun setupToolbar() {
        toolbar.title = getString(R.string.settings)
        callbacks?.onToolbarInflated(toolbar)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        themeSpinner.apply {
            adapter = arrayOf(
                getString(R.string.system_default),
                getString(R.string.light),
                getString(R.string.dark)
            ).run { getDefaultAdapter(context, this)}
            setSelection(NiceFeedPreferences.getTheme(context))
            onItemSelectedListener = getSpinnerListener(context, ACTION_SAVE_THEME)
        }

        sortFeedsSpinner.apply {
            adapter = arrayOf(
                getString(R.string.title),
                getString(R.string.unread_items)
            ).run { getDefaultAdapter(context, this) }
            setSelection(NiceFeedPreferences.getFeedsOrder(context))
            onItemSelectedListener = getSpinnerListener(context, ACTION_SAVE_FEEDS_ORDER)
        }

        sortEntriesSpinner.apply {
            adapter = arrayOf(
                getString(R.string.date_published),
                getString(R.string.unread_on_top)
            ).run { getDefaultAdapter(context, this)}
            setSelection(NiceFeedPreferences.getEntriesOrder(context))
            onItemSelectedListener = getSpinnerListener(context, ACTION_SAVE_ENTRIES_ORDER)
        }

        fontSpinner.apply {
            adapter = arrayOf(
                getString(R.string.sans_serif),
                getString(R.string.serif)
            ).run { getDefaultAdapter(context, this)}
            setSelection(NiceFeedPreferences.getFont(context))
            onItemSelectedListener = getSpinnerListener(context, ACTION_SAVE_FONT)
        }

        autoUpdateSwitch.apply {
            isChecked = NiceFeedPreferences.getAutoUpdateSetting(context)
            setOnCheckedChangeListener { _, isOn ->
                NiceFeedPreferences.saveAutoUpdateSetting(context, isOn)
            }
        }

        keepEntriesSwitch.apply {
            isChecked = NiceFeedPreferences.keepOldUnreadEntries(context)
            setOnCheckedChangeListener { _, isOn ->
                NiceFeedPreferences.setKeepOldUnreadEntries(context, isOn)
            }
        }

        syncSwitch.apply {
            isChecked = NiceFeedPreferences.syncInBackground(context)
            setOnCheckedChangeListener { _, isOn ->
                NiceFeedPreferences.setSyncInBackground(context, isOn)
                if (isOn) BackgroundSyncWorker.start(context) else BackgroundSyncWorker.cancel(context)
            }
        }

        bannerSwitch.apply {
            isChecked = NiceFeedPreferences.bannerIsEnabled(context)
            setOnCheckedChangeListener { _, isOn ->
                NiceFeedPreferences.setBannerIsEnabled(context, isOn)
            }
        }

        browserSwitch.apply {
            // Values are reversed on purpose
            isChecked = !NiceFeedPreferences.getBrowserSetting(context)
            setOnCheckedChangeListener { _, isOn ->
                NiceFeedPreferences.setBrowserSetting(context, !isOn)
            }
        }

        notificationSwitch.apply {
            isChecked = NiceFeedPreferences.getPollingSetting(context)
            setOnCheckedChangeListener { _, isOn ->
                NiceFeedPreferences.savePollingSetting(context, isOn)
                if (isOn) NewEntriesWorker.start(context) else NewEntriesWorker.cancel(context)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_settings, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.about_menu_item) {
            AboutFragment.newInstance().apply {
                setTargetFragment(fragment, 0)
                show(fragment.parentFragmentManager, "about")
            }
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun getDefaultAdapter(context: Context, items: Array<String>): ArrayAdapter<String> {
        return ArrayAdapter(context, android.R.layout.simple_spinner_item, items).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun getSpinnerListener(context: Context, action: Int): AdapterView.OnItemSelectedListener {
        return object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (action) {
                    ACTION_SAVE_THEME -> {
                        if(Build.VERSION.SDK_INT<29){
                            if(position == 0) {
                                NiceFeedPreferences.saveTheme(context, position)
                                setDarkMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                            }
                            if (position == 2) {
                                NiceFeedPreferences.saveTheme(context, position)
                                setDarkMode(AppCompatDelegate.MODE_NIGHT_YES)
                            }
                            else{
                                NiceFeedPreferences.saveTheme(context, position)
                                Utils.setTheme(position)
                            }
                        }
                        else {
                            NiceFeedPreferences.saveTheme(context, position)
                            Utils.setTheme(position)
                        }
                    }
                    ACTION_SAVE_FEEDS_ORDER -> NiceFeedPreferences.saveFeedsOrder(context, position)
                    ACTION_SAVE_ENTRIES_ORDER -> NiceFeedPreferences.saveEntriesOrder(context, position)
                    ACTION_SAVE_FONT -> NiceFeedPreferences.saveFont(context, position)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { } // Do nothing
        }
    }

    override fun onGoToRepoClicked() {
        Utils.openLink(requireActivity(), scrollView, Uri.parse(GITHUB_REPO))
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    companion object {

        private const val GITHUB_REPO = "https://www.github.com/joshuacerdenia/nicefeed"
        private const val ACTION_SAVE_THEME = 0
        private const val ACTION_SAVE_FEEDS_ORDER = 1
        private const val ACTION_SAVE_ENTRIES_ORDER = 2
        private const val ACTION_SAVE_FONT = 3

        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }

    private fun setDarkMode(@AppCompatDelegate.NightMode darkMode: Int) {
        AppCompatDelegate.setDefaultNightMode(darkMode)

    }
}