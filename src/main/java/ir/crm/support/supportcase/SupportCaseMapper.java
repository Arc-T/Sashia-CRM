package ir.crm.support.supportcase;

import ir.crm.identity.user.User;
import ir.crm.sales.contact.Contact;
import ir.crm.sales.organization.Organization;
import ir.crm.shared.web.LookupResponse;
import ir.crm.support.supportcase.dto.SupportCaseCreateRequest;
import ir.crm.support.supportcase.dto.SupportCaseResponse;
import ir.crm.support.supportcase.dto.SupportCaseUpdateRequest;
import java.util.List;

public final class SupportCaseMapper {

    private SupportCaseMapper() {}

    public static SupportCase toEntity(SupportCaseCreateRequest r, Organization org, Contact contact, User user) {
        SupportCase e = new SupportCase();
        e.setSubject(r.subject().trim());
        e.setOrganization(org);
        e.setContact(contact);
        e.setStatus(r.status());
        e.setPriority(r.priority());
        e.setNumber(trimOrNull(r.number()));
        e.setAssignedTo(user);
        e.setDescription(trimOrNull(r.description()));
        return e;
    }

    public static void updateEntity(SupportCase e, SupportCaseUpdateRequest r, Organization org, Contact contact, User user) {
        e.setSubject(r.subject().trim());
        e.setOrganization(org);
        e.setContact(contact);
        e.setStatus(r.status());
        e.setPriority(r.priority());
        e.setNumber(trimOrNull(r.number()));
        e.setAssignedTo(user);
        e.setDescription(trimOrNull(r.description()));
    }

    public static SupportCaseResponse toResponse(SupportCase e) {
        if (e == null) return null;
        LookupResponse orgLookup = e.getOrganization() != null
                ? new LookupResponse(String.valueOf(e.getOrganization().getId()), e.getOrganization().getName())
                : null;
        LookupResponse contactLookup = e.getContact() != null
                ? new LookupResponse(String.valueOf(e.getContact().getId()), e.getContact().getName())
                : null;
        LookupResponse userLookup = e.getAssignedTo() != null
                ? new LookupResponse(String.valueOf(e.getAssignedTo().getId()), e.getAssignedTo().getName())
                : null;
        return new SupportCaseResponse(
                String.valueOf(e.getId()),
                e.getSubject(),
                orgLookup,
                contactLookup,
                e.getStatus(),
                e.getPriority(),
                e.getNumber(),
                userLookup,
                e.getDescription(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    public static List<SupportCaseResponse> toResponses(List<SupportCase> entities) {
        return entities.stream().map(SupportCaseMapper::toResponse).toList();
    }

    private static String trimOrNull(String s) {
        return (s != null && !s.isBlank()) ? s.trim() : null;
    }
}
