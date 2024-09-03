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

    private static final int MAX_ATTEMPTS = 3;
    LocalDateTime now = LocalDateTime.now();

    @Transactional
    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.SECONDS)
    public void processPendingEvents() {
        List<Event> eventsPending = fetchPendingEvents();
        for (Event event : eventsPending) {
            String urlToCall = event.getOpenUrl();
                processEvent(event, urlToCall,now);
        }
    }

    @Transactional
    @Scheduled(fixedDelay = 45, timeUnit = TimeUnit.SECONDS)
    public void processFailedEvents(){
        List<Event> eventsToRetry = fetchRetryEventsWithinTimeWindow();
        for (Event event : eventsToRetry) {
            String urlToCall = event.getOpenUrl();
                processEvent(event, urlToCall, now);
        }
    }

    private List<Event> fetchPendingEvents() {
        Query query = entityManager.createQuery("SELECT e FROM Event e WHERE e.status = :status")
                .setParameter("status", Event.EventStatus.PENDING)
                .setLockMode(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        return query.getResultList();
    }

    private List<Event> fetchRetryEventsWithinTimeWindow() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowEndTime = now.plusSeconds(10);

        Query query = entityManager.createQuery(
                        "SELECT e FROM Event e WHERE e.status = :status AND e.retryTimestamp <= :windowEndTime")
                .setParameter("status", Event.EventStatus.RETRY)
                .setParameter("windowEndTime", windowEndTime)
                .setLockMode(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);

        return query.getResultList();
    }

    private void processEvent(Event event, String urlToCall, LocalDateTime now) {
        long retryIntervalSeconds = 10L * event.getAttempt();

        if (event.getAttempt() < MAX_ATTEMPTS) {
                try {
                    RestTemplate restTemplate = new RestTemplate();
                    ResponseEntity<String> response = restTemplate.getForEntity(urlToCall, String.class);

                    if (response.getStatusCode().is2xxSuccessful()) {
                        event.setStatus(Event.EventStatus.PROCESSED);
                    }
                    else {
                        if (now.isAfter(event.getRetryTimestamp().plusSeconds(retryIntervalSeconds)))
                        {
                        event.setStatus(Event.EventStatus.RETRY);
                        event.setRetryTimestamp(now);
                        event.setAttempt(event.getAttempt() + 1);
                    }
                    }
                } catch (Exception e) {
                    log.error("Error processing retry event with URL: {}", urlToCall, e);
                    event.setStatus(Event.EventStatus.RETRY);
                    event.setRetryTimestamp(now);
                    event.setAttempt(event.getAttempt() + 1);
                }
        } else {
            event.setStatus(Event.EventStatus.FAILED);
        }
        eventRepository.save(event);
    }

}
