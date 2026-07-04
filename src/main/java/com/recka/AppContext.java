package com.recka;

import com.recka.dao.DaoFactory;
import com.recka.memento.WindowStateManager;
import com.recka.service.*;

public class AppContext {
    private final DaoFactory daoFactory = new DaoFactory();
    private final ContractService contractService = new ContractService(daoFactory);
    private final TimeTrackerService timeTrackerService = new TimeTrackerService(daoFactory);
    private final PaymentService paymentService = new PaymentService(daoFactory);
    private final ExportService exportService = new ExportService(daoFactory, contractService, timeTrackerService);
    private final WindowStateManager windowStateManager = new WindowStateManager(daoFactory.settings());

    public AppContext() {
        SettingsManager.init(daoFactory.settings());
    }

    public DaoFactory daoFactory() { return daoFactory; }
    public ContractService contractService() { return contractService; }
    public TimeTrackerService timeTrackerService() { return timeTrackerService; }
    public PaymentService paymentService() { return paymentService; }
    public ExportService exportService() { return exportService; }
    public WindowStateManager windowStateManager() { return windowStateManager; }
}
