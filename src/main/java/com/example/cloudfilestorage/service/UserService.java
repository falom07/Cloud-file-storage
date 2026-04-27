package com.example.cloudfilestorage.service;

import com.example.cloudfilestorage.dto.UserDTO;
import com.example.cloudfilestorage.entity.User;
import com.example.cloudfilestorage.exception.UserAlreadyExistException;
import com.example.cloudfilestorage.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    public void createUser(UserDTO userDTO){
        if(userRepository.findByUsername(userDTO.username()).isPresent()){
            throw new UserAlreadyExistException("User already exists");
        }

        User user = new User(
                userDTO.username(),
                encoder.encode(userDTO.password())
        );

        userRepository.save(user);
    }

    public Integer getUserIdByName(String ownerName) {
        Optional<User> user = userRepository.findByUsername(ownerName);
        if(user.isPresent()){
            return user.get().getId();
        } else {
            throw new RuntimeException("No found user with username: " + ownerName);
        }
    }

    public User getUserById(Integer userId) {
        return userRepository.findById(userId);
    }
}
