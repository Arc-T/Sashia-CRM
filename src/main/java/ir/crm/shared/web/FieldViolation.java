package ir.crm.shared.web;

/** One invalid input field: technical name, Persian label and Persian message. */
public record FieldViolation(String field, String label, String message) {}
