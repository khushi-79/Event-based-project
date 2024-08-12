package com.eventbasedarchitechture.event_based;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
                // Process the event (e.g., print payload)
                System.out.println("Processing event: " + event.getPayload());
                // Update event status
                event.setStatus(Event.EventStatus.PROCESSED);
                eventRepository.save(event);
            } catch (Exception e) {
                // Handle failure
                event.setStatus(Event.EventStatus.FAILED);
                eventRepository.save(event);
            }
        }
    }

    private List fetchPendingEvents() {
        // Lock rows for update to prevent concurrent processing
        Query query = entityManager.createQuery("SELECT e FROM Event e WHERE e.status = :status")
                .setParameter("status", Event.EventStatus.PENDING)
                .setLockMode(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        return query.getResultList();
    }

//    private void processEvent(Event event) {
//        // Simulate processing logic
//        System.out.println("Processing event: " + event.getPayload());
//    }
}
