package ir.crm.reporting;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SeriesPoint(String label, double value, Double target) {

    static SeriesPoint of(String label, double value) { return new SeriesPoint(label, value, null); }
}
