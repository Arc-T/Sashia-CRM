package ir.crm.support.faq;

import ir.crm.support.faq.dto.FaqCreateRequest;
import ir.crm.support.faq.dto.FaqResponse;
import ir.crm.support.faq.dto.FaqUpdateRequest;
import java.util.List;

public final class FaqMapper {

    private FaqMapper() {}

    public static Faq toEntity(FaqCreateRequest r) {
        Faq e = new Faq();
        e.setQuestion(r.question().trim());
        e.setCategory(r.category());
        e.setStatus(r.status());
        e.setAnswer(r.answer().trim());
        return e;
    }

    public static void updateEntity(Faq e, FaqUpdateRequest r) {
        e.setQuestion(r.question().trim());
        e.setCategory(r.category());
        e.setStatus(r.status());
        e.setAnswer(r.answer().trim());
    }

    public static FaqResponse toResponse(Faq e) {
        if (e == null) return null;
        return new FaqResponse(
                String.valueOf(e.getId()),
                e.getQuestion(),
                e.getCategory(),
                e.getStatus(),
                e.getAnswer(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    public static List<FaqResponse> toResponses(List<Faq> entities) {
        return entities.stream().map(FaqMapper::toResponse).toList();
    }
}
