-- productivity: tables with inline foreign keys and Persian comments
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE tasks (
  id                      BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'شناسه یکتا',
  version                 BIGINT       NOT NULL DEFAULT 0 COMMENT 'نسخه برای همزمانی',
  created_at              DATETIME(6)  NOT NULL COMMENT 'تاریخ ایجاد',
  updated_at              DATETIME(6)  NOT NULL COMMENT 'تاریخ بروزرسانی',
  created_by              VARCHAR(100) NULL COMMENT 'ایجادکننده',
  updated_by              VARCHAR(100) NULL COMMENT 'بروزرسان',
  title                   VARCHAR(255) NOT NULL COMMENT 'عنوان کار یا وظیفه',
  status                  VARCHAR(32)  NOT NULL COMMENT 'وضعیت کار',
  priority                VARCHAR(32)  NULL COMMENT 'اولویت کار',
  due_date                DATE         NOT NULL COMMENT 'موعد سررسید',
  opportunity_id          BIGINT       NULL COMMENT 'شناسه فرصت فروش مرتبط',
  support_case_id         BIGINT       NULL COMMENT 'شناسه تیکت پشتیبانی مرتبط',
  assigned_to_id          BIGINT       NULL COMMENT 'شناسه کارشناس ارجاع‌شونده',
  description             VARCHAR(2000) NULL COMMENT 'توضیحات و شرح کار',
  PRIMARY KEY (id),
  KEY idx_tasks_status (status),
  KEY idx_tasks_opportunity_id (opportunity_id),
  KEY idx_tasks_support_case_id (support_case_id),
  KEY idx_tasks_assigned_to_id (assigned_to_id),
  CONSTRAINT fk_tasks_opportunity FOREIGN KEY (opportunity_id) REFERENCES opportunities (id) ON DELETE SET NULL,
  CONSTRAINT fk_tasks_support_case FOREIGN KEY (support_case_id) REFERENCES cases (id) ON DELETE SET NULL,
  CONSTRAINT fk_tasks_assigned_to FOREIGN KEY (assigned_to_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='وظایف و کارهای روزانه';

CREATE TABLE events (
  id                      BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'شناسه یکتا',
  version                 BIGINT       NOT NULL DEFAULT 0 COMMENT 'نسخه برای همزمانی',
  created_at              DATETIME(6)  NOT NULL COMMENT 'تاریخ ایجاد',
  updated_at              DATETIME(6)  NOT NULL COMMENT 'تاریخ بروزرسانی',
  created_by              VARCHAR(100) NULL COMMENT 'ایجادکننده',
  updated_by              VARCHAR(100) NULL COMMENT 'بروزرسان',
  title                   VARCHAR(255) NOT NULL COMMENT 'عنوان رویداد یا جلسه',
  type                    VARCHAR(32)  NULL COMMENT 'نوع رویداد (جلسه، تماس و ...)',
  status                  VARCHAR(32)  NULL COMMENT 'وضعیت برگزاری جلسه',
  start_date              DATE         NOT NULL COMMENT 'تاریخ برگزاری',
  location                VARCHAR(255) NULL COMMENT 'محل یا لینک جلسه',
  contact_id              BIGINT       NULL COMMENT 'شناسه مخاطب طرف جلسه',
  assigned_to_id          BIGINT       NULL COMMENT 'شناسه کاربر برگزارکننده',
  PRIMARY KEY (id),
  KEY idx_events_status (status),
  KEY idx_events_contact_id (contact_id),
  KEY idx_events_assigned_to_id (assigned_to_id),
  CONSTRAINT fk_events_contact FOREIGN KEY (contact_id) REFERENCES contacts (id) ON DELETE SET NULL,
  CONSTRAINT fk_events_assigned_to FOREIGN KEY (assigned_to_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='رویدادها و جلسات کاری';

CREATE TABLE emails (
  id                      BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'شناسه یکتا',
  version                 BIGINT       NOT NULL DEFAULT 0 COMMENT 'نسخه برای همزمانی',
  created_at              DATETIME(6)  NOT NULL COMMENT 'تاریخ ایجاد',
  updated_at              DATETIME(6)  NOT NULL COMMENT 'تاریخ بروزرسانی',
  created_by              VARCHAR(100) NULL COMMENT 'ایجادکننده',
  updated_by              VARCHAR(100) NULL COMMENT 'بروزرسان',
  subject                 VARCHAR(255) NOT NULL COMMENT 'موضوع ایمیل',
  folder                  VARCHAR(32)  NULL COMMENT 'پوشه ایمیل (صندوق ورودی، ارسالی و ...)',
  from_address            VARCHAR(255) NULL COMMENT 'فرستنده ایمیل',
  to_address              VARCHAR(255) NOT NULL COMMENT 'گیرنده اصلی ایمیل',
  contact_id              BIGINT       NULL COMMENT 'شناسه مخاطب مرتبط',
  sent_date               DATE         NULL COMMENT 'تاریخ ارسال',
  body                    VARCHAR(2000) NULL COMMENT 'متن پیام ایمیل',
  PRIMARY KEY (id),
  KEY idx_emails_folder (folder),
  KEY idx_emails_contact_id (contact_id),
  CONSTRAINT fk_emails_contact FOREIGN KEY (contact_id) REFERENCES contacts (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='مکاتبات ایمیلی';

CREATE TABLE documents (
  id                      BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'شناسه یکتا',
  version                 BIGINT       NOT NULL DEFAULT 0 COMMENT 'نسخه برای همزمانی',
  created_at              DATETIME(6)  NOT NULL COMMENT 'تاریخ ایجاد',
  updated_at              DATETIME(6)  NOT NULL COMMENT 'تاریخ بروزرسانی',
  created_by              VARCHAR(100) NULL COMMENT 'ایجادکننده',
  updated_by              VARCHAR(100) NULL COMMENT 'بروزرسان',
  title                   VARCHAR(255) NOT NULL COMMENT 'عنوان سند یا فایل',
  type                    VARCHAR(32)  NULL COMMENT 'پسوند یا نوع فایل سند',
  organization_id         BIGINT       NULL COMMENT 'شناسه سازمان مرتبط با سند',
  size_kb                 INT          NULL COMMENT 'حجم فایل به کیلوبایت',
  assigned_to_id          BIGINT       NULL COMMENT 'شناسه کاربر بارگذارنده سند',
  uploaded_at             DATE         NULL COMMENT 'تاریخ آپلود',
  PRIMARY KEY (id),
  KEY idx_documents_organization_id (organization_id),
  KEY idx_documents_assigned_to_id (assigned_to_id),
  CONSTRAINT fk_documents_organization FOREIGN KEY (organization_id) REFERENCES organizations (id) ON DELETE SET NULL,
  CONSTRAINT fk_documents_assigned_to FOREIGN KEY (assigned_to_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='اسناد و پرونده‌های بایگانی';

SET FOREIGN_KEY_CHECKS = 1;
