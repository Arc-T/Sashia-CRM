package ir.crm.productivity.document;

import ir.crm.identity.user.User;
import ir.crm.productivity.document.dto.DocumentCreateRequest;
import ir.crm.productivity.document.dto.DocumentResponse;
import ir.crm.productivity.document.dto.DocumentUpdateRequest;
import ir.crm.sales.organization.Organization;
import ir.crm.shared.web.LookupResponse;
import java.util.List;

public final class DocumentMapper {

    private DocumentMapper() {}

    public static Document toEntity(DocumentCreateRequest r, Organization org, User user) {
        Document e = new Document();
        e.setTitle(r.title().trim());
        e.setType(r.type());
        e.setOrganization(org);
        e.setSizeKb(r.sizeKb());
        e.setAssignedTo(user);
        e.setUploadedAt(r.uploadedAt());
        return e;
    }

    public static void updateEntity(Document e, DocumentUpdateRequest r, Organization org, User user) {
        e.setTitle(r.title().trim());
        e.setType(r.type());
        e.setOrganization(org);
        e.setSizeKb(r.sizeKb());
        e.setAssignedTo(user);
        e.setUploadedAt(r.uploadedAt());
    }

    public static DocumentResponse toResponse(Document e) {
        if (e == null) return null;
        LookupResponse orgLookup = e.getOrganization() != null
                ? new LookupResponse(String.valueOf(e.getOrganization().getId()), e.getOrganization().getName())
                : null;
        LookupResponse userLookup = e.getAssignedTo() != null
                ? new LookupResponse(String.valueOf(e.getAssignedTo().getId()), e.getAssignedTo().getName())
                : null;
        return new DocumentResponse(
                String.valueOf(e.getId()),
                e.getTitle(),
                e.getType(),
                orgLookup,
                e.getSizeKb(),
                userLookup,
                e.getUploadedAt(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    public static List<DocumentResponse> toResponses(List<Document> entities) {
        return entities.stream().map(DocumentMapper::toResponse).toList();
    }
}
