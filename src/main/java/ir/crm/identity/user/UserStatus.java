package ir.crm.identity.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum UserStatus {
    ACTIVE("active", "فعال"),
    INACTIVE("inactive", "غیرفعال");

    private final String key;
    private final String label;

    UserStatus(String key, String label) {
        this.key = key;
        this.label = label;
    }

    @JsonValue
    public String key() { return key; }

    public String label() { return label; }

    @JsonCreator
    public static UserStatus fromKey(String key) {
        return Arrays.stream(values()).filter(v -> v.key.equals(key)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown UserStatus: " + key));
    }
}
