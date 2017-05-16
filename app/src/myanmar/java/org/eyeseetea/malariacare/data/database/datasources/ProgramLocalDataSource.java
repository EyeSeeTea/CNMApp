package org.eyeseetea.malariacare.data.database.datasources;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.domain.boundary.repositories.IProgramRepository;
import org.eyeseetea.malariacare.domain.entity.Program;


public class ProgramLocalDataSource implements IProgramRepository {


    @Override
    public void saveUserProgramId(Program program) {

    }

    @Override
    public Long getUserProgramId() {
        org.eyeseetea.malariacare.data.database.model.Program program =
                org.eyeseetea.malariacare.data.database.model.Program.findByUID(
                        PreferencesState.getInstance().getContext().getString(
                                R.string.malariaProgramUID));
        if (program == null) {
            return 0l;
        }
        return program.getId_program();
    }

    @Override
    public String getUserProgramUID() {
        org.eyeseetea.malariacare.data.database.model.Program program =
                org.eyeseetea.malariacare.data.database.model.Program.findByUID(
                        PreferencesState.getInstance().getContext().getString(
                                R.string.malariaProgramUID));
        if (program == null) {
            return "";
        }
        return program.getUid();
    }
}
