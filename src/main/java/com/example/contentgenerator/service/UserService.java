package com.example.contentgenerator.service;

import com.example.contentgenerator.model.User;
import com.example.contentgenerator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User processOAuthPostLogin(String email, String name, String provider) {
        User existUser = userRepository.findByEmail(email);

        if (existUser == null) {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setProvider(provider);

            return userRepository.save(newUser);
        }

        return existUser;
    }
}
