package com.example.restaurant.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "saga_state")
public class SagaState {

    @Id
    @Column(name = "saga_id")
    private String sagaId;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "current_step", nullable = false)
    private String currentStep;

    @Column(nullable = false)
    private String status;

    @Column(name = "last_event_id")
    private String lastEventId;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "started_at", insertable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public String getSagaId() {
        return sagaId;
    }

    public void setSagaId(String sagaId) {
        this.sagaId = sagaId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(String currentStep) {
        this.currentStep = currentStep;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLastEventId() {
        return lastEventId;
    }

    public void setLastEventId(String lastEventId) {
        this.lastEventId = lastEventId;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
