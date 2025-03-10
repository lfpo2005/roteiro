package dev.luisoliveira.roteiro.service;

import dev.luisoliveira.roteiro.event.ContentEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventBusService {

    private final ApplicationEventPublisher eventPublisher;

    public void publish(ContentEvent event) {
        eventPublisher.publishEvent(event);
    }
}