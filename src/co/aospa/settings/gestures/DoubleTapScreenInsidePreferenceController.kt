/*
 * SPDX-FileCopyrightText: 2023 Yet Another AOSP Project
 * SPDX-FileCopyrightText: 2024-2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.gestures

import android.content.Context
import android.provider.Settings
import android.widget.CompoundButton
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import com.android.settingslib.core.AbstractPreferenceController
import com.android.settingslib.widget.MainSwitchPreference

class DoubleTapScreenInsidePreferenceController(
    context: Context
) : AbstractPreferenceController(context), CompoundButton.OnCheckedChangeListener {

    private var mainPreference: MainSwitchPreference? = null

    override fun getPreferenceKey(): String = KEY

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        mainPreference = screen.findPreference(getPreferenceKey())
        mainPreference?.addOnSwitchChangeListener(this)
        mainPreference?.let{updateState(it as Preference)}
    }

    fun setChecked(isChecked: Boolean) {
        mainPreference?.updateStatus(isChecked)
    }

    override fun updateState(preference: Preference) {
        val enabled = Settings.Secure.getInt(mContext.contentResolver, SECURE_KEY, 0) == 1
        setChecked(enabled)
    }

    override fun isAvailable(): Boolean = true

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        Settings.Secure.putInt(mContext.contentResolver, SECURE_KEY, if (isChecked) 1 else 0)
    }

    companion object {
        private const val KEY = "gesture_double_tap_screen"
        private const val SECURE_KEY = Settings.Secure.DOZE_DOUBLE_TAP_GESTURE
    }
}
