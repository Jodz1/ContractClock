package com.recka.command;

import com.recka.service.PaymentService;
import java.math.BigDecimal;
import java.time.LocalDate;

public class AddPaymentCommand implements Command {
    private final PaymentService service;
    private final Long contractId;
    private final BigDecimal amount;
    private final LocalDate date;
    private final String note;
    private final String currency;
    public AddPaymentCommand(PaymentService service, Long contractId, BigDecimal amount, LocalDate date, String note, String currency) {
        this.service = service;
        this.contractId = contractId;
        this.amount = amount;
        this.date = date;
        this.note = note;
        this.currency = currency;
    }
    public void execute() throws Exception { service.addPayment(contractId, amount, date, note, currency); }
}
