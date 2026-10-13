package ir.crm.support.faq;

import ir.crm.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "faq")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Faq extends BaseEntity {

    @Column(name = "question", nullable = false, length = 255)
    private String question;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 32)
    private FaqCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32)
    private FaqStatus status;

    @Column(name = "answer", nullable = false, length = 2000)
    private String answer;

    // Domain helpers
    public boolean isPublished() {
        return this.status == FaqStatus.PUBLISHED;
    }

    public void publish() {
        this.status = FaqStatus.PUBLISHED;
    }

    public void draft() {
        this.status = FaqStatus.DRAFT;
    }
}
