/*
 * SPDX-FileCopyrightText: 2023 Yet Another AOSP Project
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.gestures

import android.content.Context
import android.provider.Settings
import android.widget.CompoundButton
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import co.aospa.framework.preference.SecureSettingSwitchPreference
import com.android.settings.R
import com.android.settingslib.core.AbstractPreferenceController
import com.android.settingslib.widget.MainSwitchPreference

class PickupGestureInsidePreferenceController(
    context: Context
) : AbstractPreferenceController(context), CompoundButton.OnCheckedChangeListener {

    private val default: Boolean = mContext.resources.getBoolean(com.android.internal.R.bool.config_dozePickupGestureEnabled)
    private var mainPreference: MainSwitchPreference? = null
    private var ambientPreference: SecureSettingSwitchPreference? = null

    override fun getPreferenceKey(): String = KEY

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        mainPreference = screen.findPreference(getPreferenceKey())
        mainPreference?.addOnSwitchChangeListener(this)
        mainPreference?.let{updateState(it as Preference)}
        ambientPreference = screen.findPreference(AMBIENT_KEY)
    }

    fun setChecked(isChecked: Boolean) {
        mainPreference?.updateStatus(isChecked)
        updateAmbientEnablement(isChecked)
    }

    override fun updateState(preference: Preference) {
        val enabled = Settings.Secure.getInt(mContext.contentResolver, Settings.Secure.DOZE_PICK_UP_GESTURE, if (default) 1 else 0) == 1
        setChecked(enabled)
    }

    override fun isAvailable(): Boolean = true

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        Settings.Secure.putInt(mContext.contentResolver, Settings.Secure.DOZE_PICK_UP_GESTURE, if (isChecked) 1 else 0)
        updateAmbientEnablement(isChecked)
    }

    private fun updateAmbientEnablement(enabled: Boolean) {
        ambientPreference?.isEnabled = enabled
    }

    companion object {
        private const val KEY = "gesture_pick_up"
        private const val AMBIENT_KEY = "doze_pick_up_gesture_ambient"
    }
}
