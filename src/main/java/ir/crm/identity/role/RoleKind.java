package ir.crm.identity.role;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum RoleKind {
    ADMIN("admin", "مدیر سیستم"),
    MANAGER("manager", "مدیر"),
    SALES("sales", "فروش"),
    SUPPORT("support", "پشتیبانی"),
    FINANCE("finance", "مالی");

    private final String key;
    private final String label;

    RoleKind(String key, String label) {
        this.key = key;
        this.label = label;
    }

    @JsonValue
    public String key() { return key; }

    public String label() { return label; }

    @JsonCreator
    public static RoleKind fromKey(String key) {
        return Arrays.stream(values()).filter(v -> v.key.equals(key)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown RoleKind: " + key));
    }
}
