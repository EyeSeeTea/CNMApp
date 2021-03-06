package org.eyeseetea.malariacare.layout.adapters.survey.strategies;


import android.view.View;
import android.widget.ScrollView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.layout.adapters.survey.DynamicTabAdapter;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.views.question.IQuestionView;

import java.util.List;

public class DynamicTabAdapterStrategy extends ADynamicTabAdapterStrategy {

    public DynamicTabAdapterStrategy(DynamicTabAdapter dynamicTabAdapter) {
        super(dynamicTabAdapter);
    }

    @Override
    public boolean HasQuestionImageVisibleInHeader(Integer output) {
        return output != Constants.SWITCH_BUTTON && output != Constants.QUESTION_LABEL
                && output != Constants.RADIO_GROUP_HORIZONTAL && output != Constants.REMINDER
                && output != Constants.WARNING;
    }
    @Override
    public void initSurveys(boolean readOnly) {
        return;
    }

    @Override
    public List<QuestionDB> addAdditionalQuestions(int tabType, List<QuestionDB> screenQuestions) {
        return null;
    }

    @Override
    public void instanceOfSingleQuestion(IQuestionView questionView, QuestionDB screenQuestion) {
        return;
    }

    @Override
    public void instanceOfMultiQuestion(IQuestionView questionView, QuestionDB screenQuestion) {
        return;
    }

    @Override
    public void renderParticularSurvey(QuestionDB screenQuestion, SurveyDB survey,
            IQuestionView questionView) {
        return;
    }

    @Override
    public boolean isMultiQuestionByVariant(int tabType) {
        return tabType == Constants.TAB_MULTI_QUESTION_EXCLUSIVE;
    }

    @Override
    public void configureAnswerChangedListener(DynamicTabAdapter dynamicTabAdapter,
            IQuestionView questionView) {
        return;
    }

    @Override
    public void addScrollToSwipeTouchListener(View rowView) {
        DynamicTabAdapter.swipeTouchListener.addScrollView((ScrollView) (rowView.findViewById(
                R.id.scrolled_table)).findViewById(R.id.table_scroll));
    }

    @Override
    protected boolean shouldShowReviewScreen() {
        return true;
    }
}
