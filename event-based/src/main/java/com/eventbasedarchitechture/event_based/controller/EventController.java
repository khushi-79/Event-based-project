package com.eventbasedarchitechture.event_based;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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

//    @PostMapping("/auth")
//    public ResponseEntity<String> authEndpoint(@RequestBody String payload) {
//        return ResponseEntity.ok("Authorized request with payload: " + payload);
//    }

    @PostMapping("/token")
    public ResponseEntity<String> tokenEndpoint(@RequestParam String payload) {
        return ResponseEntity.ok("Token validated request with payload: " + payload);
    }

    @PostMapping("/create-with-id")
    public ResponseEntity<String> createEventWithId(@RequestParam String payload, @RequestParam Long id) {
        // Check if the ID already exists in the database
        Optional<Event> existingEvent = eventRepository.findById(id);

        if (existingEvent.isPresent()) {
            // If the ID exists, return an error response
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Event with ID " + id + " already exists.");
        }

        // If the ID is unique, create a new event
        Event newEvent = new Event();
        newEvent.setId(id);
        newEvent.setPayload(payload);
        newEvent.setStatus(Event.EventStatus.PENDING); // Assuming the event starts in PENDING status
        newEvent.setTimestamp(LocalDateTime.now());

        // Save the event to the database
        eventRepository.save(newEvent);

        // Return a success response
        return ResponseEntity.ok("Event created successfully with ID: " + id);
    }

}
