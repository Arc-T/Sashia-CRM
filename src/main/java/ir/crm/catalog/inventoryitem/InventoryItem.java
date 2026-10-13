package ir.crm.catalog.inventoryitem;

import ir.crm.catalog.product.Product;
import ir.crm.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItem extends BaseEntity {

    @Column(name = "sku", nullable = false, length = 255)
    private String sku;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "warehouse", length = 32)
    private InventoryItemWarehouse warehouse;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "reorder_level")
    private Integer reorderLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32)
    private InventoryItemStatus status;

    // Domain helpers
    public boolean isLowStock() {
        return this.quantity != null && this.reorderLevel != null && this.quantity <= this.reorderLevel;
    }

    public void updateStockStatus() {
        if (this.quantity == null || this.quantity <= 0) {
            this.status = InventoryItemStatus.OUT;
        } else if (this.reorderLevel != null && this.quantity <= this.reorderLevel) {
            this.status = InventoryItemStatus.LOW;
        } else {
            this.status = InventoryItemStatus.IN;
        }
    }

    public Long getProductId() {
        return this.product != null ? this.product.getId() : null;
    }
}
