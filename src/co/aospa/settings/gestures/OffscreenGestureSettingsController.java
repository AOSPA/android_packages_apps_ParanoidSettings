/*
 * SPDX-FileCopyrightText: 2024 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.settings.gestures;

import android.content.Context;
import com.android.internal.R;
import com.android.settings.core.BasePreferenceController;

public class OffscreenGestureSettingsController extends BasePreferenceController {

    public OffscreenGestureSettingsController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        // Disable offscreen gestures page if KeyHandler is disabled.
        final boolean mKeyHandlerEnabled = mContext.getResources().getBoolean(R.bool.config_enableKeyHandler);
        return mKeyHandlerEnabled ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }
}
