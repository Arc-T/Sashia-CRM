package ir.crm.catalog.product;

import ir.crm.catalog.vendor.Vendor;
import ir.crm.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "sku", length = 255)
    private String sku;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 32)
    private ProductCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(name = "stock")
    private Integer stock;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "description", length = 2000)
    private String description;

    // Domain helpers
    public boolean isInStock() {
        return this.stock != null && this.stock > 0;
    }

    public boolean canFulfill(int quantity) {
        return this.active && this.stock != null && this.stock >= quantity;
    }

    public void reduceStock(int quantity) {
        if (!canFulfill(quantity)) {
            throw new IllegalStateException("Insufficient stock or product inactive for product: " + this.name);
        }
        this.stock -= quantity;
    }

    public void addStock(int quantity) {
        if (this.stock == null) {
            this.stock = 0;
        }
        this.stock += quantity;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public Long getVendorId() {
        return this.vendor != null ? this.vendor.getId() : null;
    }
}
