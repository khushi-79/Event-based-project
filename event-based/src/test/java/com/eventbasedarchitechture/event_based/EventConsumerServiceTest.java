package com.eventbasedarchitechture.event_based;

import com.eventbasedarchitechture.event_based.entity.model.Event;
import com.eventbasedarchitechture.event_based.entity.repository.EventRepository;
import com.eventbasedarchitechture.event_based.service.EventConsumerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class EventConsumerServiceTest {

    @Autowired
    private EventConsumerService consumerService;

    @Autowired
    private EventRepository eventRepository;

    @Test
    @Transactional
    public void testEventProcessing() {
        // Arrange
        Event event1 = new Event();
        event1.setPayload("Test Event 1");
        event1.setStatus(Event.EventStatus.PENDING);
        event1.setTimestamp(LocalDateTime.now());
        eventRepository.save(event1);

        Event event2 = new Event();
        event2.setPayload("Test Event 2");
        event2.setStatus(Event.EventStatus.PENDING);
        event2.setTimestamp(LocalDateTime.now());
        eventRepository.save(event2);

        // Act
//        consumerService.processPendingEvents();

        // Assert
        List<Event> events = eventRepository.findAll();
        assertEquals(2, events.size());
        assertEquals(Event.EventStatus.PROCESSED, events.get(0).getStatus());
        System.out.println(event1.getStatus());
        assertEquals(Event.EventStatus.PROCESSED, events.get(1).getStatus());



        // Ensure the first event failed and the second event was processed
//        assertEquals(Event.EventStatus.FAILED, events.get(0).getStatus());
    }



}
