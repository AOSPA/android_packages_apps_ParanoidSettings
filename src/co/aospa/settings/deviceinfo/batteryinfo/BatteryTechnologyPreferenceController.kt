/*
 * SPDX-FileCopyrightText: 2024 The Android Open Source Project
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.deviceinfo.batteryinfo

import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import com.android.settings.R
import com.android.settings.core.BasePreferenceController
import com.android.settingslib.fuelgauge.BatteryUtils

/**
 * A controller that manages the information about battery technology.
 */
class BatteryTechnologyPreferenceController(
    context: Context,
    preferenceKey: String
) : BasePreferenceController(context, preferenceKey) {

    override fun getAvailabilityStatus(): Int {
        return AVAILABLE
    }

    override fun getSummary(): CharSequence {
        val batteryIntent: Intent = BatteryUtils.getBatteryIntent(mContext)
        val technology = batteryIntent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
        return technology ?: mContext.getString(R.string.battery_technology_not_available)
    }
}
