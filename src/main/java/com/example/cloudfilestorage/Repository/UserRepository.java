package com.example.cloudfilestorage.Repository;

import com.example.cloudfilestorage.Entity.User;
import org.springframework.data.repository.Repository;

public interface UserRepository extends Repository<User, Long> {
    User findByUsername(String username);
}
