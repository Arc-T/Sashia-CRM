-- support: tables with inline foreign keys and Persian comments
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE cases (
  id                      BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'شناسه یکتا',
  version                 BIGINT       NOT NULL DEFAULT 0 COMMENT 'نسخه برای همزمانی',
  created_at              DATETIME(6)  NOT NULL COMMENT 'تاریخ ایجاد',
  updated_at              DATETIME(6)  NOT NULL COMMENT 'تاریخ بروزرسانی',
  created_by              VARCHAR(100) NULL COMMENT 'ایجادکننده',
  updated_by              VARCHAR(100) NULL COMMENT 'بروزرسان',
  subject                 VARCHAR(255) NOT NULL COMMENT 'موضوع درخواست پشتیبانی',
  organization_id         BIGINT       NULL COMMENT 'شناسه سازمان درخواست‌دهنده',
  contact_id              BIGINT       NULL COMMENT 'شناسه فرد تماس‌گیرنده',
  status                  VARCHAR(32)  NOT NULL COMMENT 'وضعیت تیکت پشتیبانی',
  priority                VARCHAR(32)  NOT NULL COMMENT 'اولویت رسیدگی',
  number                  VARCHAR(255) NULL COMMENT 'شماره پیگیری تیکت',
  assigned_to_id          BIGINT       NULL COMMENT 'شناسه کارشناس پشتیبان',
  description             VARCHAR(2000) NULL COMMENT 'شرح مشکل یا درخواست',
  PRIMARY KEY (id),
  KEY idx_cases_organization_id (organization_id),
  KEY idx_cases_contact_id (contact_id),
  KEY idx_cases_status (status),
  KEY idx_cases_assigned_to_id (assigned_to_id),
  CONSTRAINT fk_cases_organization FOREIGN KEY (organization_id) REFERENCES organizations (id) ON DELETE SET NULL,
  CONSTRAINT fk_cases_contact FOREIGN KEY (contact_id) REFERENCES contacts (id) ON DELETE SET NULL,
  CONSTRAINT fk_cases_assigned_to FOREIGN KEY (assigned_to_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='درخواست‌ها و تیکت‌های پشتیبانی';

CREATE TABLE faq (
  id                      BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'شناسه یکتا',
  version                 BIGINT       NOT NULL DEFAULT 0 COMMENT 'نسخه برای همزمانی',
  created_at              DATETIME(6)  NOT NULL COMMENT 'تاریخ ایجاد',
  updated_at              DATETIME(6)  NOT NULL COMMENT 'تاریخ بروزرسانی',
  created_by              VARCHAR(100) NULL COMMENT 'ایجادکننده',
  updated_by              VARCHAR(100) NULL COMMENT 'بروزرسان',
  question                VARCHAR(255) NOT NULL COMMENT 'پرسش متداول',
  category                VARCHAR(32)  NULL COMMENT 'دسته‌بندی پرسش',
  status                  VARCHAR(32)  NULL COMMENT 'وضعیت انتشار یا پیش‌نویس',
  answer                  VARCHAR(2000) NOT NULL COMMENT 'پاسخ پرسش متداول',
  PRIMARY KEY (id),
  KEY idx_faq_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='پرسش‌ها و پاسخ‌های متداول';

SET FOREIGN_KEY_CHECKS = 1;
