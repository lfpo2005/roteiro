package dev.luisoliveira.roteiro.repository;

import dev.luisoliveira.roteiro.model.PrayerContent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PrayerContentRepository extends MongoRepository<PrayerContent, UUID> {


    List<PrayerContent> findByTitleContaining(String title);

    List<PrayerContent> findByTheme(String theme);

    List<PrayerContent> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<PrayerContent> findByTextoContaining(String texto);

    List<PrayerContent> findByProcessId(String processId);

    List<PrayerContent> findByLanguage(String language);

    List<PrayerContent> findByStyle(String style);

    List<PrayerContent> findByUserId(String userId);

    List<PrayerContent> findByUserIdOrderByCreatedAtDesc(String userId);


}