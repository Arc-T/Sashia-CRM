-- catalog: tables with inline foreign keys and Persian comments
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE vendors (
  id                      BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'شناسه یکتا',
  version                 BIGINT       NOT NULL DEFAULT 0 COMMENT 'نسخه برای همزمانی',
  created_at              DATETIME(6)  NOT NULL COMMENT 'تاریخ ایجاد',
  updated_at              DATETIME(6)  NOT NULL COMMENT 'تاریخ بروزرسانی',
  created_by              VARCHAR(100) NULL COMMENT 'ایجادکننده',
  updated_by              VARCHAR(100) NULL COMMENT 'بروزرسان',
  name                    VARCHAR(255) NOT NULL COMMENT 'نام تأمین‌کننده',
  phone                   VARCHAR(20)  NULL COMMENT 'شماره تماس',
  email                   VARCHAR(255) NULL COMMENT 'آدرس ایمیل',
  city                    VARCHAR(255) NULL COMMENT 'شهر',
  website                 VARCHAR(500) NULL COMMENT 'وب‌سایت',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='تأمین‌کنندگان کالا و خدمات';

CREATE TABLE products (
  id                      BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'شناسه یکتا',
  version                 BIGINT       NOT NULL DEFAULT 0 COMMENT 'نسخه برای همزمانی',
  created_at              DATETIME(6)  NOT NULL COMMENT 'تاریخ ایجاد',
  updated_at              DATETIME(6)  NOT NULL COMMENT 'تاریخ بروزرسانی',
  created_by              VARCHAR(100) NULL COMMENT 'ایجادکننده',
  updated_by              VARCHAR(100) NULL COMMENT 'بروزرسان',
  name                    VARCHAR(255) NOT NULL COMMENT 'نام محصول',
  sku                     VARCHAR(255) NULL COMMENT 'کد انبارداری (SKU)',
  category                VARCHAR(32)  NULL COMMENT 'دسته‌بندی محصول',
  vendor_id               BIGINT       NULL COMMENT 'شناسه تأمین‌کننده',
  price                   DECIMAL(19,2) NOT NULL COMMENT 'قیمت پایه فروش',
  stock                   INT          NULL COMMENT 'موجودی انبار',
  active                  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT 'وضعیت فعال/غیرفعال بودن محصول',
  description             VARCHAR(2000) NULL COMMENT 'توضیحات و مشخصات فنی',
  PRIMARY KEY (id),
  KEY idx_products_vendor_id (vendor_id),
  CONSTRAINT fk_products_vendor FOREIGN KEY (vendor_id) REFERENCES vendors (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='کاتالوگ محصولات';

CREATE TABLE services (
  id                      BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'شناسه یکتا',
  version                 BIGINT       NOT NULL DEFAULT 0 COMMENT 'نسخه برای همزمانی',
  created_at              DATETIME(6)  NOT NULL COMMENT 'تاریخ ایجاد',
  updated_at              DATETIME(6)  NOT NULL COMMENT 'تاریخ بروزرسانی',
  created_by              VARCHAR(100) NULL COMMENT 'ایجادکننده',
  updated_by              VARCHAR(100) NULL COMMENT 'بروزرسان',
  name                    VARCHAR(255) NOT NULL COMMENT 'عنوان خدمت',
  code                    VARCHAR(255) NULL COMMENT 'کد خدمت',
  category                VARCHAR(32)  NULL COMMENT 'دسته‌بندی خدمت',
  price                   DECIMAL(19,2) NOT NULL COMMENT 'تعرفه ارائه خدمت',
  hours                   INT          NULL COMMENT 'مدت زمان استاندارد به ساعت',
  active                  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT 'وضعیت فعال بودن خدمت',
  description             VARCHAR(2000) NULL COMMENT 'شرح جزئیات خدمت',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='خدمات و تعرفه‌ها';

CREATE TABLE price_books (
  id                      BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'شناسه یکتا',
  version                 BIGINT       NOT NULL DEFAULT 0 COMMENT 'نسخه برای همزمانی',
  created_at              DATETIME(6)  NOT NULL COMMENT 'تاریخ ایجاد',
  updated_at              DATETIME(6)  NOT NULL COMMENT 'تاریخ بروزرسانی',
  created_by              VARCHAR(100) NULL COMMENT 'ایجادکننده',
  updated_by              VARCHAR(100) NULL COMMENT 'بروزرسان',
  name                    VARCHAR(255) NOT NULL COMMENT 'عنوان لیست قیمت',
  status                  VARCHAR(32)  NULL COMMENT 'وضعیت فعال یا منقضی',
  discount_pct            INT          NULL COMMENT 'درصد تخفیف پیش‌فرض',
  valid_from              DATE         NULL COMMENT 'تاریخ آغاز اعتبار',
  valid_to                DATE         NULL COMMENT 'تاریخ پایان اعتبار',
  description             VARCHAR(2000) NULL COMMENT 'توضیحات لیست قیمت',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='دفاتر قیمت‌گذاری و تخفیفات';

CREATE TABLE inventory (
  id                      BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'شناسه یکتا',
  version                 BIGINT       NOT NULL DEFAULT 0 COMMENT 'نسخه برای همزمانی',
  created_at              DATETIME(6)  NOT NULL COMMENT 'تاریخ ایجاد',
  updated_at              DATETIME(6)  NOT NULL COMMENT 'تاریخ بروزرسانی',
  created_by              VARCHAR(100) NULL COMMENT 'ایجادکننده',
  updated_by              VARCHAR(100) NULL COMMENT 'بروزرسان',
  sku                     VARCHAR(255) NOT NULL COMMENT 'کد کالای انبار',
  product_id              BIGINT       NOT NULL COMMENT 'شناسه محصول مرتبط',
  warehouse               VARCHAR(32)  NULL COMMENT 'انبار نگهداری',
  quantity                INT          NULL COMMENT 'تعداد موجودی فیزیکی',
  reorder_level           INT          NULL COMMENT 'حداقل نقطه سفارش مجدد',
  status                  VARCHAR(32)  NULL COMMENT 'وضعیت سلامت موجودی',
  PRIMARY KEY (id),
  KEY idx_inventory_product_id (product_id),
  KEY idx_inventory_status (status),
  CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='موجودی انبار و کالاها';

CREATE TABLE assets (
  id                      BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'شناسه یکتا',
  version                 BIGINT       NOT NULL DEFAULT 0 COMMENT 'نسخه برای همزمانی',
  created_at              DATETIME(6)  NOT NULL COMMENT 'تاریخ ایجاد',
  updated_at              DATETIME(6)  NOT NULL COMMENT 'تاریخ بروزرسانی',
  created_by              VARCHAR(100) NULL COMMENT 'ایجادکننده',
  updated_by              VARCHAR(100) NULL COMMENT 'بروزرسان',
  name                    VARCHAR(255) NOT NULL COMMENT 'نام دارایی یا تجهیز',
  serial                  VARCHAR(255) NULL COMMENT 'شماره سریال دارایی',
  organization_id         BIGINT       NULL COMMENT 'شناسه سازمان مالک',
  status                  VARCHAR(32)  NULL COMMENT 'وضعیت عملیاتی دارایی',
  purchase_date           DATE         NULL COMMENT 'تاریخ خرید دارایی',
  warranty_end            DATE         NULL COMMENT 'تاریخ اتمام گارانتی',
  PRIMARY KEY (id),
  KEY idx_assets_organization_id (organization_id),
  KEY idx_assets_status (status),
  CONSTRAINT fk_assets_organization FOREIGN KEY (organization_id) REFERENCES organizations (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='دارایی‌ها و اموال تحویل داده شده';

SET FOREIGN_KEY_CHECKS = 1;
