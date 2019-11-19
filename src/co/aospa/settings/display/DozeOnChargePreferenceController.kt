 /*
 * SPDX-FileCopyrightText: 2018 The Android Open Source Project
 * SPDX-FileCopyrightText: 2022 FlamingoOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.display

import android.content.Context
import android.database.ContentObserver
import android.hardware.display.AmbientDisplayConfiguration
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import android.provider.Settings

import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreference

import com.android.settings.R
import com.android.settings.core.TogglePreferenceController
import com.android.settingslib.core.lifecycle.Lifecycle

class DozeOnChargePreferenceController(
    context: Context,
    lifecycle: Lifecycle?,
) : TogglePreferenceController(context, KEY),
    LifecycleEventObserver {

    private var config: AmbientDisplayConfiguration = AmbientDisplayConfiguration(context)
    private var preference: Preference? = null

    private val settingsObserver = object : ContentObserver(
        Handler(Looper.getMainLooper())
    ) {
        override fun onChange(selfChange: Boolean) {
            preference?.let{ updateState(it) }
        }
    }

    init {
        lifecycle?.addObserver(this)
    }

    override fun getAvailabilityStatus(): Int {
        return if (config.alwaysOnAvailableForUser(UserHandle.USER_CURRENT)) {
            if (config.alwaysOnEnabledSetting(UserHandle.USER_CURRENT))
                DISABLED_DEPENDENT_SETTING
            else
                AVAILABLE
        } else {
            UNSUPPORTED_ON_DEVICE
        }
    }

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        preference = screen.findPreference(preferenceKey)
    }

    override fun isChecked(): Boolean =
        Settings.Secure.getIntForUser(mContext.contentResolver,
            Settings.Secure.DOZE_ON_CHARGE, 0, UserHandle.USER_CURRENT) == 1

    override fun setChecked(isChecked: Boolean): Boolean =
        Settings.Secure.putIntForUser(
            mContext.contentResolver,
            Settings.Secure.DOZE_ON_CHARGE,
            if (isChecked) 1 else 0,
            UserHandle.USER_CURRENT
        )

    override fun getSliceHighlightMenuRes() = R.string.menu_key_display

    override fun updateState(preference: Preference) {
        preference.setEnabled(getAvailabilityStatus() == AVAILABLE)
        super.updateState(preference)
    }

    override fun onStateChanged(owner: LifecycleOwner, event: Event) {
        if (event == Event.ON_START) {
            mContext.contentResolver.registerContentObserver(
                Settings.Secure.getUriFor(Settings.Secure.DOZE_ALWAYS_ON),
                false,
                settingsObserver
            )
        } else if (event == Event.ON_STOP) {
            mContext.contentResolver.unregisterContentObserver(settingsObserver)
        }
    }

    fun setConfig(
        config: AmbientDisplayConfiguration
    ): DozeOnChargePreferenceController {
        this.config = config
        return this
    }

    companion object {
        private const val KEY = "doze_on_charge"
    }
}
