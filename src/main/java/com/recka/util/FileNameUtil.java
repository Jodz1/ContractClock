package com.recka.util;

import java.io.File;
import java.time.LocalDate;

public final class FileNameUtil {
    private FileNameUtil() {}

    public static String singleBriefName(LocalDate date, String extension) {
        return DateTimeUtil.FILE_DATE.format(date) + "-brief." + extension;
    }

    public static String combinedBriefName(LocalDate from, LocalDate to, String extension) {
        if (from.getYear() == to.getYear() && from.getMonth() == to.getMonth()) {
            return String.format("%02d-%02d-%02d-%04d-brief.%s",
                    from.getDayOfMonth(), to.getDayOfMonth(), from.getMonthValue(), from.getYear(), extension);
        }
        return DateTimeUtil.FILE_DATE.format(from) + "-to-" + DateTimeUtil.FILE_DATE.format(to) + "-brief." + extension;
    }

    public static File uniqueFile(File preferred) {
        if (!preferred.exists()) return preferred;
        String name = preferred.getName();
        int dot = name.lastIndexOf('.');
        String base = dot > 0 ? name.substring(0, dot) : name;
        String ext = dot > 0 ? name.substring(dot) : "";
        File parent = preferred.getParentFile();
        for (int i = 2; i < 10000; i++) {
            File candidate = new File(parent, base + "-" + i + ext);
            if (!candidate.exists()) return candidate;
        }
        return new File(parent, base + "-copy" + ext);
    }
}
