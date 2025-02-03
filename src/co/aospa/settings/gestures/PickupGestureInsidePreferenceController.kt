/*
 * SPDX-FileCopyrightText: 2023 Yet Another AOSP Project
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.gestures

import android.content.Context
import android.provider.Settings
import android.provider.Settings.Secure.DOZE_PICK_UP_GESTURE
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
    private var preference: MainSwitchPreference? = null
    private var ambientPreference: SecureSettingSwitchPreference? = null

    override fun getPreferenceKey(): String = "gesture_pick_up"

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        preference = screen.findPreference(getPreferenceKey())
        preference?.addOnSwitchChangeListener(this)
        preference?.let { updateState(it) }
        ambientPreference = screen.findPreference("doze_pick_up_gesture_ambient")
    }

    override fun updateState(pref: Preference) {
        val enabled = Settings.Secure.getInt(mContext.contentResolver,
                DOZE_PICK_UP_GESTURE, if (default) 1 else 0) == 1
        preference?.updateStatus(enabled)
        updateAmbientEnablement(enabled)
    }

    override fun isAvailable(): Boolean = true

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        Settings.Secure.putInt(mContext.contentResolver,
                DOZE_PICK_UP_GESTURE, if (isChecked) 1 else 0)
        updateAmbientEnablement(isChecked)
    }

    private fun updateAmbientEnablement(enabled: Boolean) {
        ambientPreference?.isEnabled = enabled
    }
}
