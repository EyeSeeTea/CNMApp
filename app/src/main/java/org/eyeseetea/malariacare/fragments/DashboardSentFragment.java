/*
 * Copyright (c) 2015.
 *
 * This file is part of QIS Surveillance App.
 *
 *  QIS Surveillance App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  QIS Surveillance App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with QIS Surveillance App.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.adapters.dashboard.AssessmentAdapter;
import org.eyeseetea.malariacare.layout.adapters.dashboard.strategies.HeaderUseCase;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.services.SurveyService;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class DashboardSentFragment extends ListFragment {


    public static final String TAG = ".SentFragment";
    protected AssessmentAdapter adapter;
    private SurveyReceiver surveyReceiver;
    private List<Survey> surveys;

    public DashboardSentFragment() {
        this.surveys = new ArrayList();
    }

    public static DashboardSentFragment newInstance(int index) {
        DashboardSentFragment f = new DashboardSentFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("index", index);
        f.setArguments(args);

        return f;
    }


    public int getShownIndex() {
        return getArguments().getInt("index", 0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        if (container == null) {
            return null;
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        initAdapter();
        initListView();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        registerSurveysReceiver();
        super.onResume();
    }

    /**
     * Inits adapter.
     * Most of times is just an AssessmentAdapter.
     * In a version with several adapters in dashboard (like in 'mock' branch) a new one like the
     * one in session is created.
     */
    private void initAdapter() {
        this.adapter = new AssessmentAdapter(getString(R.string.assessment_sent_title_header),
                this.surveys, getActivity());
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Log.d(TAG, "onListItemClick");
        super.onListItemClick(l, v, position, id);

        //Discard clicks on header|footer (which is attended on newSurvey via super)
        if (!isPositionASurvey(position)) {
            return;
        }

        //Put selected survey in session
        Session.setSurvey(surveys.get(position - 1));
        // Go to SurveyActivity
        DashboardActivity.dashboardActivity.openSentSurvey();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        unregisterSurveysReceiver();

        super.onStop();
    }

    /**
     * Checks if the given position points to a real survey instead of a footer or header of the
     * listview.
     *
     * @return true|false
     */
    private boolean isPositionASurvey(int position) {
        return !isPositionFooter(position) && !isPositionHeader(position);
    }

    /**
     * Checks if the given position is the header of the listview instead of a real survey
     *
     * @return true|false
     */
    private boolean isPositionHeader(int position) {
        return position <= 0;
    }

    /**
     * Checks if the given position is the footer of the listview instead of a real survey
     *
     * @return true|false
     */
    private boolean isPositionFooter(int position) {
        return position == (this.surveys.size() + 1);
    }

    /**
     * Initializes the listview component, adding a listener for swiping right
     */
    private void initListView() {
        if (Session.isNotFullOfUnsent(getActivity())) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View header = HeaderUseCase.getInstance().loadHeader(this.adapter.getHeaderLayout(),
                    inflater);
            View footer = inflater.inflate(this.adapter.getFooterLayout(), null, false);
            ListView listView = getListView();
            if (header != null) {
                listView.addHeaderView(header);
            }
            listView.addFooterView(footer);
            LayoutUtils.setRowDivider(listView);
            setListAdapter((BaseAdapter) adapter);
            setListShown(false);
        }
    }


    /**
     * Register a survey receiver to load surveys into the listadapter
     */
    public void registerSurveysReceiver() {
        Log.d(TAG, "registerSurveysReceiver");

        if (surveyReceiver == null) {
            surveyReceiver = new SurveyReceiver();
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(surveyReceiver,
                    new IntentFilter(SurveyService.ALL_SENT_SURVEYS_ACTION));
        }
    }


    /**
     * Unregisters the survey receiver.
     * It really important to do this, otherwise each receiver will invoke its code.
     */
    public void unregisterSurveysReceiver() {
        Log.d(TAG, "unregisterSurveysReceiver");
        if (surveyReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(surveyReceiver);
            surveyReceiver = null;
        }
    }

    public void reloadSurveys(List<Survey> newListSurveys) {
        Log.d(TAG, "reloadSurveys (Thread: " + Thread.currentThread().getId() + "): "
                + newListSurveys.size());
        this.surveys.clear();
        this.surveys.addAll(newListSurveys);
        this.adapter.notifyDataSetChanged();
        setListShown(true);
    }

    public void reloadHeader(Activity activity) {
        HeaderUseCase.getInstance().init(activity, R.string.tab_tag_improve);
    }

    public void reloadData() {
        //Reload data using service
        Intent surveysIntent = new Intent(
                PreferencesState.getInstance().getContext().getApplicationContext(),
                SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.ALL_SENT_SURVEYS_ACTION);
        PreferencesState.getInstance().getContext().getApplicationContext().startService(
                surveysIntent);
    }

    /**
     * Inner private class that receives the result from the service
     */
    private class SurveyReceiver extends BroadcastReceiver {
        private SurveyReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive");
            //Listening only intents from this method
            if (SurveyService.ALL_SENT_SURVEYS_ACTION.equals(intent.getAction())) {
                List<Survey> surveysFromService;
                Session.valuesLock.readLock().lock();
                try {
                    surveysFromService = (List<Survey>) Session.popServiceValue(
                            SurveyService.ALL_SENT_SURVEYS_ACTION);
                } finally {
                    Session.valuesLock.readLock().unlock();
                }
                reloadSurveys(surveysFromService);
            }
        }
    }
}