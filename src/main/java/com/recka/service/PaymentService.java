package com.recka.service;

import com.recka.dao.*;
import com.recka.event.*;
import com.recka.model.Payment;
import com.recka.util.ValidationException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class PaymentService {
    private final PaymentDao paymentDao;
    private final ContractDao contractDao;
    private final AppEventBus events = AppEventBus.getInstance();

    public PaymentService(DaoFactory factory) {
        this.paymentDao = factory.payments();
        this.contractDao = factory.contracts();
    }

    public Payment addPayment(Long contractId, BigDecimal amount, LocalDate date, String note, String currency) throws SQLException {
        if (contractId == null) throw new ValidationException("Contract is required.");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) throw new ValidationException("Payment amount must be positive.");
        Payment p = new Payment();
        p.setContractId(contractId);
        p.setAmount(amount);
        p.setPaymentDate(date == null ? LocalDate.now() : date);
        p.setNote(note);
        p.setCurrencyCode(currency == null || currency.isBlank() ? "USD" : currency);
        p = paymentDao.save(p);
        events.publish(new PaymentAddedEvent(contractId));
        return p;
    }

    public void markBalancePaid(Long contractId, BigDecimal balance, String currency) throws SQLException {
        if (balance == null || balance.compareTo(BigDecimal.ZERO) <= 0) throw new ValidationException("No unpaid balance to mark as paid.");
        addPayment(contractId, balance, LocalDate.now(), "Mark balance as paid", currency);
    }

    public List<Payment> payments(Long contractId) throws SQLException { return paymentDao.findByContract(contractId); }
}
