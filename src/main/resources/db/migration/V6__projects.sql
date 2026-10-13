-- projects: tables with inline foreign keys and Persian comments
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE projects (
  id                      BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'شناسه یکتا',
  version                 BIGINT       NOT NULL DEFAULT 0 COMMENT 'نسخه برای همزمانی',
  created_at              DATETIME(6)  NOT NULL COMMENT 'تاریخ ایجاد',
  updated_at              DATETIME(6)  NOT NULL COMMENT 'تاریخ بروزرسانی',
  created_by              VARCHAR(100) NULL COMMENT 'ایجادکننده',
  updated_by              VARCHAR(100) NULL COMMENT 'بروزرسان',
  name                    VARCHAR(255) NOT NULL COMMENT 'عنوان پروژه',
  organization_id         BIGINT       NULL COMMENT 'شناسه سازمان کارفرما',
  status                  VARCHAR(32)  NULL COMMENT 'وضعیت پروژه',
  progress                INT          NULL COMMENT 'درصد پیشرفت کار',
  priority                VARCHAR(32)  NULL COMMENT 'اولویت اجرای پروژه',
  start_date              DATE         NULL COMMENT 'تاریخ شروع پروژه',
  end_date                DATE         NULL COMMENT 'تاریخ تحویل نهایی',
  budget                  DECIMAL(19,2) NULL COMMENT 'بودجه تخصیص‌یافته',
  assigned_to_id          BIGINT       NULL COMMENT 'شناسه مدیر پروژه',
  PRIMARY KEY (id),
  KEY idx_projects_organization_id (organization_id),
  KEY idx_projects_status (status),
  KEY idx_projects_assigned_to_id (assigned_to_id),
  CONSTRAINT fk_projects_organization FOREIGN KEY (organization_id) REFERENCES organizations (id) ON DELETE SET NULL,
  CONSTRAINT fk_projects_assigned_to FOREIGN KEY (assigned_to_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='پروژه‌ها';

CREATE TABLE project_tasks (
  id                      BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'شناسه یکتا',
  version                 BIGINT       NOT NULL DEFAULT 0 COMMENT 'نسخه برای همزمانی',
  created_at              DATETIME(6)  NOT NULL COMMENT 'تاریخ ایجاد',
  updated_at              DATETIME(6)  NOT NULL COMMENT 'تاریخ بروزرسانی',
  created_by              VARCHAR(100) NULL COMMENT 'ایجادکننده',
  updated_by              VARCHAR(100) NULL COMMENT 'بروزرسان',
  name                    VARCHAR(255) NOT NULL COMMENT 'عنوان فعالیت پروژه',
  project_id              BIGINT       NOT NULL COMMENT 'شناسه پروژه مرتبط',
  status                  VARCHAR(32)  NULL COMMENT 'وضعیت فعالیت',
  priority                VARCHAR(32)  NULL COMMENT 'اولویت فعالیت',
  hours                   INT          NULL COMMENT 'ساعت کار برآوردی',
  due_date                DATE         NULL COMMENT 'موعد تحویل فعالیت',
  assigned_to_id          BIGINT       NULL COMMENT 'شناسه کارشناس مسئول وظیفه',
  PRIMARY KEY (id),
  KEY idx_project_tasks_project_id (project_id),
  KEY idx_project_tasks_status (status),
  KEY idx_project_tasks_assigned_to_id (assigned_to_id),
  CONSTRAINT fk_project_tasks_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
  CONSTRAINT fk_project_tasks_assigned_to FOREIGN KEY (assigned_to_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='فعالیت‌ها و وظایف پروژه‌ها';

SET FOREIGN_KEY_CHECKS = 1;
