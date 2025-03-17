package dev.luisoliveira.roteiro.repository;

import dev.luisoliveira.roteiro.model.PrayerContent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositório unificado para acesso às orações no MongoDB
 * Combina as funcionalidades de OracaoRepository e PrayerContentRepository
 */
@Repository
public interface PrayerContentRepository extends MongoRepository<PrayerContent, String> {

    /**
     * Busca orações por título
     * 
     * @param title Título para busca (parcial)
     * @return Lista de orações
     */
    List<PrayerContent> findByTitleContaining(String title);

    /**
     * Busca orações por tema
     * 
     * @param theme Tema para busca
     * @return Lista de orações
     */
    List<PrayerContent> findByTheme(String theme);

    /**
     * Busca orações criadas em um período
     * 
     * @param start Data inicial
     * @param end   Data final
     * @return Lista de orações
     */
    List<PrayerContent> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Busca orações por conteúdo
     * 
     * @param texto Texto para busca (parcial)
     * @return Lista de orações
     */
    List<PrayerContent> findByTextoContaining(String texto);

    /**
     * Busca orações por ID do processo
     * 
     * @param processId ID do processo
     * @return Lista de orações
     */
    List<PrayerContent> findByProcessId(String processId);

    /**
     * Busca orações por idioma
     * 
     * @param language Idioma
     * @return Lista de orações
     */
    List<PrayerContent> findByLanguage(String language);

    /**
     * Busca orações por estilo
     * 
     * @param style Estilo
     * @return Lista de orações
     */
    List<PrayerContent> findByStyle(String style);

    /**
     * Busca orações por data de criação (compatibilidade com Oracao)
     * 
     * @param inicio Data inicial
     * @param fim    Data final
     * @return Lista de orações
     */
    List<PrayerContent> findByDataCriacaoBetween(LocalDateTime inicio, LocalDateTime fim);
}