package dev.luisoliveira.roteiro.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modelo unificado para representar orações no MongoDB
 * Combina as funcionalidades de Oracao e PrayerContent
 */
@Document(collection = "oracoes")
public class PrayerContent {

    @Id
    private String id;

    // Campos básicos da oração
    private String texto; // Conteúdo principal da oração
    private String audioUrl; // URL do áudio da oração

    // Campos adicionais para metadados
    private String title; // Título da oração
    private String theme; // Tema da oração
    private String style; // Estilo da oração
    private String duration; // Duração da oração
    private String shortContent; // Versão curta da oração
    private String description; // Descrição para redes sociais
    private String language; // Idioma da oração
    private String processId; // ID do processo que gerou a oração

    // Campos para controle de datas
    private LocalDateTime createdAt; // Data de criação
    private LocalDateTime updatedAt; // Data de atualização

    public PrayerContent() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public PrayerContent(String texto) {
        this();
        this.texto = texto;
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getFullContent() {
        return texto; // Alias para manter compatibilidade
    }

    public void setFullContent(String fullContent) {
        this.texto = fullContent; // Alias para manter compatibilidade
    }

    public String getShortContent() {
        return shortContent;
    }

    public void setShortContent(String shortContent) {
        this.shortContent = shortContent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Aliases para manter compatibilidade com Oracao
    public LocalDateTime getDataCriacao() {
        return createdAt;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.createdAt = dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return updatedAt;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.updatedAt = dataAtualizacao;
    }
}