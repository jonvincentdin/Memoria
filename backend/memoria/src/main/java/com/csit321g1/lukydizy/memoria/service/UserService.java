package com.csit321g1.lukydizy.memoria.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.csit321g1.lukydizy.memoria.dto.LoginRequest;
import com.csit321g1.lukydizy.memoria.dto.RegisterRequest;
import com.csit321g1.lukydizy.memoria.dto.UserProfile;
import com.csit321g1.lukydizy.memoria.entity.UserEntity;
import com.csit321g1.lukydizy.memoria.exception.EmailAlreadyUsedException;
import com.csit321g1.lukydizy.memoria.exception.InvalidCredentialsException;
import com.csit321g1.lukydizy.memoria.exception.InvalidRegistrationException;
import com.csit321g1.lukydizy.memoria.repository.UserRepository;

@Service
public class UserService {

    private static final int BCRYPT_MAX_BYTES = 72;

    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    public UserProfile register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (repo.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyUsedException();
        }
        if (request.password().getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
            throw new InvalidRegistrationException("Password must be no more than 72 UTF-8 bytes.");
        }

        UserEntity user = new UserEntity(
            request.name().trim(),
            normalizedEmail,
            passwordEncoder.encode(request.password()),
            LocalDateTime.now(ZoneOffset.UTC)
        );
        try {
            return toProfile(repo.saveAndFlush(user));
        } catch (DataIntegrityViolationException exception) {
            if (repo.existsByEmail(normalizedEmail)) {
                throw new EmailAlreadyUsedException();
            }
            throw exception;
        }
    }

    public UserEntity authenticate(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        String password = request.password();
        if (password == null || password.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
            throw new InvalidCredentialsException();
        }

        UserEntity user = repo.findByEmail(normalizedEmail)
            .filter(candidate -> passwordEncoder.matches(password, candidate.getPasswordHash()))
            .orElseThrow(InvalidCredentialsException::new);
        return user;
    }

    public UserProfile getProfile(String id) {
        return toProfile(readById(id));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private UserProfile toProfile(UserEntity user) {
        return new UserProfile(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
    }

    public List<UserEntity> readAll() {
        return repo.findAll();
    }

    public UserEntity readById(String id) {
        return repo.findById(id).orElseThrow(() ->
            new RuntimeException("User not found with id:")
        );
    }

    public UserEntity update(String id, UserEntity newDetails) {
        UserEntity userEntity = readById(id);
        userEntity.setName(newDetails.getName());
        userEntity.setEmail(newDetails.getEmail());
        return repo.save(userEntity);
    }

    public String delete(String id) {
        UserEntity userEntity = readById(id);
        repo.delete(userEntity);
        return "Succesfully deleted user: " + id;
    }
}
