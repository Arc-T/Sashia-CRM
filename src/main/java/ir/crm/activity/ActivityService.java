package ir.crm.activity;

import ir.crm.shared.exception.BusinessException;
import ir.crm.shared.exception.ErrorCode;
import ir.crm.shared.security.ModuleCatalog;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private static final int LIMIT = 50;

    private final ActivityLogRepository repository;

    @Transactional(readOnly = true)
    List<ActivityResponse> timeline(String moduleKey, Long recordId) {
        if (!ModuleCatalog.KEYS.contains(moduleKey)) throw new BusinessException(ErrorCode.ROUTE_NOT_FOUND);
        return repository.findByModuleKeyAndRecordIdOrderByCreatedAtDescIdDesc(moduleKey, recordId, PageRequest.of(0, LIMIT)).stream()
                .map(a -> new ActivityResponse(String.valueOf(a.getId()), a.getKind(), a.getText(), a.getActor(), a.getCreatedAt()))
                .toList();
    }
}
