package com.example.clientsideencryption.repository;

import com.example.clientsideencryption.domain.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, String> {
}
