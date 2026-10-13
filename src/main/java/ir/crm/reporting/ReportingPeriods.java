package ir.crm.reporting;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Locale;

/** Date windows and number formatting shared by reporting services. */
final class ReportingPeriods {

    static final ZoneId TEHRAN = ZoneId.of("Asia/Tehran");
    private static final Locale FA = Locale.forLanguageTag("fa-IR");

    private ReportingPeriods() {}

    static LocalDate today() { return LocalDate.now(TEHRAN); }

    static YearMonth currentMonth() { return YearMonth.now(TEHRAN); }

    /** Start of a calendar day as the UTC LocalDateTime that the DATETIME(6) columns hold. */
    static LocalDateTime utc(LocalDate day) {
        return day.atStartOfDay(TEHRAN).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    static YearMonth monthOf(LocalDate day) { return YearMonth.from(day); }

    static String monthLabel(YearMonth month) {
        return month.getMonth().name().substring(0, 3) + " " + month.getYear();
    }

    static String monthYearLabel(YearMonth month) {
        return month.getMonth().name() + " " + month.getYear();
    }

    static String number(double value) { return NumberFormat.getNumberInstance(FA).format(value); }

    static String compact(double amount) {
        if (Math.abs(amount) >= 1e9) return NumberFormat.getNumberInstance(FA).format(Math.round(amount / 1e8) / 10.0) + " میلیارد";
        if (Math.abs(amount) >= 1e6) return NumberFormat.getNumberInstance(FA).format(Math.round(amount / 1e6)) + " میلیون";
        return NumberFormat.getNumberInstance(FA).format(Math.round(amount));
    }

    static double pctChange(double current, double previous) {
        if (previous == 0) return current == 0 ? 0 : 100;
        return Math.round((current - previous) / previous * 1000) / 10.0;
    }

    static double round2(double v) { return Math.round(v * 100) / 100.0; }
}
