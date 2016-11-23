/*
 * SPDX-FileCopyrightText: 2019 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.gestures;

import static android.provider.Settings.System.VOLBTN_MUSIC_CONTROLS;

import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;

import com.android.settings.gestures.GesturePreferenceController;

public class VolumeButtonMusicControlPreferenceController extends GesturePreferenceController {

    private final int ON = 1;
    private final int OFF = 0;

    private static final String PREF_KEY_VIDEO = "volume_button_music_control_video";

    public VolumeButtonMusicControlPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), "volume_button_music_control");
    }

    @Override
    protected String getVideoPrefKey() {
        return PREF_KEY_VIDEO;
    }

    @Override
    public boolean isChecked() {
        return Settings.System.getIntForUser(mContext.getContentResolver(), VOLBTN_MUSIC_CONTROLS,
                OFF, UserHandle.USER_CURRENT) == ON;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        return Settings.System.putIntForUser(mContext.getContentResolver(), VOLBTN_MUSIC_CONTROLS,
                isChecked ? ON : OFF, UserHandle.USER_CURRENT);
    }
}
