package ir.crm.productivity.email;

import ir.crm.productivity.email.dto.EmailCreateRequest;
import ir.crm.productivity.email.dto.EmailResponse;
import ir.crm.productivity.email.dto.EmailUpdateRequest;
import ir.crm.sales.contact.Contact;
import ir.crm.shared.web.LookupResponse;
import java.util.List;

public final class EmailMapper {

    private EmailMapper() {}

    public static Email toEntity(EmailCreateRequest r, Contact contact) {
        Email e = new Email();
        e.setSubject(r.subject().trim());
        e.setFolder(r.folder());
        e.setFromAddress(trimOrNull(r.fromAddress()));
        e.setToAddress(r.toAddress().trim());
        e.setContact(contact);
        e.setSentDate(r.sentDate());
        e.setBody(trimOrNull(r.body()));
        return e;
    }

    public static void updateEntity(Email e, EmailUpdateRequest r, Contact contact) {
        e.setSubject(r.subject().trim());
        e.setFolder(r.folder());
        e.setFromAddress(trimOrNull(r.fromAddress()));
        e.setToAddress(r.toAddress().trim());
        e.setContact(contact);
        e.setSentDate(r.sentDate());
        e.setBody(trimOrNull(r.body()));
    }

    public static EmailResponse toResponse(Email e) {
        if (e == null) return null;
        LookupResponse contactLookup = e.getContact() != null
                ? new LookupResponse(String.valueOf(e.getContact().getId()), e.getContact().getName())
                : null;
        return new EmailResponse(
                String.valueOf(e.getId()),
                e.getSubject(),
                e.getFolder(),
                e.getFromAddress(),
                e.getToAddress(),
                contactLookup,
                e.getSentDate(),
                e.getBody(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    public static List<EmailResponse> toResponses(List<Email> entities) {
        return entities.stream().map(EmailMapper::toResponse).toList();
    }

    private static String trimOrNull(String s) {
        return (s != null && !s.isBlank()) ? s.trim() : null;
    }
}
