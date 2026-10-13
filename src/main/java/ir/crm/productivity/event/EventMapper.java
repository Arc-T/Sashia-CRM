package ir.crm.productivity.event;

import ir.crm.identity.user.User;
import ir.crm.productivity.event.dto.EventCreateRequest;
import ir.crm.productivity.event.dto.EventResponse;
import ir.crm.productivity.event.dto.EventUpdateRequest;
import ir.crm.sales.contact.Contact;
import ir.crm.shared.web.LookupResponse;
import java.util.List;

public final class EventMapper {

    private EventMapper() {}

    public static Event toEntity(EventCreateRequest r, Contact contact, User user) {
        Event e = new Event();
        e.setTitle(r.title().trim());
        e.setType(r.type());
        e.setStatus(r.status());
        e.setStartDate(r.startDate());
        e.setLocation(trimOrNull(r.location()));
        e.setContact(contact);
        e.setAssignedTo(user);
        return e;
    }

    public static void updateEntity(Event e, EventUpdateRequest r, Contact contact, User user) {
        e.setTitle(r.title().trim());
        e.setType(r.type());
        e.setStatus(r.status());
        e.setStartDate(r.startDate());
        e.setLocation(trimOrNull(r.location()));
        e.setContact(contact);
        e.setAssignedTo(user);
    }

    public static EventResponse toResponse(Event e) {
        if (e == null) return null;
        LookupResponse contactLookup = e.getContact() != null
                ? new LookupResponse(String.valueOf(e.getContact().getId()), e.getContact().getName())
                : null;
        LookupResponse userLookup = e.getAssignedTo() != null
                ? new LookupResponse(String.valueOf(e.getAssignedTo().getId()), e.getAssignedTo().getName())
                : null;
        return new EventResponse(
                String.valueOf(e.getId()),
                e.getTitle(),
                e.getType(),
                e.getStatus(),
                e.getStartDate(),
                e.getLocation(),
                contactLookup,
                userLookup,
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    public static List<EventResponse> toResponses(List<Event> entities) {
        return entities.stream().map(EventMapper::toResponse).toList();
    }

    private static String trimOrNull(String s) {
        return (s != null && !s.isBlank()) ? s.trim() : null;
    }
}
