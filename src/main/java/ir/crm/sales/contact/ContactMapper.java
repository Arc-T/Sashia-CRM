package ir.crm.sales.contact;

import ir.crm.identity.user.User;
import ir.crm.sales.contact.dto.ContactCreateRequest;
import ir.crm.sales.contact.dto.ContactResponse;
import ir.crm.sales.contact.dto.ContactUpdateRequest;
import ir.crm.sales.organization.Organization;
import ir.crm.shared.web.LookupResponse;
import java.util.List;

public final class ContactMapper {

    private ContactMapper() {}

    public static Contact toEntity(ContactCreateRequest r, Organization org, User user) {
        Contact e = new Contact();
        e.setName(r.name().trim());
        e.setOrganization(org);
        e.setTitle(trimOrNull(r.title()));
        e.setPhone(r.phone().trim());
        e.setEmail(trimOrNull(r.email()));
        e.setCity(trimOrNull(r.city()));
        e.setDoNotCall(Boolean.TRUE.equals(r.doNotCall()));
        e.setAssignedTo(user);
        e.setDescription(trimOrNull(r.description()));
        return e;
    }

    public static void updateEntity(Contact e, ContactUpdateRequest r, Organization org, User user) {
        e.setName(r.name().trim());
        e.setOrganization(org);
        e.setTitle(trimOrNull(r.title()));
        e.setPhone(r.phone().trim());
        e.setEmail(trimOrNull(r.email()));
        e.setCity(trimOrNull(r.city()));
        e.setDoNotCall(Boolean.TRUE.equals(r.doNotCall()));
        e.setAssignedTo(user);
        e.setDescription(trimOrNull(r.description()));
    }

    public static ContactResponse toResponse(Contact e) {
        if (e == null) return null;
        LookupResponse orgLookup = e.getOrganization() != null
                ? new LookupResponse(String.valueOf(e.getOrganization().getId()), e.getOrganization().getName())
                : null;
        LookupResponse userLookup = e.getAssignedTo() != null
                ? new LookupResponse(String.valueOf(e.getAssignedTo().getId()), e.getAssignedTo().getName())
                : null;
        return new ContactResponse(
                String.valueOf(e.getId()),
                e.getName(),
                orgLookup,
                e.getTitle(),
                e.getPhone(),
                e.getEmail(),
                e.getCity(),
                e.isDoNotCall(),
                userLookup,
                e.getDescription(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    public static List<ContactResponse> toResponses(List<Contact> entities) {
        return entities.stream().map(ContactMapper::toResponse).toList();
    }

    private static String trimOrNull(String s) {
        return (s != null && !s.isBlank()) ? s.trim() : null;
    }
}
