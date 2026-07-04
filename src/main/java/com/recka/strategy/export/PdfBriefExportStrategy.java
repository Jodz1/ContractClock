package com.recka.strategy.export;

import com.recka.model.Contract;
import com.recka.model.WorkSession;
import com.recka.util.DateTimeUtil;
import com.recka.util.FileNameUtil;
import com.recka.util.MoneyUtil;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import java.io.*;
import java.time.LocalDate;
import java.util.List;

public class PdfBriefExportStrategy implements BriefExportStrategy {
    @Override
    public File exportSingle(WorkSession session, Contract contract, File preferredFile) throws IOException {
        File file = FileNameUtil.uniqueFile(preferredFile);
        try {
            Document doc = new Document(PageSize.A4, 48, 48, 48, 48);
            PdfWriter.getInstance(doc, new FileOutputStream(file));
            doc.open();
            addTitle(doc, "Brief");
            addParagraph(doc, "Date: " + DateTimeUtil.DATE.format(session.getStartTime().toLocalDate()));
            addParagraph(doc, "Contract: " + contract.getTitle());
            addParagraph(doc, "Client: " + contract.getClientName());
            addParagraph(doc, "Time: " + DateTimeUtil.TIME.format(session.getStartTime()) + " - " + DateTimeUtil.TIME.format(session.getEndTime()));
            addParagraph(doc, "Duration: " + DateTimeUtil.formatDuration(session.getDurationSeconds()));
            addParagraph(doc, "Hourly Rate: " + MoneyUtil.format(session.getHourlyRateSnapshot(), session.getCurrencyCode()) + "/h");
            addParagraph(doc, "Amount: " + MoneyUtil.format(session.getFinalAmount(), session.getCurrencyCode()));
            addSubtitle(doc, "Brief");
            addParagraph(doc, emptyBrief(session));
            doc.close();
            return file;
        } catch (DocumentException e) {
            throw new IOException("PDF export failed", e);
        }
    }

    @Override
    public File exportCombined(Contract contract, LocalDate from, LocalDate to, List<WorkSession> sessions, File preferredFile) throws IOException {
        File file = FileNameUtil.uniqueFile(preferredFile);
        try {
            Document doc = new Document(PageSize.A4, 48, 48, 48, 48);
            PdfWriter.getInstance(doc, new FileOutputStream(file));
            doc.open();
            addTitle(doc, "Combined Brief");
            addParagraph(doc, "Contract: " + contract.getTitle());
            addParagraph(doc, "Client: " + contract.getClientName());
            addParagraph(doc, "Period: " + DateTimeUtil.DATE.format(from) + " - " + DateTimeUtil.DATE.format(to));
            doc.add(new Paragraph(" "));
            for (WorkSession s : sessions) {
                addSubtitle(doc, DateTimeUtil.DATE.format(s.getStartTime().toLocalDate()) + " · " + DateTimeUtil.TIME.format(s.getStartTime()) + " - " + DateTimeUtil.TIME.format(s.getEndTime()));
                addParagraph(doc, "Duration: " + DateTimeUtil.formatDuration(s.getDurationSeconds()));
                addParagraph(doc, "Amount: " + MoneyUtil.format(s.getFinalAmount(), s.getCurrencyCode()));
                addParagraph(doc, emptyBrief(s));
                doc.add(new Paragraph(" "));
            }
            doc.close();
            return file;
        } catch (DocumentException e) {
            throw new IOException("PDF export failed", e);
        }
    }

    private void addTitle(Document doc, String text) throws DocumentException {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
        Paragraph p = new Paragraph(text, font);
        p.setSpacingAfter(14);
        doc.add(p);
    }

    private void addSubtitle(Document doc, String text) throws DocumentException {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
        Paragraph p = new Paragraph(text, font);
        p.setSpacingBefore(8);
        p.setSpacingAfter(5);
        doc.add(p);
    }

    private void addParagraph(Document doc, String text) throws DocumentException {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 11);
        doc.add(new Paragraph(text, font));
    }

    private String emptyBrief(WorkSession s) {
        return s.getDescription() == null || s.getDescription().isBlank() ? "No brief added" : s.getDescription();
    }

    @Override
    public String extension() { return "pdf"; }
}
