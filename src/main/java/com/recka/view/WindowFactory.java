package com.recka.view;

import com.recka.AppContext;
import com.recka.model.DashboardContractRow;
import com.recka.model.WorkSession;
import javafx.stage.Stage;
import java.util.Optional;

/** Factory pattern: creates application windows in one place. */
public class WindowFactory {
    private final AppContext context;

    public WindowFactory(AppContext context) {
        this.context = context;
    }

    public void openTracker(Stage owner, Optional<DashboardContractRow> selected) {
        new TrackerWindow(context, owner, selected).show();
    }

    public void openSessionDetails(Stage owner, WorkSession session) {
        new SessionDetailsWindow(context, owner, session).show();
    }
}
