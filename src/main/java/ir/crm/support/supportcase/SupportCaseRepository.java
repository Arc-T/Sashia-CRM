package ir.crm.support.supportcase;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SupportCaseRepository extends JpaRepository<SupportCase, Long>, JpaSpecificationExecutor<SupportCase> {}
