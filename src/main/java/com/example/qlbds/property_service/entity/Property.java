package com.example.qlbds.property_service.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.qlbds.shared.entity.enums.PropertyStatus;
import com.example.qlbds.user_service.entity.Agent;
import com.example.qlbds.user_service.entity.Owner;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "properties", indexes = {
        @Index(name = "idx_properties_city", columnList = "city"),
        @Index(name = "idx_properties_district", columnList = "district"),
        @Index(name = "idx_properties_price", columnList = "price"),
        @Index(name = "idx_properties_status", columnList = "status"),
        @Index(name = "idx_properties_owner", columnList = "owner_id"),
        @Index(name = "idx_properties_agent", columnList = "agent_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uq_properties_slug", columnNames = "slug")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column
    private Double area;

    @Column
    private Integer bedrooms;

    @Column
    private Integer bathrooms;

    @Column(length = 255)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String district;

    @Column(length = 255, unique = true)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PropertyStatus status = PropertyStatus.PENDING;

    @Column(nullable = false)
    @Builder.Default
    private Boolean visibility = true;

    @Column
    @Builder.Default
    private Integer viewCount = 0;

    @Column
    @Builder.Default
    private Integer favoriteCount = 0;

    @ManyToOne
    @JoinColumn(name = "owner_id", foreignKey = @ForeignKey(name = "fk_property_owner"))
    private Owner owner;

    @ManyToOne
    @JoinColumn(name = "agent_id", foreignKey = @ForeignKey(name = "fk_property_agent"))
    private Agent agent;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
