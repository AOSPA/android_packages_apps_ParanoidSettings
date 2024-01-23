/*
 * SPDX-FileCopyrightText: 2024 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.display;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

public class RefreshRatePreferenceController extends BasePreferenceController
        implements LifecycleObserver, OnStart, OnStop {

    private static final String TAG = "RefreshRatePreferenceController";

    private RefreshRateUtils mUtils;
    private Preference mPreference;
    private final PowerManager mPowerManager;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mPreference != null)
                updateState(mPreference);
        }
    };

    public RefreshRatePreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        mUtils = new RefreshRateUtils(context);
        mPowerManager = context.getSystemService(PowerManager.class);
    }

    @Override
    public void onStart() {
        mContext.registerReceiver(mReceiver,
                new IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED));
    }

    @Override
    public void onStop() {
        mContext.unregisterReceiver(mReceiver);
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
    }

    @Override
    public CharSequence getSummary() {
        if (mPowerManager.isPowerSaveMode()) {
            return mContext.getString(R.string.dark_ui_mode_disabled_summary_dark_theme_on);
        }
        return mContext.getString(mUtils.isVrrEnabled() ? R.string.refresh_rate_summary_vrr_on
                : R.string.refresh_rate_summary_vrr_off, mUtils.getCurrentRefreshRate());
    }

    @Override
    public int getAvailabilityStatus() {
        return mUtils.isHighRefreshRateAvailable() ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        preference.setEnabled(!mPowerManager.isPowerSaveMode());
    }
}
