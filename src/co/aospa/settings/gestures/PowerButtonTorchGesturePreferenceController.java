/*
 * SPDX-FileCopyrightText: 2018 The Android Open Source Project
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.gestures;

import static android.provider.Settings.System.TORCH_POWER_BUTTON_GESTURE;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;

import com.android.settings.gestures.GesturePreferenceController;

public class PowerButtonTorchGesturePreferenceController extends GesturePreferenceController {

    private final int ON = 1;
    private final int OFF = 0;

    private final String PREF_KEY_VIDEO = "gesture_quick_torch_video";

    public PowerButtonTorchGesturePreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return mContext.getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH) ? AVAILABLE :
                        UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), "power_button_torch");
    }

    @Override
    public String getVideoPrefKey() {
        return PREF_KEY_VIDEO;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        return Settings.System.putInt(mContext.getContentResolver(), TORCH_POWER_BUTTON_GESTURE,
                isChecked ? ON : OFF);
    }

    @Override
    public boolean isChecked() {
        return Settings.System.getInt(mContext.getContentResolver(), TORCH_POWER_BUTTON_GESTURE, OFF) != OFF;
    }
}
