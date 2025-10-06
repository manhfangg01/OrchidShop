package com.orchid_backend.model.entity;

import java.time.Instant;

import com.orchid_backend.util.constant.PaymentMethod;
import com.orchid_backend.util.constant.PaymentStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Instant paymentDate;
    private Long amount;
    private PaymentMethod method;
    private PaymentStatus status;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

}
