package ir.crm.productivity.email;

import ir.crm.sales.contact.Contact;
import ir.crm.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "emails")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Email extends BaseEntity {

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(name = "folder", length = 32)
    private EmailFolder folder;

    @Column(name = "from_address", length = 255)
    private String fromAddress;

    @Column(name = "to_address", nullable = false, length = 255)
    private String toAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id")
    private Contact contact;

    @Column(name = "sent_date")
    private LocalDate sentDate;

    @Column(name = "body", length = 2000)
    private String body;

    // Domain helpers
    public boolean isSent() {
        return this.folder == EmailFolder.SENT;
    }

    public void markSent() {
        this.folder = EmailFolder.SENT;
        this.sentDate = LocalDate.now();
    }

    public Long getContactId() {
        return this.contact != null ? this.contact.getId() : null;
    }
}
