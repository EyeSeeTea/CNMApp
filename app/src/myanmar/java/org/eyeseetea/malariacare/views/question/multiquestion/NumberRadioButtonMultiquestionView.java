package org.eyeseetea.malariacare.views.question.multiquestion;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.OptionDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.ValueDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.domain.entity.TreatmentQueries;
import org.eyeseetea.malariacare.layout.utils.BaseLayoutUtils;
import org.eyeseetea.malariacare.views.question.AKeyboardQuestionView;
import org.eyeseetea.malariacare.views.question.AOptionQuestionView;
import org.eyeseetea.malariacare.views.question.CommonQuestionView;
import org.eyeseetea.malariacare.views.question.IImageQuestionView;
import org.eyeseetea.malariacare.views.question.IMultiQuestionView;
import org.eyeseetea.malariacare.views.question.IQuestionView;
import org.eyeseetea.sdk.presentation.views.CustomRadioButton;
import org.eyeseetea.sdk.presentation.views.CustomTextView;

import java.util.List;

public class NumberRadioButtonMultiquestionView extends CommonQuestionView implements IQuestionView,
        IMultiQuestionView, IImageQuestionView, RadioGroup.OnCheckedChangeListener {
    CustomTextView header;
    ImageView image;
    RadioGroup radioGroup;
    Context context;
    QuestionDB mQuestionDB;

    String yesOptionIdentifier;
    String noOptionIdentifier;

    protected AKeyboardQuestionView.onAnswerChangedListener mOnAnswerChangedListener;
    protected AOptionQuestionView.onAnswerChangedListener mOnAnswerOptionChangedListener;
    float dose = 0;

    public NumberRadioButtonMultiquestionView(Context context) {
        super(context);
        this.context = context;
        init(context);
    }

    public void setDose(float dose) {
        this.dose = dose;
    }

    @Override
    public void setHeader(String headerValue) {
        header.setText(headerValue);
    }

    @Override
    public boolean hasError() {
        if (mQuestionDB.isCompulsory() && radioGroup.getCheckedRadioButtonId() == -1) {
            return true;
        }
        return false;
    }

    @Override
    public void setHelpText(String helpText) {

    }

    @Override
    public void setValue(ValueDB value) {
        if (value == null || value.getValue() == null) {
            return;
        }
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            CustomRadioButton customRadioButton = (CustomRadioButton) radioGroup.getChildAt(i);
            OptionDB option = (OptionDB) customRadioButton.getTag();
            if (isOptionIdentifier(option, noOptionIdentifier)
                    && Float.parseFloat(value.getValue()) == 0) {
                customRadioButton.setChecked(true);
            } else if (isOptionIdentifier(option, yesOptionIdentifier)
                    && Float.parseFloat(value.getValue()) > 0) {
                customRadioButton.setChecked(true);
            }
        }

    }

    private boolean isOptionIdentifier(OptionDB option, String optionIdentifier) {
        return option.getName().equals(optionIdentifier) ||
                option.getCode().equals(optionIdentifier);
    }

    @Override
    public void setImage(String path) {
        if (path != null && !path.equals("")) {
            BaseLayoutUtils.makeImageVisible(path, image);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        radioGroup.setEnabled(enabled);
    }

    public void setQuestionDB(QuestionDB questionDB) {
        this.mQuestionDB = questionDB;
    }

    public void setOptions(List<OptionDB> options) {
        LayoutInflater lInflater = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        for (OptionDB option : options) {
            CustomRadioButton radioButton =
                    (CustomRadioButton) lInflater.inflate(
                            R.layout.uncheckeable_radiobutton, null);
            radioButton.setTag(option);
            radioButton.setText(option.getInternationalizedName());
            fixRadioButtonWidth(radioButton);

            radioButton.setEnabled(radioGroup.isEnabled());

            radioGroup.addView(radioButton);
            radioGroup.setOnCheckedChangeListener(this);
        }
    }


    private void init(final Context context) {
        View view = inflate(context, R.layout.multi_question_radio_button_row, this);
        header = (CustomTextView) view.findViewById(R.id.row_header_text);
        image = (ImageView) view.findViewById(R.id.question_image_row);
        radioGroup = (RadioGroup) view.findViewById(R.id.answer);
        radioGroup.setOrientation(HORIZONTAL);

        yesOptionIdentifier = context.getString(R.string.yes_option_identifier);
        noOptionIdentifier = context.getString(R.string.no_option_identifier);
    }


    private void fixRadioButtonWidth(
            CustomRadioButton radioButton) {
        Drawable radioButtonIcon = getResources().getDrawable(R.drawable.radio_on);
        BaseLayoutUtils.setLayoutParamsAs50Percent(radioButton, context,
                calculateFixedWidth(radioButtonIcon));
    }

    private int calculateFixedWidth(Drawable radioButtonIcon) {
        int width = radioButtonIcon.getIntrinsicWidth();
        int height = radioButtonIcon.getIntrinsicHeight();
        int fixedHeight =
                PreferencesState.getInstance().getContext().getResources()
                        .getDimensionPixelSize(
                                R.dimen.image_size);
        int newHeightPercent = (fixedHeight * 100) / height;
        return (newHeightPercent * width) / 100;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        int value = 0;
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            CustomRadioButton customRadioButton = (CustomRadioButton) radioGroup.getChildAt(i);
            OptionDB option = (OptionDB) customRadioButton.getTag();
            if (isOptionIdentifier(option, noOptionIdentifier)
                    && checkedId == customRadioButton.getId()) {
                value = 0;
            } else if (isOptionIdentifier(option, yesOptionIdentifier)
                    && checkedId == customRadioButton.getId()) {
                value = (int) dose;
            }
            QuestionDB questionDB = (QuestionDB) this.getTag();
            if (checkedId == customRadioButton.getId()
                    && !TreatmentQueries.isCq(questionDB.getUid())) {
                notifyAnswerOptionChange(this.getTag(), ((OptionDB) customRadioButton.getTag()));
                changeTotalQuestions();
            }
        }


        notifyAnswerChanged(String.valueOf(value));
    }

    protected void notifyAnswerOptionChange(Object tag, OptionDB option) {
        if (mOnAnswerOptionChangedListener != null) {
            View view = new View(context);
            view.setTag(TreatmentQueries.getTreatmentQuestionForTag(tag));
            mOnAnswerOptionChangedListener.onAnswerChanged(view, option);
        }
    }

    public void setOnAnswerOptionChangedListener(
            AOptionQuestionView.onAnswerChangedListener onAnswerOptionChangedListener) {
        mOnAnswerOptionChangedListener = onAnswerOptionChangedListener;
    }

    protected void notifyAnswerChanged(String newValue) {
        if (mOnAnswerChangedListener != null) {
            mOnAnswerChangedListener.onAnswerChanged(this, newValue);
        }
    }

    public void setOnAnswerChangedListener(
            AKeyboardQuestionView.onAnswerChangedListener onAnswerChangedListener) {
        mOnAnswerChangedListener = onAnswerChangedListener;
    }

    /**
     * Changing the total questions of the alternative pq questions depending on the answer provided
     */
    private void changeTotalQuestions() {
        QuestionDB pqQuestionDB = TreatmentQueries.getPqQuestion();
        QuestionDB actQuestionDB =
                TreatmentQueries.getAlternativePqQuestion();//// FIXME: 14/03/2017 is it correct?
        QuestionDB alternativePqQuestionDB = TreatmentQueries.getAlternativePqQuestion();
        ValueDB actValue = null;
        ValueDB pqValue = null;
        List<ValueDB> values = Session.getMalariaSurveyDB().getValuesFromDB();
        for (ValueDB sValue : values) {
            if (sValue.getQuestionDB() == null) {
                continue;
            }
            if (sValue.getQuestionDB().equals(actQuestionDB)) {
                actValue = sValue;
                break;
            }
            if (sValue.getQuestionDB().getUid().equals(pqQuestionDB.getUid())) {
                pqValue = sValue;
                break;
            }
        }
        if ((actValue == null || actValue.getOptionDB().getCode().equals(yesOptionIdentifier))
                || (pqValue == null || Float.parseFloat(pqValue.getValue()) > 0)) {
            alternativePqQuestionDB.setTotalQuestions(8);
        } else {
            alternativePqQuestionDB.setTotalQuestions(9);
        }
        alternativePqQuestionDB.save();
    }
}
