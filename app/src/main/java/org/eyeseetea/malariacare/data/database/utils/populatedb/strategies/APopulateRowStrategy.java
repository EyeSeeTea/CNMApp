package org.eyeseetea.malariacare.data.database.utils.populatedb.strategies;

import android.support.annotation.Nullable;

import org.eyeseetea.malariacare.data.database.model.PartnerDB;
import org.eyeseetea.malariacare.data.database.model.StringKeyDB;
import org.eyeseetea.malariacare.data.database.model.TreatmentDB;

import java.util.HashMap;

public class APopulateRowStrategy {

  public TreatmentDB populateTreatments(String[] line, HashMap<Long, PartnerDB> organisationFK,
            HashMap<Long, StringKeyDB> stringKeyList, @Nullable TreatmentDB treatmentDB){
      return null;
  }
}
