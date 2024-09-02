package com.eventbasedarchitechture.event_based.entity.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
public class Event {

    public enum EventStatus {
        PENDING,
        PROCESSED,
        FAILED,
        RETRY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public int getAttempt() {
        return attempt;
    }

    public void setAttempt(int attempt) {
        this.attempt = attempt;
    }

    private int attempt;

    private String payload;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    private LocalDateTime timestamp;

    public LocalDateTime getRetryTimestamp() {
        return retryTimestamp;
    }

    public void setRetryTimestamp(LocalDateTime retryTimestamp) {
        this.retryTimestamp = retryTimestamp;
    }

    private LocalDateTime retryTimestamp;

    private String openUrl;

    public String getOpenUrl() {
        return openUrl;
    }

    public void setOpenUrl(String openUrl) {
        this.openUrl = openUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
