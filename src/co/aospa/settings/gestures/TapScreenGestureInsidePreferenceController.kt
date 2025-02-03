/*
 * SPDX-FileCopyrightText: 2023 Yet Another AOSP Project
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.gestures

import android.content.Context
import android.os.SystemProperties
import android.provider.Settings
import android.provider.Settings.Secure.DOZE_TAP_SCREEN_GESTURE
import android.widget.CompoundButton
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import co.aospa.framework.preference.SecureSettingSwitchPreference
import com.android.settings.R
import com.android.settingslib.core.AbstractPreferenceController
import com.android.settingslib.widget.MainSwitchPreference

class TapScreenGestureInsidePreferenceController(
    context: Context
) : AbstractPreferenceController(context), CompoundButton.OnCheckedChangeListener {

    private var preference: MainSwitchPreference? = null
    private var ambientPreference: SecureSettingSwitchPreference? = null

    override fun getPreferenceKey(): String = "gesture_tap"

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        preference = screen.findPreference(getPreferenceKey())
        preference?.addOnSwitchChangeListener(this)
        preference?.let { updateState(it) }
        ambientPreference = screen.findPreference("doze_tap_gesture_ambient")
    }

    override fun updateState(pref: Preference) {
        val enabled = Settings.Secure.getInt(mContext.contentResolver,
                DOZE_TAP_SCREEN_GESTURE, 1) == 1
        preference?.updateStatus(enabled)
        updateAmbientEnablement(enabled)
    }

    override fun isAvailable(): Boolean = true

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        Settings.Secure.putInt(mContext.contentResolver,
                DOZE_TAP_SCREEN_GESTURE, if (isChecked) 1 else 0)
        SystemProperties.set("persist.sys.tap_gesture", if (isChecked) "1" else "0")
        updateAmbientEnablement(isChecked)
    }

    private fun updateAmbientEnablement(enabled: Boolean) {
        ambientPreference?.isEnabled = enabled
    }
}
