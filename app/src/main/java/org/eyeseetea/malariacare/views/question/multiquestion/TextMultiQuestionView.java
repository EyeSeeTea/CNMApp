package org.eyeseetea.malariacare.views.question.multiquestion;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.EditText;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.ValueDB;
import org.eyeseetea.malariacare.views.question.AKeyboardQuestionView;
import org.eyeseetea.malariacare.views.question.IMultiQuestionView;
import org.eyeseetea.malariacare.views.question.IQuestionView;
import org.eyeseetea.malariacare.views.question.multiquestion.strategies
        .ATextMultiQuestionViewStrategy;
import org.eyeseetea.malariacare.views.question.multiquestion.strategies
        .TextMultiQuestionViewStrategy;
import org.eyeseetea.sdk.presentation.views.CustomEditText;
import org.eyeseetea.sdk.presentation.views.CustomTextView;

public class TextMultiQuestionView extends AKeyboardQuestionView implements IQuestionView,
        IMultiQuestionView {
    CustomTextView header;
    CustomEditText mCustomEditText;
    ATextMultiQuestionViewStrategy mTextMultiQuestionViewStrategy;


    public TextMultiQuestionView(Context context) {
        super(context);
        mTextMultiQuestionViewStrategy = new TextMultiQuestionViewStrategy();
        init(context);
    }

    @Override
    public EditText getAnswerView() {
        return mCustomEditText;
    }

    @Override
    public void setHeader(String headerValue) {
        header.setText(headerValue);
    }

    @Override
    public void setEnabled(boolean enabled) {
        mCustomEditText.setEnabled(enabled);
    }

    @Override
    public void setValue(ValueDB valueDB) {
        if (valueDB != null) {
            mCustomEditText.setText(valueDB.getValue());
        }
    }

    @Override
    public void setHelpText(String helpText) {
        mCustomEditText.setHint(helpText);
    }


    @Override
    public boolean hasError() {
        return false;
    }

    @Override
    public void requestAnswerFocus() {
        mCustomEditText.requestFocus();
        showKeyboard(getContext(), mCustomEditText);
    }


    private void init(Context context) {
        inflate(context, R.layout.multi_question_tab_text_row, this);

        header = (CustomTextView) findViewById(R.id.row_header_text);
        mCustomEditText = (CustomEditText) findViewById(R.id.answer);

        mCustomEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                mTextMultiQuestionViewStrategy.afterTextChange(TextMultiQuestionView.this);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                notifyAnswerChanged(String.valueOf(s));
            }
        });
        mTextMultiQuestionViewStrategy.init(this);
    }

    public void setInputType(int value) {
        mCustomEditText.setInputType(value | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
    }
}
