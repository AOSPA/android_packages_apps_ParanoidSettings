/*
 * SPDX-FileCopyrightText: 2024 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.display;

import static android.hardware.display.DcDimmingManager.MODE_AUTO_OFF;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.hardware.display.DcDimmingManager;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;
import com.android.settingslib.widget.TwoTargetPreference;

public class DcDimmingPreference extends TwoTargetPreference {

    private CompoundButton mSwitch;
    private boolean mChecked;
    private final DcDimmingManager mDcDimmingManager;

    public DcDimmingPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDcDimmingManager = (DcDimmingManager) context.getSystemService(Context.DC_DIM_SERVICE);
        if (mDcDimmingManager != null && mDcDimmingManager.isAvailable()) {
            SettingsObserver settingsObserver =
                    new SettingsObserver(new Handler(Looper.getMainLooper()));
            settingsObserver.observe();
        }
    }

    @Override
    protected int getSecondTargetResId() {
        return androidx.preference.R.layout.preference_widget_switch_compat;
    }

    @Override
    protected void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy(preferenceManager);
        if (mDcDimmingManager == null || !mDcDimmingManager.isAvailable()) {
            setVisible(false);
            return;
        }
        boolean active = mDcDimmingManager.isDcDimmingOn();
        setChecked(active);
        updateSummary(active);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        final View widgetView = holder.findViewById(android.R.id.widget_frame);
        if (widgetView != null) {
            widgetView.setOnClickListener(v -> {
                if (mSwitch != null && !mSwitch.isEnabled()) {
                    return;
                }
                setUserChecked(!mChecked);
                if (!callChangeListener(mChecked)) {
                    setUserChecked(!mChecked);
                } else {
                    persistBoolean(mChecked);
                }
            });
        }

        mSwitch = (CompoundButton) holder.findViewById(androidx.preference.R.id.switchWidget);
        if (mSwitch != null) {
            mSwitch.setContentDescription(getTitle());
            mSwitch.setChecked(mChecked);
        }
    }

    public void setUserChecked(boolean checked) {
        setChecked(checked);
        mDcDimmingManager.setDcDimming(checked);
        updateSummary(mDcDimmingManager.isDcDimmingOn());
    }

    private void updateSummary(boolean active) {
        String summary;
        if (mDcDimmingManager.getAutoMode() != MODE_AUTO_OFF) {
            summary = getContext().getString(active
                    ? R.string.dark_ui_summary_on_auto_mode_auto
                    : R.string.dark_ui_summary_off_auto_mode_auto);
        } else {
            summary = getContext().getString(active
                    ? R.string.dark_ui_summary_on_auto_mode_never
                    : R.string.dark_ui_summary_off_auto_mode_never);
        }
        setSummary(summary);
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
        if (mSwitch != null) {
            mSwitch.setChecked(checked);
        }
    }

    private class SettingsObserver extends ContentObserver {

        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = getContext().getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.DC_DIMMING_AUTO_MODE), false, this,
                    UserHandle.USER_ALL);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.DC_DIMMING_STATE), false, this,
                    UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange) {
            updateSummary(mDcDimmingManager.isDcDimmingOn());
            setChecked(mDcDimmingManager.isDcDimmingOn());
        }
    }
}
