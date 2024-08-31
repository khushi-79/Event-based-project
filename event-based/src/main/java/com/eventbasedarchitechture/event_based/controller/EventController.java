package com.eventbasedarchitechture.event_based.controller;

import com.eventbasedarchitechture.event_based.entity.repository.EventRepository;
import com.eventbasedarchitechture.event_based.entity.model.Event;
import com.eventbasedarchitechture.event_based.service.EventConsumerService;
import com.eventbasedarchitechture.event_based.service.EventProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
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


//    @PostMapping
//    public ResponseEntity<Event> createEvent(@RequestBody String payload){
//        Event event = eventProducerService.produceEvent(payload);
//        return ResponseEntity.status(HttpStatus.CREATED).body(event);
//
//    }
//@PostMapping
//public ResponseEntity<Event> createEvent(@RequestBody Event eventRequest) {
//    Event event = eventProducerService.produceEvent(
//            eventRequest.getPayload(),
//            eventRequest.getOpenUrl()
//    );
//    return ResponseEntity.status(HttpStatus.CREATED).body(event);
//}

    @PostMapping
    public ResponseEntity<Event> createEvent(
            @RequestParam(required = false) String payload,
            @RequestParam(required = false) String openUrl) {

        if (payload == null || payload.isEmpty() || openUrl == null || openUrl.isEmpty()) {
            return ResponseEntity.badRequest().body(null); // Send 400 response if validation fails
        }

        Event event = eventProducerService.produceEvent(payload, openUrl);
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    @PutMapping("/{id}/retry")
    public ResponseEntity<Event> updateEventForRetry(@PathVariable Long id, @RequestParam String openUrl) {
        Optional<Event> optionalEvent = eventRepository.findById(id);
        if (optionalEvent.isPresent()) {
            Event event = optionalEvent.get();
            if (event.getStatus() == Event.EventStatus.RETRY) {
                event.setOpenUrl(openUrl);
                eventRepository.save(event);
                return ResponseEntity.ok(event);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Only allow retry updates for RETRY status
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }


    @GetMapping
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        Optional<Event> event = eventRepository.findById(id);
        return event.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }


}
