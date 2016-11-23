/*
 * SPDX-FileCopyrightText: 2019 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.gestures;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;

public class VolumeButtonMusicControlGestureSettings extends DashboardFragment {

    private static final String TAG = "VolumeButtonMusicControlGestureSettings";

    @Override
    public int getMetricsCategory() {
        return -1;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.volume_button_music_control_gesture_settings;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.volume_button_music_control_gesture_settings);
}
