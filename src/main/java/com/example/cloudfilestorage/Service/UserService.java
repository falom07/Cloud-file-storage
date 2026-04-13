package com.example.cloudfilestorage.Service;

import com.example.cloudfilestorage.DTO.UserDTO;
import com.example.cloudfilestorage.Entity.User;
import com.example.cloudfilestorage.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    public void createUser(UserDTO userDTO){
        if(userRepository.findByUsername(userDTO.username()).isPresent()){
            throw new RuntimeException("User already exists");
        }

        User user = new User(
                userDTO.username(),
                encoder.encode(userDTO.password())
        );

        userRepository.save(user);
    }
}
