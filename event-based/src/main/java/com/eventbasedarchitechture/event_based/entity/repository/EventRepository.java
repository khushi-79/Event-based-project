package com.eventbasedarchitechture.event_based.entity.repository;

import com.eventbasedarchitechture.event_based.entity.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;


public interface EventRepository extends JpaRepository<Event, Long> {

}
