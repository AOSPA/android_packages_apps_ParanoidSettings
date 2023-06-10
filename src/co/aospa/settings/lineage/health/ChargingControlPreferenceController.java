/*
 * SPDX-FileCopyrightText: 2022 The PixelExperience Project
 * SPDX-FileCopyrightText: 2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.lineage.health;

import android.content.Context;
import android.os.IBinder;
import android.os.ServiceManager;

import com.android.settings.core.BasePreferenceController;
import com.android.settings.R;

public class ChargingControlPreferenceController extends BasePreferenceController {

    public static final String KEY = "charging_control";

    private Context mContext;

    public ChargingControlPreferenceController(Context context, String key) {
        super(context, key);

        mContext = context;
    }

    public ChargingControlPreferenceController(Context context) {
        this(context, KEY);

        mContext = context;
    }

    private boolean isNegated(String key) {
        return key != null && key.startsWith("!");
    }

    @Override
    public int getAvailabilityStatus() {
        String rService =  "lineagehealth";
        boolean negated = isNegated(rService);
        if (negated) {
           rService = rService.substring(1);
        }
        IBinder value = ServiceManager.getService(rService);
        boolean available = value != null;
        if (available == negated) {
            return UNSUPPORTED_ON_DEVICE;
        }
        return AVAILABLE;
    }

}
