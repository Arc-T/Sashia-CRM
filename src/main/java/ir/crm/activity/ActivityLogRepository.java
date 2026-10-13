package ir.crm.activity;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findByModuleKeyAndRecordIdOrderByCreatedAtDescIdDesc(String moduleKey, Long recordId, Pageable pageable);
}
