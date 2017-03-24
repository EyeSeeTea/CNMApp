package org.eyeseetea.malariacare.presentation.factory.monitor.rows;

import android.content.Context;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.presentation.factory.monitor.utils.SurveyMonitor;

/**
 * Created by idelcano on 21/07/2016.
 */
public class ACT6RowBuilder extends CounterRowBuilder {
    public ACT6RowBuilder(Context context) {
        super(context, context.getString(R.string.ACT_x_6));
    }

    @Override
    protected Integer incrementCount(SurveyMonitor surveyMonitor) {
        return 0;
    }
}
