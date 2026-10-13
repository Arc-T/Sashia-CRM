package ir.crm.catalog.pricebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PriceBookRepository extends JpaRepository<PriceBook, Long>, JpaSpecificationExecutor<PriceBook> {}
