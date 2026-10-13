package ir.crm.shared.security;

import java.util.Arrays;
import java.util.Optional;

public enum PermissionAction {
    VIEW, CREATE, EDIT, DELETE, EXPORT;

    public String key() {
        return name().toLowerCase();
    }

    public static Optional<PermissionAction> fromKey(String key) {
        return Arrays.stream(values()).filter(a -> a.key().equals(key)).findFirst();
    }
}
