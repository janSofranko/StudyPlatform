package org.example.studyplatform.service;

import org.example.studyplatform.entity.User;
import org.example.studyplatform.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // REGISTRATION
    public User register(String name, String email, String password) {

        if (userRepository.findByName(name).isPresent()) {
            throw new RuntimeException("Username is already taken");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email is already registered");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(password));

        return userRepository.save(user);
    }

    // LOGIN – username only
    public User login(String name, String rawPassword) {

        User user = userRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!encoder.matches(rawPassword, user.getPasswordHash())) {
            throw new RuntimeException("Invalid username or password");
        }

        return user;
    }

    // OTHERS
    public List<User> getAllUsers() { return userRepository.findAll(); }

    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }



    public User changePassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // heslo musí byť zahashované
        user.setPasswordHash(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        return userRepository.save(user);
    }

    public User changeEmail(Long id, String newEmail) {
        if (userRepository.findByEmail(newEmail).isPresent()) {
            throw new RuntimeException("Email already in use");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEmail(newEmail);
        return userRepository.save(user);
    }


}
