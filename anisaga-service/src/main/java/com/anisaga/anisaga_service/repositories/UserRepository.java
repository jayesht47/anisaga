package com.anisaga.anisaga_service.repositories;

import com.anisaga.anisaga_service.entities.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    public Optional<User> findByUserName(String userName);
}
