package ir.crm.shared.web;

/** Regular expressions shared by request validation (empty string is allowed; use @NotBlank for required). */
public final class Patterns {

    public static final String PHONE = "^$|^(\\+98|0)?9\\d{9}$|^0\\d{10}$";
    public static final String URL = "^$|^https?://\\S+$";

    private Patterns() {}
}
