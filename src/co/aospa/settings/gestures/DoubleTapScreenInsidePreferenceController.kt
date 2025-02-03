/*
 * SPDX-FileCopyrightText: 2023 Yet Another AOSP Project
 * SPDX-FileCopyrightText: 2024-2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.gestures

import android.content.Context
import android.provider.Settings
import android.provider.Settings.Secure.DOZE_DOUBLE_TAP_GESTURE
import android.widget.CompoundButton
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import com.android.settingslib.core.AbstractPreferenceController
import com.android.settingslib.widget.MainSwitchPreference

class DoubleTapScreenInsidePreferenceController(
    context: Context
) : AbstractPreferenceController(context), CompoundButton.OnCheckedChangeListener {

    private var preference: MainSwitchPreference? = null

    override fun getPreferenceKey(): String = "gesture_double_tap_screen"

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        preference = screen.findPreference(getPreferenceKey())
        preference?.addOnSwitchChangeListener(this)
        preference?.let { updateState(it) }
    }

    override fun updateState(pref: Preference) {
        preference?.updateStatus(Settings.Secure.getInt(mContext.contentResolver,
                DOZE_DOUBLE_TAP_GESTURE, 0) == 1)
    }

    override fun isAvailable(): Boolean = true

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        Settings.Secure.putInt(mContext.contentResolver, DOZE_DOUBLE_TAP_GESTURE,
                if (isChecked) 1 else 0)
    }
}
