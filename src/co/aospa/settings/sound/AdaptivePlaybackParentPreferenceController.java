/*
 * SPDX-FileCopyrightText: 2020-2021 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.sound;

import static android.provider.Settings.System.ADAPTIVE_PLAYBACK_ENABLED;
import static android.provider.Settings.System.ADAPTIVE_PLAYBACK_TIMEOUT;

import static co.aospa.settings.sound.AdaptivePlaybackSoundPreferenceController.ADAPTIVE_PLAYBACK_TIMEOUT_10_MIN;
import static co.aospa.settings.sound.AdaptivePlaybackSoundPreferenceController.ADAPTIVE_PLAYBACK_TIMEOUT_1_MIN;
import static co.aospa.settings.sound.AdaptivePlaybackSoundPreferenceController.ADAPTIVE_PLAYBACK_TIMEOUT_2_MIN;
import static co.aospa.settings.sound.AdaptivePlaybackSoundPreferenceController.ADAPTIVE_PLAYBACK_TIMEOUT_30_SECS;
import static co.aospa.settings.sound.AdaptivePlaybackSoundPreferenceController.ADAPTIVE_PLAYBACK_TIMEOUT_5_MIN;
import static co.aospa.settings.sound.AdaptivePlaybackSoundPreferenceController.ADAPTIVE_PLAYBACK_TIMEOUT_NONE;

import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class AdaptivePlaybackParentPreferenceController extends BasePreferenceController {

    public AdaptivePlaybackParentPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public CharSequence getSummary() {
        boolean enabled = Settings.System.getIntForUser(
                mContext.getContentResolver(), ADAPTIVE_PLAYBACK_ENABLED, 0,
                UserHandle.USER_CURRENT) != 0;
        int timeout = Settings.System.getIntForUser(
                mContext.getContentResolver(), ADAPTIVE_PLAYBACK_TIMEOUT,
                ADAPTIVE_PLAYBACK_TIMEOUT_30_SECS,
                UserHandle.USER_CURRENT);
        int summary = R.string.adaptive_playback_disabled_summary;
        if (!enabled) {
            return mContext.getText(summary);
        }
        switch (timeout) {
            case ADAPTIVE_PLAYBACK_TIMEOUT_NONE ->
                    summary = R.string.adaptive_playback_timeout_none_summary;
            case ADAPTIVE_PLAYBACK_TIMEOUT_30_SECS ->
                    summary = R.string.adaptive_playback_timeout_30_secs_summary;
            case ADAPTIVE_PLAYBACK_TIMEOUT_1_MIN ->
                    summary = R.string.adaptive_playback_timeout_1_min_summary;
            case ADAPTIVE_PLAYBACK_TIMEOUT_2_MIN ->
                    summary = R.string.adaptive_playback_timeout_2_min_summary;
            case ADAPTIVE_PLAYBACK_TIMEOUT_5_MIN ->
                    summary = R.string.adaptive_playback_timeout_5_min_summary;
            case ADAPTIVE_PLAYBACK_TIMEOUT_10_MIN ->
                    summary = R.string.adaptive_playback_timeout_10_min_summary;
        }
        return mContext.getText(summary);
    }
}
