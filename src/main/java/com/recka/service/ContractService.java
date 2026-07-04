package com.recka.service;

import com.recka.dao.*;
import com.recka.event.*;
import com.recka.model.*;
import com.recka.util.ValidationException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ContractService {
    private final ClientDao clientDao;
    private final ContractDao contractDao;
    private final RateDao rateDao;
    private final AppEventBus events = AppEventBus.getInstance();

    public ContractService(DaoFactory factory) {
        this.clientDao = factory.clients();
        this.contractDao = factory.contracts();
        this.rateDao = factory.rates();
    }

    public List<Client> clients() throws SQLException { return clientDao.findAll(); }
    public List<DashboardContractRow> dashboard(String search) throws SQLException { return contractDao.findDashboardRows(search); }
    public Map<String, BigDecimal> earnedTotals(List<Long> contractIds, LocalDate fromDate, LocalDate toDate) throws SQLException { return contractDao.calculateEarnedTotals(contractIds, fromDate, toDate); }
    public Contract findContract(Long id) throws SQLException { return contractDao.findById(id).orElseThrow(() -> new ValidationException("Contract not found.")); }

    public Contract createContract(String clientName, String clientEmail, String company, String title, String description, BigDecimal rate, String currency) throws SQLException {
        validateContract(clientName, title, rate);
        Client client = new Client();
        client.setName(clientName.trim());
        client.setEmail(clientEmail == null || clientEmail.isBlank() ? null : clientEmail.trim());
        client.setCompanyName(company == null || company.isBlank() ? null : company.trim());
        client = clientDao.save(client);

        Contract contract = new Contract();
        contract.setClientId(client.getId());
        contract.setTitle(title.trim());
        contract.setDescription(description);
        contract.setStatus(ContractStatus.ACTIVE);
        contract.setCurrentHourlyRate(rate);
        contract.setCurrencyCode(currency == null || currency.isBlank() ? "USD" : currency.toUpperCase());
        contract = contractDao.save(contract);

        ContractRate cr = new ContractRate();
        cr.setContractId(contract.getId());
        cr.setHourlyRate(rate);
        cr.setCurrencyCode(contract.getCurrencyCode());
        cr.setValidFrom(LocalDateTime.now());
        cr.setNote("Initial rate");
        rateDao.save(cr);
        events.publish(new ContractUpdatedEvent(contract.getId()));
        return contract;
    }

    public void updateContract(Contract contract) throws SQLException {
        if (contract.getTitle() == null || contract.getTitle().isBlank()) throw new ValidationException("Contract title is required.");
        contractDao.update(contract);
        events.publish(new ContractUpdatedEvent(contract.getId()));
    }

    public void archive(Long contractId) throws SQLException {
        contractDao.updateStatus(contractId, ContractStatus.ARCHIVED);
        events.publish(new ContractUpdatedEvent(contractId));
    }

    public void changeRate(Long contractId, BigDecimal newRate, String currency, String note) throws SQLException {
        if (newRate == null || newRate.compareTo(BigDecimal.ZERO) < 0) throw new ValidationException("Rate must be 0 or higher.");
        LocalDateTime now = LocalDateTime.now();
        rateDao.closeActiveRate(contractId, now);
        ContractRate cr = new ContractRate();
        cr.setContractId(contractId);
        cr.setHourlyRate(newRate);
        cr.setCurrencyCode(currency == null || currency.isBlank() ? "USD" : currency.toUpperCase());
        cr.setValidFrom(now);
        cr.setNote(note);
        rateDao.save(cr);
        contractDao.updateCurrentRate(contractId, newRate, cr.getCurrencyCode());
        events.publish(new RateChangedEvent(contractId));
        events.publish(new ContractUpdatedEvent(contractId));
    }

    private void validateContract(String clientName, String title, BigDecimal rate) {
        if (clientName == null || clientName.isBlank()) throw new ValidationException("Client name is required.");
        if (title == null || title.isBlank()) throw new ValidationException("Contract title is required.");
        if (rate == null || rate.compareTo(BigDecimal.ZERO) < 0) throw new ValidationException("Rate must be 0 or higher.");
    }
}
