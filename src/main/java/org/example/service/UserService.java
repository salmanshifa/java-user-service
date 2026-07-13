package org.example.service;

import org.example.entity.UserEntity;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.example.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public List<User> findAll() {
        return userRepository.findAll().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id).map(this::toModel);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username).map(this::toModel);
    }

    public Optional<User> authenticate(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(entity -> passwordEncoder.matches(password, entity.getPassword()))
                .map(this::toModel);
    }

    public User create(String username, String email, String mobileNumber, String password, boolean enabled, String role) {
        validateUniqueUserFields(username, email, mobileNumber, null);
        String encodedPassword = passwordEncoder.encode(password);
        UserEntity entity = userRepository.save(new UserEntity(username, email, mobileNumber, role, enabled, encodedPassword));
        return toModel(entity);
    }

    public Optional<User> update(Long id, String username, String email, String mobileNumber) {
        return userRepository.findById(id).map(entity -> {
            validateUniqueUserFields(username, email, mobileNumber, id);
            entity.setUsername(username);
            entity.setEmail(email);
            entity.setMobileNumber(mobileNumber);
            return toModel(userRepository.save(entity));
        });
    }

    private void validateUniqueUserFields(String username, String email, String mobileNumber, Long currentUserId) {
        userRepository.findByUsername(username)
                .filter(entity -> currentUserId == null || !entity.getId().equals(currentUserId))
                .ifPresent(entity -> {
                    throw new IllegalArgumentException("Username is already taken");
                });

        userRepository.findByEmail(email)
                .filter(entity -> currentUserId == null || !entity.getId().equals(currentUserId))
                .ifPresent(entity -> {
                    throw new IllegalArgumentException("Email is already registered");
                });

        userRepository.findByMobileNumber(mobileNumber)
                .filter(entity -> currentUserId == null || !entity.getId().equals(currentUserId))
                .ifPresent(entity -> {
                    throw new IllegalArgumentException("Mobile number is already registered");
                });
    }

    public boolean delete(Long id) {
        if (!userRepository.existsById(id)) {
            return false;
        }
        userRepository.deleteById(id);
        return true;
    }

    public long count() {
        return userRepository.count();
    }

    public String generateJwtToken(User user) {
        return jwtService.generateToken(user);
    }

    private User toModel(UserEntity entity) {
        return new User(entity.getId(), entity.getUsername(), entity.getEmail(), entity.getMobileNumber(), entity.getRole(), entity.isEnabled());
    }
}
