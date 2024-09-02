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

import java.time.Duration;
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

    private static final int MAX_ATTEMPTS = 5; // Maximum number of retry attempts

    @Transactional
    @Scheduled(fixedDelay = 15, timeUnit = TimeUnit.SECONDS) // Poll every 15 seconds
    public void processEvents() {
        List<Event> eventsToRetry = fetchRetryEventsWithinTimeWindow();
        LocalDateTime now = LocalDateTime.now();

        for (Event event : eventsToRetry) {
            String urlToCall = event.getOpenUrl();
            if (event.getStatus() == Event.EventStatus.RETRY) {
                processRetryEvent(event, urlToCall, now);
            }
        }

        List<Event> events = fetchAllEvents();
        for (Event event : events) {
            String urlToCall = event.getOpenUrl();
            if (event.getStatus() == Event.EventStatus.PENDING) {
                processPendingEvent(event, urlToCall);
            }
        }


//        List<Event> events = fetchAllEvents();
//        LocalDateTime now = LocalDateTime.now();
//
//        for (Event event : events) {
//            String urlToCall = event.getOpenUrl();
//
//            if (event.getStatus() == Event.EventStatus.PENDING) {
//                processPendingEvent(event, urlToCall);
//            }
//            else if (event.getStatus() == Event.EventStatus.RETRY) {
//                processRetryEvent(event, urlToCall, now);
//            }
//        }
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

    private void processPendingEvent(Event event, String urlToCall) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(urlToCall, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                event.setStatus(Event.EventStatus.PROCESSED);
            } else {
                event.setStatus(Event.EventStatus.RETRY);
                event.setRetryTimestamp(LocalDateTime.now()); // Set retry timestamp
                event.setAttempt(1); // Initialize attempt count
            }
        } catch (Exception e) {
            log.error("Error processing event with URL: {}", urlToCall, e);
            event.setStatus(Event.EventStatus.RETRY);
            event.setRetryTimestamp(LocalDateTime.now()); // Set retry timestamp
            event.setAttempt(1); // Initialize attempt count
        } finally {
            eventRepository.save(event); // Ensure event is saved in all cases
        }
    }

    private void processRetryEvent(Event event, String urlToCall, LocalDateTime now) {
        long retryIntervalSeconds = 15L * event.getAttempt(); // 15 seconds multiplied by the attempt number

        try {
            // Check if the event has not reached the maximum number of attempts
            if (event.getAttempt() < MAX_ATTEMPTS) {
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<String> response = restTemplate.getForEntity(urlToCall, String.class);

                // If request is successful, mark event as processed
                if (response.getStatusCode().is2xxSuccessful()) {
                    event.setStatus(Event.EventStatus.PROCESSED);
                } else {
                    long secondsSinceLastRetry = Duration.between(event.getRetryTimestamp().plusSeconds(retryIntervalSeconds), now).getSeconds();

                    // If the retry interval has passed, increment the attempt count and retry
                    if (secondsSinceLastRetry <= retryIntervalSeconds) {
                        event.setAttempt(event.getAttempt() + 1); // Increment the attempt count
                        event.setRetryTimestamp(now); // Update the retry timestamp for the next retry
                        event.setStatus(Event.EventStatus.RETRY); // Set status to RETRY
                    }
                }
            }

            if(event.getAttempt()>=MAX_ATTEMPTS){
                event.setStatus(Event.EventStatus.RETRY);
            }

        } catch (Exception e) {
            log.error("Error processing retry event with URL: {}", urlToCall, e);
            event.setStatus(Event.EventStatus.FAILED); // Set status to FAILED if there is an exception
        }

        eventRepository.save(event); // Save event in all cases
    }
//    private void processRetryEvent(Event event, String urlToCall, LocalDateTime now) {
//        long retryIntervalSeconds = 7L * event.getAttempt(); // 7 seconds multiplied by the attempt number
//
//        // Only process retries if the max attempts have not been reached
//        if (event.getAttempt() < MAX_ATTEMPTS) {
//
////            if (event.getRetryTimestamp() == null || now.isAfter(event.getRetryTimestamp().plusSeconds(retryIntervalSeconds))) {
//                try {
//                    RestTemplate restTemplate = new RestTemplate();
//                    ResponseEntity<String> response = restTemplate.getForEntity(urlToCall, String.class);
//
//                    if (response.getStatusCode().is2xxSuccessful()) {
//                        event.setStatus(Event.EventStatus.PROCESSED);
//                    } else {
//                        long newRetryTime = Duration.between(LocalDateTime.now(), event.getRetryTimestamp().plusSeconds(retryIntervalSeconds)).getSeconds();
//
////                        event.setStatus(Event.EventStatus.RETRY);
////                        event.setRetryTimestamp(now); // Update the retry timestamp for the next retry
////                        event.setAttempt(event.getAttempt() + 1); // Increment the attempt count
//
//                        if (newRetryTime <= 5) {
//                            event.setStatus(Event.EventStatus.RETRY);
//                            event.setAttempt(event.getAttempt() + 1);
//                            event.setRetryTimestamp(now);
//                        }
//
//                    }
//                } catch (Exception e) {
//                    log.error("Error processing retry event with URL: {}", urlToCall, e);
//                    event.setStatus(Event.EventStatus.RETRY);
//                    event.setRetryTimestamp(now); // Update the retry timestamp for another retry attempt
//                    event.setAttempt(event.getAttempt() + 1); // Increment the attempt count
//                }
////
////            } else {
////                log.info("Retry delay has not passed for event with ID: {}", event.getId());
////            }
//        } else {
//            event.setStatus(Event.EventStatus.FAILED); // Mark as failed after max attempts
//        }
//
//        eventRepository.save(event); // Ensure the event is saved in all cases
//    }

    private List<Event> fetchAllEvents() {
        Query query = entityManager.createQuery("SELECT e FROM Event e WHERE e.status = :status OR e.status = :retryStatus")
                .setParameter("status", Event.EventStatus.PENDING)
                .setParameter("retryStatus", Event.EventStatus.RETRY)
                .setLockMode(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        return query.getResultList();
    }
}
