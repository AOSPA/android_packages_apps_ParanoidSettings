/*
 * SPDX-FileCopyrightText: 2021 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.gestures;

import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.widget.SwitchWidgetController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class OffscreenGestureEnabler implements SwitchWidgetController.OnSwitchChangeListener,
        LifecycleObserver, OnResume, OnPause {
    private final Context mContext;
    private final SwitchWidgetController mSwitchWidgetController;
    private final OnGestureSwitchChangeListener mListener;

    OffscreenGestureEnabler(Context context, SwitchWidgetController switchWidgetController,
            OnGestureSwitchChangeListener listener, Lifecycle lifecycle) {
        mContext = context;
        mSwitchWidgetController = switchWidgetController;
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }

        final boolean enabled = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.GESTURES_ENABLED, 1, UserHandle.USER_CURRENT) != 0;
        mSwitchWidgetController.setChecked(enabled);
        mSwitchWidgetController.setTitle(mContext.getString(R.string.gestures_master));
        mSwitchWidgetController.setupView();
        mListener = listener;

        mSwitchWidgetController.setListener(this);
    }

    @Override
    public void onPause() {
        try {
            mSwitchWidgetController.stopListening();
        } catch (IllegalStateException e) {
            // Ignore
        }
    }

    @Override
    public void onResume() {
        try {
            mSwitchWidgetController.startListening();
        } catch (IllegalStateException e) {
            // Ignore
        }
    }

    @Override
    public boolean onSwitchToggled(boolean isChecked) {
        final boolean succeed = Settings.System.putIntForUser(mContext.getContentResolver(),
                Settings.System.GESTURES_ENABLED, isChecked ? 1 : 0, UserHandle.USER_CURRENT);
        if (succeed && mListener != null) {
            mListener.onChanged(isChecked);
        }
        return succeed;
    }

    public void teardownSwitchController() {
        try {
            mSwitchWidgetController.stopListening();
        } catch (IllegalStateException e) {
            // Ignore
        }
        mSwitchWidgetController.teardownView();
    }

    public interface OnGestureSwitchChangeListener {
        void onChanged(boolean enabled);
    }
}
