package com.eventbasedarchitechture.event_based;

import com.eventbasedarchitechture.event_based.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;


public interface EventRepository extends JpaRepository<Event, Long> {

}
