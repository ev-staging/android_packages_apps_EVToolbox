/*
 * Copyright (C) 2013 The Evervolv Project
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

package com.evervolv.toolbox.fragments;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.preference.SwitchPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.Toolbox;
import com.evervolv.toolbox.custom.NumberPickerPreference;

public class InterfaceGeneral extends PreferenceFragment implements
        OnPreferenceChangeListener,
        Toolbox.DisabledListener {

    private static final String TAG = "EVToolbox";

    private static final String DENSITY_PICKER_PREF = "pref_interface_density_picker";
    private static final String LOCKSCREEN_MUSIC_CTRL_VOLBTN = "pref_lockscreen_music_controls_volbtn";
    //private static final String FANCY_UI = "pref_interface_fancy_ui";
    //private static final String SETTINGS_SWITCH_TOGGLE = "pref_interface_settings_switch_toggle";

    private static final String ASSIST_WAKE_TOGGLE = "pref_assist_wake_toggle";
    private static final String APP_SWITCH_WAKE_TOGGLE = "pref_app_switch_wake_toggle";
    private static final String BACK_WAKE_TOGGLE = "pref_back_wake_toggle";
    private static final String HOME_WAKE_TOGGLE = "pref_home_wake_toggle";
    private static final String MENU_WAKE_TOGGLE = "pref_menu_wake_toggle";
    private static final String VOLUME_WAKE_TOGGLE = "pref_volume_wake_toggle";

    private static final int MIN_DENSITY_VALUE = DisplayMetrics.DENSITY_LOW;
    private static final int MAX_DENSITY_VALUE = DisplayMetrics.DENSITY_XXXHIGH; //4k

    private SwitchPreference mAssistWake;
    private SwitchPreference mAppSwitchWake;
    private SwitchPreference mBackWake;
    private SwitchPreference mHomeWake;
    private SwitchPreference mMenuWake;
    private SwitchPreference mVolumeWake;
    private SwitchPreference mSettingsSwitch;

    private SwitchPreference mMusicCtrlVolBtn;
    private SwitchPreference mFancyUi;
    private NumberPickerPreference mDensityPicker;
    private ContentResolver mCr;
    private PreferenceScreen mPrefSet;

    private int mRecommendedDpi;
    private int mDefaultDpi;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.interface_general);

        mPrefSet = getPreferenceScreen();

        mCr = getActivity().getContentResolver();

        /* App switch wake pref */
        mAppSwitchWake = (SwitchPreference) mPrefSet.findPreference(APP_SWITCH_WAKE_TOGGLE);
        mAppSwitchWake.setChecked(Settings.System.getInt(mCr,
                Settings.System.APP_SWITCH_WAKE_SCREEN, 0) == 1);

        /* Assist wake pref */
        mAssistWake = (SwitchPreference) mPrefSet.findPreference(ASSIST_WAKE_TOGGLE);
        mAssistWake.setChecked(Settings.System.getInt(mCr,
                Settings.System.ASSIST_WAKE_SCREEN, 0) == 1);

        /* Back wake pref */
        mBackWake = (SwitchPreference) mPrefSet.findPreference(BACK_WAKE_TOGGLE);
        mBackWake.setChecked(Settings.System.getInt(mCr,
                Settings.System.BACK_WAKE_SCREEN, 0) == 1);

        /* Home wake pref */
        mHomeWake = (SwitchPreference) mPrefSet.findPreference(HOME_WAKE_TOGGLE);
        mHomeWake.setChecked(Settings.System.getInt(mCr,
                Settings.System.HOME_WAKE_SCREEN, 0) == 1);

        /* Menu wake pref */
        mMenuWake = (SwitchPreference) mPrefSet.findPreference(MENU_WAKE_TOGGLE);
        mMenuWake.setChecked(Settings.System.getInt(mCr,
                Settings.System.MENU_WAKE_SCREEN, 0) == 1);

        /* Only show wake keys if the device has hardware buttons */
        if (getResources().getBoolean(R.bool.has_physical_buttons)) {
            /* Allow an overlay to determine the wake keys the specific device has available */
            if (!getResources().getBoolean(R.bool.has_app_switch_button)) {
                mPrefSet.removePreference(mAppSwitchWake);
            }
            if (!getResources().getBoolean(R.bool.has_assist_button)) {
                mPrefSet.removePreference(mAssistWake);
            }
            if (!getResources().getBoolean(R.bool.has_back_button)) {
                mPrefSet.removePreference(mBackWake);
            }
            if (!getResources().getBoolean(R.bool.has_home_button)) {
                mPrefSet.removePreference(mHomeWake);
            }
            if (!getResources().getBoolean(R.bool.has_menu_button)) {
                mPrefSet.removePreference(mMenuWake);
            }
        } else {
            /* No hardware buttons, no problem */
            mPrefSet.removePreference(mAppSwitchWake);
            mPrefSet.removePreference(mAssistWake);
            mPrefSet.removePreference(mBackWake);
            mPrefSet.removePreference(mHomeWake);
            mPrefSet.removePreference(mMenuWake);
        }

        /* Volume wake pref */
        mVolumeWake = (SwitchPreference) mPrefSet.findPreference(VOLUME_WAKE_TOGGLE);
        mVolumeWake.setChecked(Settings.System.getInt(mCr,
                Settings.System.VOLUME_WAKE_SCREEN, 0) == 1);

        /* Volume button music controls */
        mMusicCtrlVolBtn = (SwitchPreference) mPrefSet.findPreference(LOCKSCREEN_MUSIC_CTRL_VOLBTN);
        mMusicCtrlVolBtn.setChecked(Settings.System.getInt(mCr,
                Settings.System.LOCKSCREEN_MUSIC_CONTROLS_VOLBTN, 1) == 1);

        /* Density picker */
        int currentDensity = SystemProperties.getInt("persist.sys.fake_density", 0);
        if (currentDensity == 0) {
            currentDensity = SystemProperties.getInt("ro.sf.lcd_density", DisplayMetrics.DENSITY_DEFAULT);
        }
        mRecommendedDpi = Integer.valueOf(getActivity().getString(R.string.config_recommendedTabletDpi));
        mDefaultDpi = SystemProperties.getInt("ro.sf.lcd_density", DisplayMetrics.DENSITY_DEFAULT);
        mDensityPicker = (NumberPickerPreference) mPrefSet.findPreference(DENSITY_PICKER_PREF);
        mDensityPicker.setOnPreferenceChangeListener(this);
        mDensityPicker.setSummary(String.format(getActivity().getString(
                R.string.pref_interface_density_picker_summary,
                currentDensity, mRecommendedDpi, mDefaultDpi)));
        mDensityPicker.setMinValue(MIN_DENSITY_VALUE);
        mDensityPicker.setMaxValue(MAX_DENSITY_VALUE);
        mDensityPicker.setCurrentValue(currentDensity);
    }

    @Override
    public void onStart() {
        super.onStart();
        mPrefSet.setEnabled(Toolbox.isEnabled(getActivity()));
        ((Toolbox) getActivity()).registerCallback(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        ((Toolbox) getActivity()).unRegisterCallback(this);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;

        if (preference == mAssistWake) {
            value = mAssistWake.isChecked();
            Settings.System.putInt(mCr, Settings.System.ASSIST_WAKE_SCREEN,
                    value ? 1 : 0);
            return true;
        } else if (preference == mAppSwitchWake) {
            value = mAppSwitchWake.isChecked();
            Settings.System.putInt(mCr, Settings.System.APP_SWITCH_WAKE_SCREEN,
                    value ? 1 : 0);
            return true;
        } else if (preference == mBackWake) {
            value = mBackWake.isChecked();
            Settings.System.putInt(mCr, Settings.System.BACK_WAKE_SCREEN,
                    value ? 1 : 0);
            return true;
        } else if (preference == mHomeWake) {
            value = mHomeWake.isChecked();
            Settings.System.putInt(mCr, Settings.System.HOME_WAKE_SCREEN,
                    value ? 1 : 0);
            return true;
        } else if (preference == mMenuWake) {
            value = mMenuWake.isChecked();
            Settings.System.putInt(mCr, Settings.System.MENU_WAKE_SCREEN,
                    value ? 1 : 0);
            return true;
        } else if (preference == mVolumeWake) {
            value = mVolumeWake.isChecked();
            Settings.System.putInt(mCr, Settings.System.VOLUME_WAKE_SCREEN,
                    value ? 1 : 0);
            return true;
        } else if (preference == mMusicCtrlVolBtn) {
            value = mMusicCtrlVolBtn.isChecked();
            Settings.System.putInt(mCr, Settings.System.LOCKSCREEN_MUSIC_CONTROLS_VOLBTN,
                    value ? 1 : 0);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mDensityPicker) {
            int fakeDensity = (Integer) newValue;
            int realDensity = SystemProperties.getInt("ro.sf.lcd_density", DisplayMetrics.DENSITY_DEFAULT);
            if (fakeDensity != realDensity) {
                // Use the new value
                SystemProperties.set("persist.sys.fake_density", String.valueOf(fakeDensity));
            } else {
                // Set zero to use default
                SystemProperties.set("persist.sys.fake_density", "0");
            }
            mDensityPicker.setSummary(String.format(getActivity().getString(
                    R.string.pref_interface_density_picker_summary,
                    fakeDensity, mRecommendedDpi, mDefaultDpi)));
            requestReboot(R.string.pref_interface_reboot_required_text);
        }
        return false;
    }

    private void requestReboot(int messageId) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.reboot_required)
                .setMessage(getString(messageId))
                .setPositiveButton(getString(R.string.reboot),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                PowerManager power = (PowerManager) getActivity()
                                        .getSystemService(Context.POWER_SERVICE);
                                power.reboot("Toolbox");
                            }
                        })
                .setNegativeButton(getString(R.string.later),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                .show();
    }

    @Override
    public void onToolboxDisabled(boolean enabled) {
        mPrefSet.setEnabled(enabled);
    }
}
