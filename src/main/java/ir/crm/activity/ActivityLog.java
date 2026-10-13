package ir.crm.activity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "activity_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "module_key", nullable = false, length = 50)
    private String moduleKey;

    @Column(name = "record_id", nullable = false)
    private Long recordId;

    @Column(name = "kind", nullable = false, length = 20)
    private String kind;

    @Column(name = "text", nullable = false, length = 500)
    private String text;

    @Column(name = "actor", nullable = false, length = 100)
    private String actor;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    ActivityLog(String moduleKey, Long recordId, String kind, String text, String actor, Instant createdAt) {
        this(null, moduleKey, recordId, kind, text, actor, createdAt);
    }
}
