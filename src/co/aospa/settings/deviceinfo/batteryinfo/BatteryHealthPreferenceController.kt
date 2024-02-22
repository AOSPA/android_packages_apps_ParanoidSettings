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
 * A controller that manages the information about battery health.
 */
class BatteryHealthPreferenceController(
    context: Context,
    preferenceKey: String
) : BasePreferenceController(context, preferenceKey) {

    override fun getAvailabilityStatus(): Int {
        return AVAILABLE
    }

    override fun getSummary(): CharSequence {
        val batteryIntent: Intent = BatteryUtils.getBatteryIntent(mContext)
        val health: Int = batteryIntent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)

        return when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> mContext.getString(R.string.battery_health_good)
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> mContext.getString(R.string.battery_health_overheat)
            BatteryManager.BATTERY_HEALTH_DEAD -> mContext.getString(R.string.battery_health_dead)
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> mContext.getString(R.string.battery_health_over_voltage)
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> mContext.getString(R.string.battery_health_unspecified_failure)
            BatteryManager.BATTERY_HEALTH_COLD -> mContext.getString(R.string.battery_health_cold)
            else -> mContext.getString(R.string.battery_health_unknown)
        }
    }
}
