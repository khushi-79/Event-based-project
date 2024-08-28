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

import java.util.List;

@Slf4j
@Service
public class EventConsumerService {

    @Autowired
    private EventRepository eventRepository;

    @PersistenceContext
    private EntityManager entityManager;


    @Transactional
    @Scheduled(fixedRate = 5000) // Poll every 5 seconds
    public void processPendingEvents() {
        List<Event> events = fetchPendingEvents();
        for (Event event : events) {
            try {
                RestTemplate restTemplate = new RestTemplate();

                String openApiUrl = "https://reqres.in/api/users";
                ResponseEntity<String> response = restTemplate.getForEntity(openApiUrl, String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    event.setStatus(Event.EventStatus.PROCESSED);
                    log.info("Event processed successfully: {}", event.getId());
                } else {
                    throw new Exception("Failed to get a successful response from the API");
                }
            } catch (Exception e) {
                log.error("Failed to process event: {}", event.getId(), e);
                event.setStatus(Event.EventStatus.FAILED);
            }
            eventRepository.save(event);
        }
//            try {
//                // Process the event (e.g., print payload)
//                System.out.println("Processing event: " + event.getPayload());
//                // Update event status
//                event.setStatus(Event.EventStatus.PROCESSED);
//                eventRepository.save(event);
//
//                log.info("Event processed successfully: {}", event.getId());
//            } catch (Exception e) {
//                log.error("Failed to process event: {}",event.getId(),e);
//                // Handle failure
//                event.setStatus(Event.EventStatus.FAILED);
//                eventRepository.save(event);
//            }
//        }
    }

    private List fetchPendingEvents() {
        // Lock rows for update to prevent concurrent processing
        Query query = entityManager.createQuery("SELECT e FROM Event e WHERE e.status = :status")
                .setParameter("status", Event.EventStatus.PENDING)
                .setLockMode(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        return query.getResultList();
    }

}