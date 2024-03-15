/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.fuelgauge;

import android.content.Context;

import com.android.settings.fuelgauge.BatterySettingsFeatureProviderImpl;
import com.android.settings.R;

/** Feature provider implementation for battery settings usage. */
public class ParanoidBatterySettingsFeatureProviderImpl extends BatterySettingsFeatureProviderImpl {

    @Override
    public boolean isBatteryInfoEnabled(Context context) {
        return context.getResources().getBoolean(R.bool.config_show_battery_info);
    }

}
