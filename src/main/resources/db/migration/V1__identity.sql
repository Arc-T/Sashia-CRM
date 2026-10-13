-- Identity & access: roles, teams, users, role permissions.
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE roles (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'شناسه یکتا',
  version     BIGINT       NOT NULL DEFAULT 0 COMMENT 'نسخه برای همزمانی',
  created_at  DATETIME(6)  NOT NULL COMMENT 'تاریخ ایجاد',
  updated_at  DATETIME(6)  NOT NULL COMMENT 'تاریخ بروزرسانی',
  created_by  VARCHAR(100) NULL COMMENT 'ایجادکننده',
  updated_by  VARCHAR(100) NULL COMMENT 'بروزرسان',
  name        VARCHAR(100) NOT NULL COMMENT 'نام نقش',
  kind        VARCHAR(32)  NOT NULL COMMENT 'نوع نقش',
  description VARCHAR(2000) NULL COMMENT 'توضیحات نقش',
  PRIMARY KEY (id),
  UNIQUE KEY uk_roles_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='نقش‌های کاربری';

CREATE TABLE role_permissions (
  role_id    BIGINT       NOT NULL COMMENT 'شناسه نقش',
  permission VARCHAR(100) NOT NULL COMMENT 'کد دسترسی',
  PRIMARY KEY (role_id, permission),
  CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='دسترسی‌های هر نقش';

CREATE TABLE teams (
  id             BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'شناسه یکتا',
  version        BIGINT        NOT NULL DEFAULT 0 COMMENT 'نسخه برای همزمانی',
  created_at     DATETIME(6)   NOT NULL COMMENT 'تاریخ ایجاد',
  updated_at     DATETIME(6)   NOT NULL COMMENT 'تاریخ بروزرسانی',
  created_by     VARCHAR(100)  NULL COMMENT 'ایجادکننده',
  updated_by     VARCHAR(100)  NULL COMMENT 'بروزرسان',
  name           VARCHAR(255)  NOT NULL COMMENT 'نام تیم',
  leader_id      BIGINT        NULL COMMENT 'شناسه سرپرست تیم',
  monthly_target DECIMAL(19,2) NULL COMMENT 'هدف فروش ماهانه',
  description    VARCHAR(2000) NULL COMMENT 'توضیحات تیم',
  PRIMARY KEY (id),
  KEY idx_teams_leader_id (leader_id),
  CONSTRAINT fk_teams_leader FOREIGN KEY (leader_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='تیم‌ها و گروه‌های کاری';

CREATE TABLE users (
  id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'شناسه یکتا',
  version       BIGINT       NOT NULL DEFAULT 0 COMMENT 'نسخه برای همزمانی',
  created_at    DATETIME(6)  NOT NULL COMMENT 'تاریخ ایجاد',
  updated_at    DATETIME(6)  NOT NULL COMMENT 'تاریخ بروزرسانی',
  created_by    VARCHAR(100) NULL COMMENT 'ایجادکننده',
  updated_by    VARCHAR(100) NULL COMMENT 'بروزرسان',
  username      VARCHAR(100) NOT NULL COMMENT 'نام کاربری',
  name          VARCHAR(255) NOT NULL COMMENT 'نام و نام خانوادگی',
  email         VARCHAR(255) NOT NULL COMMENT 'آدرس ایمیل',
  phone         VARCHAR(20)  NULL COMMENT 'شماره تلفن',
  password_hash VARCHAR(100) NOT NULL COMMENT 'هش رمز عبور',
  role_id       BIGINT       NOT NULL COMMENT 'شناسه نقش کاربر',
  team_id       BIGINT       NULL COMMENT 'شناسه تیم کاربر',
  status        VARCHAR(32)  NOT NULL COMMENT 'وضعیت کاربر',
  last_login_at DATETIME(6)  NULL COMMENT 'تاریخ آخرین ورود',
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_username (username),
  UNIQUE KEY uk_users_email (email),
  KEY idx_users_role_id (role_id),
  KEY idx_users_team_id (team_id),
  CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id),
  CONSTRAINT fk_users_team FOREIGN KEY (team_id) REFERENCES teams (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='کاربران سیستم';

INSERT INTO roles (version, created_at, updated_at, created_by, name, kind, description) VALUES
  (0, NOW(6), NOW(6), 'system', 'مدیر سیستم',       'ADMIN',   'دسترسی کامل به همهٔ بخش‌ها'),
  (0, NOW(6), NOW(6), 'system', 'مدیر فروش',        'MANAGER', 'مدیریت تیم فروش و گزارش‌ها'),
  (0, NOW(6), NOW(6), 'system', 'کارشناس فروش',     'SALES',   'ثبت و پیگیری سرنخ‌ها و فرصت‌ها'),
  (0, NOW(6), NOW(6), 'system', 'کارشناس پشتیبانی', 'SUPPORT', 'رسیدگی به درخواست‌های پشتیبانی'),
  (0, NOW(6), NOW(6), 'system', 'مدیر مالی',        'FINANCE', 'فاکتورها و مطالبات');

SET FOREIGN_KEY_CHECKS = 1;
