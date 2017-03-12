/*
 * Copyright (C) 2013-2017 The Evervolv Project
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
import android.content.Context;
import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.Toolbox;

public class StatusbarQuickSettings extends PreferenceFragment implements
        Toolbox.DisabledListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.statusbar_qs);
    }

    @Override
    public void onStart() {
        super.onStart();
        getPreferenceScreen().setEnabled(Toolbox.isEnabled(getActivity()));
        ((Toolbox) getActivity()).registerCallback(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        ((Toolbox) getActivity()).unRegisterCallback(this);
    }

    @Override
    public void onToolboxDisabled(boolean enabled) {
        getPreferenceScreen().setEnabled(enabled);
    }
}
