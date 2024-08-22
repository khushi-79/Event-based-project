package com.eventbasedarchitechture.event_based;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {

    @Autowired
    private EventProducerService eventProducerService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventConsumerService eventConsumerService;


    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody String payload){
        Event event = eventProducerService.produceEvent(payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    @GetMapping
    public List<Event> getAllEvents() {
        eventConsumerService.processPendingEvents();
        return eventRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        Optional<Event> event = eventRepository.findById(id);
        return event.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/auth")
    public ResponseEntity<String> authEndpoint(@RequestBody String payload) {
        return ResponseEntity.ok("Authorized request with payload: " + payload);
    }

    @PostMapping("/token")
    public ResponseEntity<String> tokenEndpoint(@RequestBody String payload) {
        return ResponseEntity.ok("Token validated request with payload: " + payload);
    }

}
