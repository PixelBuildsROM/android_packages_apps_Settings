/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.deviceinfo.firmwareversion;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.net.Uri;
import android.os.SystemProperties;
import android.os.SystemClock;
import android.util.Log;

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class CustomFirmwareVersionPreferenceController extends BasePreferenceController {
    private static final String TAG = "CustomFirmwareVersion";
    private static final String VERSION_PROPERTY = "ro.build.version.custom";
    private static final String BUILD_TYPE_PROPERTY = "ro.pb.buildtype";
    private static final String DEVICE_CODENAME_PROPERTY = "ro.build.version.device";
    private static final int DELAY_TIMER_MILLIS = 1500;
    private static final int ACTIVITY_TRIGGER_COUNT = 10;

    private final long[] mHits = new long[ACTIVITY_TRIGGER_COUNT];
    private final PackageManager mPackageManager;

    public CustomFirmwareVersionPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        mPackageManager = mContext.getPackageManager();
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public CharSequence getSummary() {
        String internalVer = SystemProperties.get(VERSION_PROPERTY,
                mContext.getString(R.string.device_info_default));
        String deviceCodename = SystemProperties.get(DEVICE_CODENAME_PROPERTY, 
                mContext.getString(R.string.device_info_default));
        String buildType = SystemProperties.get(BUILD_TYPE_PROPERTY,
                mContext.getString(R.string.device_info_default));
        // Only append build type when this is a release build
        if (buildType != null && "release".equals(buildType)) {
            return internalVer + " | " + deviceCodename + " | " + buildType;
        } else {
            return internalVer + " | " + deviceCodename;
        }
    }
    
    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), getPreferenceKey())) {
            return false;
        }
        arrayCopy();
        mHits[mHits.length - 1] = SystemClock.uptimeMillis();
        if (mHits[0] >= (SystemClock.uptimeMillis() - DELAY_TIMER_MILLIS)) {
            final Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(mContext.getString(R.string.custom_easter_uri)));
            if (mPackageManager.queryIntentActivities(intent, 0).isEmpty()) {
            	// Don't send out the intent to stop crash
            	Log.w(TAG, "queryIntentActivities() returns empty");
            	return true;
            }
            try {
                mContext.startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Unable to start activity " + intent.toString());
            }
        }
        return true;
    }
    /**
     * Copies the array onto itself to remove the oldest hit.
     */
    void arrayCopy() {
        System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
    }
}
