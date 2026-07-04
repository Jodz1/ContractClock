package com.recka.strategy.export;

import com.recka.model.Contract;
import com.recka.model.WorkSession;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/** Strategy pattern: TXT and PDF exports share a common contract. */
public interface BriefExportStrategy {
    File exportSingle(WorkSession session, Contract contract, File preferredFile) throws IOException;
    File exportCombined(Contract contract, LocalDate from, LocalDate to, List<WorkSession> sessions, File preferredFile) throws IOException;
    String extension();
}
