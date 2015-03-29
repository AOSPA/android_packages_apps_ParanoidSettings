/*
 * SPDX-FileCopyrightText: 2017 The Android Open Source Project
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.notification;

import static com.android.settings.notification.SettingPref.TYPE_SYSTEM;

import android.content.Context;

import android.provider.Settings.System;

import com.android.settings.notification.SettingPref;
import com.android.settings.notification.SettingPrefController;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class ScreenshotSoundPreferenceController extends SettingPrefController {

    private static final String KEY_SCREENSHOT_SOUNDS = "screenshot_shutter_sound";

    public ScreenshotSoundPreferenceController(Context context, SettingsPreferenceFragment parent,
            Lifecycle lifecycle) {
        super(context, parent, lifecycle);
        mPreference = new SettingPref(
            TYPE_SYSTEM, KEY_SCREENSHOT_SOUNDS, System.SCREENSHOT_SHUTTER_SOUND, DEFAULT_ON);
    }

}
