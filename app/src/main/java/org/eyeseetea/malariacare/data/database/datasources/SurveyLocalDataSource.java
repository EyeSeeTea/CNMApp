package org.eyeseetea.malariacare.data.database.datasources;

import org.eyeseetea.malariacare.data.IDataSourceCallback;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.database.model.ValueDB;
import org.eyeseetea.malariacare.domain.boundary.repositories.ISurveyRepository;
import org.eyeseetea.malariacare.domain.entity.Program;
import org.eyeseetea.malariacare.domain.entity.Question;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.entity.Value;

import java.util.ArrayList;
import java.util.List;

public class SurveyLocalDataSource implements ISurveyRepository {
    @Override
    public List<Survey> getLastSentSurveys(int count) {
        List<Survey> surveys = new ArrayList<>();

        List<SurveyDB> surveysInDB =
                SurveyDB.getSentSurveys(
                        count);

        for (SurveyDB surveyDBDB : surveysInDB) {
            Survey survey = new Survey(surveyDBDB.getEventDate());

            surveys.add(survey);
        }

        return surveys;
    }

    @Override
    public void deleteSurveys() {
        List<SurveyDB> surveyDBs =
                SurveyDB.getAllSurveys();
        for (SurveyDB surveyDB : surveyDBs) {
            surveyDB.delete();
        }
    }

    @Override
    public void getUnsentSurveys(IDataSourceCallback<List<Survey>> callback) {
        List<Survey> unsentSurveys = getUnsentSurveys();
        callback.onSuccess(unsentSurveys);
    }

    @Override
    public List<Survey> getUnsentSurveys() {
        List<Survey> unsentSurveys = new ArrayList<>();
        for (SurveyDB surveyDB : SurveyDB.getAllUnsentSurveys()) {
            Survey survey = new Survey(surveyDB.getEventDate());
            unsentSurveys.add(survey);
        }
        return unsentSurveys;
    }

    @Override
    public List<Survey> getAllQuarantineSurveys() {
        List<SurveyDB> surveyDBs = SurveyDB.getAllQuarantineSurveys();
        List<Survey> surveys = new ArrayList<>();
        for(SurveyDB surveyDB : surveyDBs){
            surveys.add(new Survey(surveyDB.getId_survey(), surveyDB.getStatus(), null));
        }
        return surveys;
    }

    @Override
    public List<Survey> getAllCompletedSurveys() {
        List<Survey> completedSurveys = new ArrayList<>();
        for (SurveyDB surveyDB : SurveyDB.getAllCompletedSurveys()) {
            Survey survey = new Survey(surveyDB.getEventDate());
            completedSurveys.add(survey);
        }
        return completedSurveys;
    }

    @Override
    public long save(Survey survey) {
        SurveyDB surveyDB = SurveyDB.findById(survey.getId());
        if (surveyDB == null) {
            OrgUnitDB orgUnitDB = null;
            if (survey.getOrganisationUnit() != null) {
                orgUnitDB = OrgUnitDB.findByUID(survey.getOrganisationUnit().getUid());
            }
            UserDB userDB = null;
            if (survey.getUserAccount() != null) {
                userDB = UserDB.findByUID(survey.getUserAccount().getUserUid());
            }
            surveyDB = new SurveyDB(orgUnitDB, ProgramDB.getProgram(survey.getProgram().getId()),
                    userDB, survey.getType());
            surveyDB.save();
        }
        surveyDB.setStatus(survey.getStatus());
        surveyDB.update();
        return surveyDB.getId_survey();
    }


    public List<Survey> getSurveysByProgram(String uid) {
        List<SurveyDB> surveysDB = SurveyDB.getSurveysWithProgram(uid);
        List<Survey> surveys = new ArrayList<>();

        for (SurveyDB surveyDB : surveysDB) {
            List<Question> questions = getQuestionsBySurvey(surveyDB);
            ProgramDB programDB = surveyDB.getProgramDB();
            Program program = new Program(programDB.getName(), programDB.getUid());

            Survey survey = new Survey.Builder()
                    .id(surveyDB.getId_survey())
                    .program(program)
                    .type(surveyDB.getType())
                    .questions(questions)
                    .surveyDate(surveyDB.getCompletionDate()    )
                    .build();
            surveys.add(survey);
        }
        return surveys;
    }

    private List<Question> getQuestionsBySurvey(SurveyDB surveyDB) {
        List<QuestionDB> questionsDB = surveyDB.getQuestionsFromValues();
        List<Question> questions = new ArrayList<>();
        for (QuestionDB questionDB : questionsDB) {
            ValueDB valueDB = questionDB.getValueBySurvey(surveyDB);
            Value value = new Value(valueDB.getValue());
            Question question = new Question.Builder()
                    .code(questionDB.getCode())
                    .id(questionDB.getId_question())
                    .name(questionDB.getForm_name())
                    .uid(questionDB.getUid())
                    .type(QuestionLocalDataSource.mapOutputToQuestionType(
                            questionDB.getOutput()))
                    .value(value)
                    .build();
            questions.add(question);
        }
        return questions;
    }
}
