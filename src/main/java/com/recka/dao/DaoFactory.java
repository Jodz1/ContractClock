package com.recka.dao;

import com.recka.dao.impl.*;

/** Factory pattern: central creation point for DAO objects. */
public class DaoFactory {
    private final ClientDao clientDao = new JdbcClientDao();
    private final ContractDao contractDao = new JdbcContractDao();
    private final RateDao rateDao = new JdbcRateDao();
    private final WorkSessionDao workSessionDao = new JdbcWorkSessionDao();
    private final PaymentDao paymentDao = new JdbcPaymentDao();
    private final ActivityTagDao activityTagDao = new JdbcActivityTagDao();
    private final SettingsDao settingsDao = new JdbcSettingsDao();

    public ClientDao clients() { return clientDao; }
    public ContractDao contracts() { return contractDao; }
    public RateDao rates() { return rateDao; }
    public WorkSessionDao sessions() { return workSessionDao; }
    public PaymentDao payments() { return paymentDao; }
    public ActivityTagDao activityTags() { return activityTagDao; }
    public SettingsDao settings() { return settingsDao; }
}
