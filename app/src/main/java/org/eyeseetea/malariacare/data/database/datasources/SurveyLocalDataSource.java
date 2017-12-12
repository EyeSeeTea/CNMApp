package org.eyeseetea.malariacare.data.database.datasources;

import org.eyeseetea.malariacare.data.IDataSourceCallback;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.domain.boundary.repositories.ISurveyRepository;
import org.eyeseetea.malariacare.domain.entity.Survey;

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
        List<Survey> unsentSurveys = new ArrayList<>();
        for (SurveyDB surveyDB : SurveyDB.getAllUnsentSurveys()) {
            Survey survey = new Survey(surveyDB.getEventDate());
            unsentSurveys.add(survey);
        }
        callback.onSuccess(unsentSurveys);
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
    public Survey save(Survey survey) {
        SurveyDB surveyDB = SurveyDB.findById(survey.getId());
        if (surveyDB == null) {
            surveyDB = new SurveyDB(OrgUnitDB.findByUID(survey.getOrganisationUnit().getUid()),
                    ProgramDB.getProgram(survey.getProgram().getId()),
                    UserDB.findByUID(survey.getUserAccount().getUserUid()), survey.getType());
            surveyDB.save();
        }
        surveyDB.setStatus(survey.getStatus());
        surveyDB.update();
        survey.setId(surveyDB.getId_survey());
        return survey;
    }
}
