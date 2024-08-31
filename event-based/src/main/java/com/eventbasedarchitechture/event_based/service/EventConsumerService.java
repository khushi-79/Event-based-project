package com.eventbasedarchitechture.event_based.service;

import com.eventbasedarchitechture.event_based.entity.repository.EventRepository;
import com.eventbasedarchitechture.event_based.entity.model.Event;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class EventConsumerService {

    @Autowired
    private EventRepository eventRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private static final long RETRY_DELAY_SECONDS = 45; // Retry delay after the status is set to RETRY

    @Transactional
    @Scheduled(fixedDelay = 15, timeUnit = TimeUnit.SECONDS) // Poll every 15 seconds
    public void processEvents() {
        List<Event> events = fetchAllEvents();
        LocalDateTime now = LocalDateTime.now();

        for (Event event : events) {
            String urlToCall = event.getOpenUrl();

            if (event.getStatus() == Event.EventStatus.PENDING) {
                processPendingEvent(event, urlToCall);
            }

            else if (event.getStatus() == Event.EventStatus.RETRY) {
                processRetryEvent(event, urlToCall, now);
            }
        }
    }

    private void processPendingEvent(Event event, String urlToCall) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(urlToCall, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                event.setStatus(Event.EventStatus.PROCESSED);
            } else {
                event.setStatus(Event.EventStatus.RETRY);
                event.setRetryTimestamp(LocalDateTime.now()); // Set retry timestamp
            }
        } catch (Exception e) {
            log.error("Error processing event with URL: {}", urlToCall, e);
            event.setStatus(Event.EventStatus.RETRY);
            event.setRetryTimestamp(LocalDateTime.now()); // Set retry timestamp
        }

        eventRepository.save(event);
    }

    private void processRetryEvent(Event event, String urlToCall, LocalDateTime now) {
        if (event.getRetryTimestamp() == null || now.isBefore(event.getRetryTimestamp().plusSeconds(RETRY_DELAY_SECONDS))) {
            log.info("Retry delay has not passed for event with ID: {}", event.getId());
            return;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(urlToCall, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                event.setStatus(Event.EventStatus.PROCESSED);
            } else {
                event.setStatus(Event.EventStatus.FAILED);
            }
        } catch (Exception e) {
            log.error("Error processing retry event with URL: {}", urlToCall, e);
            event.setStatus(Event.EventStatus.FAILED);
        }

        eventRepository.save(event);
    }

    private List<Event> fetchAllEvents() {
        Query query = entityManager.createQuery("SELECT e FROM Event e WHERE e.status = :status OR e.status = :retryStatus")
                .setParameter("status", Event.EventStatus.PENDING)
                .setParameter("retryStatus", Event.EventStatus.RETRY)
                .setLockMode(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        return query.getResultList();
    }
}
