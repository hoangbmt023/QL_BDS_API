package com.example.qlbds.conversation_service.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.example.qlbds.property_service.entity.Property;
import com.example.qlbds.user_service.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "conversations", uniqueConstraints = {
        @UniqueConstraint(name = "uq_conversation", columnNames = { "property_id", "user_one_id", "user_two_id" })
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "property_id", foreignKey = @ForeignKey(name = "fk_conv_property"))
    private Property property;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_one_id", nullable = false, foreignKey = @ForeignKey(name = "fk_conv_user_one"))
    private User userOne;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_two_id", nullable = false, foreignKey = @ForeignKey(name = "fk_conv_user_two"))
    private User userTwo;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
