/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.fuelgauge

import android.content.Context
import com.android.settings.fuelgauge.BatterySettingsFeatureProviderImpl
import com.android.settings.R

/** Feature provider implementation for battery settings usage. */
class ParanoidBatterySettingsFeatureProviderImpl : BatterySettingsFeatureProviderImpl() {

    override fun isBatteryInfoEnabled(context: Context): Boolean {
        return context.resources.getBoolean(R.bool.config_show_battery_info)
    }
}
