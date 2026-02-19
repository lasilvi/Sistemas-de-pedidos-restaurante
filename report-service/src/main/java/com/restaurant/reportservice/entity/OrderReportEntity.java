package com.restaurant.reportservice.entity;

import com.restaurant.reportservice.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "report_orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderReportEntity {
    @Id
    private UUID id;

    @Column(name = "table_id", nullable = false)
    private Integer tableId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItemReportEntity> items = new ArrayList<>();

    public void addItem(OrderItemReportEntity item) {
        item.setOrder(this);
        items.add(item);
    }
}
