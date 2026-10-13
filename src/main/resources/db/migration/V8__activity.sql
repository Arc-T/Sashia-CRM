-- Activity log table
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE activity_log (
  id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'شناسه یکتا',
  module_key VARCHAR(50)  NOT NULL COMMENT 'کلید ماژول مربوطه',
  record_id  BIGINT       NOT NULL COMMENT 'شناسه رکورد ماژول',
  kind       VARCHAR(20)  NOT NULL COMMENT 'نوع فعالیت (ایجاد، ویرایش، حذف)',
  text       VARCHAR(500) NOT NULL COMMENT 'شرح فعالیت',
  actor      VARCHAR(100) NOT NULL COMMENT 'انجام‌دهنده فعالیت',
  created_at DATETIME(6)  NOT NULL COMMENT 'تاریخ و زمان وقوع فعالیت',
  PRIMARY KEY (id),
  KEY idx_activity_record (module_key, record_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='لاگ فعالیت‌ها و تغییرات رکوردها';

SET FOREIGN_KEY_CHECKS = 1;
