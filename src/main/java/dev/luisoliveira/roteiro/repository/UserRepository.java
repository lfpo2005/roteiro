package dev.luisoliveira.roteiro.repository;

import dev.luisoliveira.roteiro.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends MongoRepository<User, UUID> {

    Optional<User> findByGoogleId(String googleId);
    Optional<User> findByEmail(String email);
}