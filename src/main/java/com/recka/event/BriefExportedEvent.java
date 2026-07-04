package com.recka.event;

import java.io.File;

public class BriefExportedEvent extends AppEvent {
    private final File file;
    public BriefExportedEvent(File file) { this.file = file; }
    public File getFile() { return file; }
}
