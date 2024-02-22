/*
 * SPDX-FileCopyrightText: 2024 The Android Open Source Project
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.deviceinfo.batteryinfo

import android.content.Context
import android.os.BatteryManager
import com.android.settings.R
import com.android.settings.core.BasePreferenceController
import com.android.settingslib.fuelgauge.BatteryUtils

/**
 * A controller that manages the information about battery maximum capacity.
 */
class BatteryMaximumCapacityPreferenceController(
    context: Context,
    preferenceKey: String
) : BasePreferenceController(context, preferenceKey) {

    override fun getAvailabilityStatus(): Int = AVAILABLE

    override fun getSummary(): CharSequence {
        val batteryIntent = BatteryUtils.getBatteryIntent(mContext)
        val maxCapacityUah = batteryIntent.getIntExtra(BatteryManager.EXTRA_MAXIMUM_CAPACITY, -1)
        val designCapacityUah = batteryIntent.getIntExtra(BatteryManager.EXTRA_DESIGN_CAPACITY, -1)

        return if (maxCapacityUah > 0 && designCapacityUah > 0) {
            val maxCapacity = maxCapacityUah / 1_000
            val designCapacity = designCapacityUah / 1_000
            val percentage = (maxCapacity * 100) / designCapacity
            mContext.getString(R.string.battery_maximum_capacity_summary, maxCapacity, percentage)
        } else {
            mContext.getString(R.string.battery_maximum_capacity_not_available)
        }
    }
}
