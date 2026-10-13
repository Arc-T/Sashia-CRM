-- marketing: tables with inline foreign keys and Persian comments
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE campaigns (
  id                      BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'شناسه یکتا',
  version                 BIGINT       NOT NULL DEFAULT 0 COMMENT 'نسخه برای همزمانی',
  created_at              DATETIME(6)  NOT NULL COMMENT 'تاریخ ایجاد',
  updated_at              DATETIME(6)  NOT NULL COMMENT 'تاریخ بروزرسانی',
  created_by              VARCHAR(100) NULL COMMENT 'ایجادکننده',
  updated_by              VARCHAR(100) NULL COMMENT 'بروزرسان',
  name                    VARCHAR(255) NOT NULL COMMENT 'عنوان کمپین بازاریابی',
  type                    VARCHAR(32)  NULL COMMENT 'نوع کانال کمپین',
  status                  VARCHAR(32)  NOT NULL COMMENT 'وضعیت اجرای کمپین',
  budget                  DECIMAL(19,2) NULL COMMENT 'بودجه مصوب کمپین',
  actual_cost             DECIMAL(19,2) NULL COMMENT 'هزینه واقعی صرف‌شده',
  expected_leads          INT          NULL COMMENT 'تعداد سرنخ مورد انتظار',
  start_date              DATE         NULL COMMENT 'تاریخ شروع اجرای کمپین',
  end_date                DATE         NULL COMMENT 'تاریخ خاتمه کمپین',
  assigned_to_id          BIGINT       NULL COMMENT 'شناسه کارشناس مسئول اجرای کمپین',
  description             VARCHAR(2000) NULL COMMENT 'توضیحات و اهداف بازاریابی',
  PRIMARY KEY (id),
  KEY idx_campaigns_status (status),
  KEY idx_campaigns_assigned_to_id (assigned_to_id),
  CONSTRAINT fk_campaigns_assigned_to FOREIGN KEY (assigned_to_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='کمپین‌های تبلیغاتی و بازاریابی';

SET FOREIGN_KEY_CHECKS = 1;
