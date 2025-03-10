package dev.luisoliveira.roteiro.service;

import dev.luisoliveira.roteiro.event.ContentEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventBusService {

    private final ApplicationEventPublisher eventPublisher;

    public void publish(ContentEvent event) {
        if (event == null) {
            log.error("Tentativa de publicar evento nulo");
            throw new IllegalArgumentException("Evento não pode ser nulo");
        }

        String eventClassName = event.getClass().getSimpleName();
        String processId = event.getProcessId();

        log.info("Publicando evento: {} para o processId: {}", eventClassName, processId);

        try {
            eventPublisher.publishEvent(event);
            log.info("Evento {} publicado com sucesso para o processId: {}", eventClassName, processId);
        } catch (Exception e) {
            log.error("Erro ao publicar evento {} para o processId {}: {}",
                    eventClassName, processId, e.getMessage(), e);
            throw e;
        }
    }
}