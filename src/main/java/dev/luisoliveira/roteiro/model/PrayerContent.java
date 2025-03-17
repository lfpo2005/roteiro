package dev.luisoliveira.roteiro.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "prayer")
public class PrayerContent {

    @Id
    private UUID id;

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
    private String processId;
    private String userId;// ID do processo que gerou a oração

    // Campos para controle de datas
    private LocalDateTime createdAt; // Data de criação
    private LocalDateTime updatedAt; // Data de atualização


    public PrayerContent(String oracaoContent) {
    }
}