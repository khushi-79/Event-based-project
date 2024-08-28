package com.eventbasedarchitechture.event_based.services;

import com.eventbasedarchitechture.event_based.entity.repository.EventRepository;
import com.eventbasedarchitechture.event_based.entity.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EventProducerService {

    @Autowired
    private EventRepository eventRepository;

    public Event produceEvent(String payload){
        Event event = new Event();
        event.setPayload(payload);
        event.setStatus(Event.EventStatus.PENDING);
        event.setTimestamp(LocalDateTime.now());
        return eventRepository.save(event);
    }
}
