package com.recka.command;

import com.recka.service.ContractService;
import java.math.BigDecimal;

public class ChangeRateCommand implements Command {
    private final ContractService service;
    private final Long contractId;
    private final BigDecimal rate;
    private final String currency;
    private final String note;
    public ChangeRateCommand(ContractService service, Long contractId, BigDecimal rate, String currency, String note) {
        this.service = service;
        this.contractId = contractId;
        this.rate = rate;
        this.currency = currency;
        this.note = note;
    }
    public void execute() throws Exception { service.changeRate(contractId, rate, currency, note); }
}
