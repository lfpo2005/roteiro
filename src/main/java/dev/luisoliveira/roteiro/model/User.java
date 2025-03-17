package dev.luisoliveira.roteiro.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private UUID id;

    private String name;
    private String email;
    private String googleId;
    private String pictureUrl;

}