package com.example.cloudfilestorage.service;

import com.example.cloudfilestorage.Repository.UserRepository;
import com.example.cloudfilestorage.dto.UserDTO;
import com.example.cloudfilestorage.entity.User;
import com.example.cloudfilestorage.exception.UserAlreadyExistException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

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

    public User getUserById(Integer userId) {
        return userRepository.findById(userId);
    }
}
