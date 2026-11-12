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
import com.csit321g1.lukydizy.memoria.exception.UserNotFoundException;
import com.csit321g1.lukydizy.memoria.repository.UserRepository;

// Service bean: holds the business logic between the controllers and the repository.
@Service
public class UserService {

    // BCrypt only uses the first 72 bytes of a password, so longer ones are rejected.
    private static final int BCRYPT_MAX_BYTES = 72;

    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;

    /** Creates the service with its user repository and password encoder. */
    public UserService(UserRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    /** Sets the creation time and saves a new user. */
    public UserEntity create(UserEntity user) {
        // The server sets the creation time; a client-sent value is ignored.
        user.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        // INSERT into users.
        return repo.save(user);
    }

    /** Registers a user with a normalized email and a hashed password. */
    public UserProfile register(RegisterRequest request) {
        // Lowercase and trim so Mina@Example.com and mina@example.com are the same account.
        String normalizedEmail = normalizeEmail(request.email());
        // Duplicate email -> 409 Conflict.
        if (repo.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyUsedException();
        }
        if (request.password().getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
            throw new InvalidRegistrationException("Password must be no more than 72 UTF-8 bytes.");
        }

        // Store a BCrypt hash of the password, never the password itself.
        UserEntity user = new UserEntity(
            request.name().trim(),
            normalizedEmail,
            passwordEncoder.encode(request.password()),
            LocalDateTime.now(ZoneOffset.UTC)
        );
        try {
            // saveAndFlush runs the INSERT now so a unique-email clash is caught here.
            return toProfile(repo.saveAndFlush(user));
        // Two sign-ups with the same email at the same moment: report it as a duplicate.
        } catch (DataIntegrityViolationException exception) {
            if (repo.existsByEmail(normalizedEmail)) {
                throw new EmailAlreadyUsedException();
            }
            throw exception;
        }
    }

    /** Finds a user by email and verifies the supplied password. */
    public UserEntity authenticate(LoginRequest request) {
        // Use the same normalized email that registration stored.
        String normalizedEmail = normalizeEmail(request.email());
        String password = request.password();
        if (password == null || password.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
            throw new InvalidCredentialsException();
        }

        // Wrong email and wrong password give the same error, so attackers can't tell which accounts exist.
        return repo.findByEmail(normalizedEmail)
            .filter(candidate -> passwordEncoder.matches(password, candidate.getPasswordHash()))
            .orElseThrow(InvalidCredentialsException::new);
    }

    /** Returns the public profile for a user ID. */
    public UserProfile getProfile(String id) {
        return toProfile(readById(id));
    }

    /** Normalizes an email address for consistent lookup and storage. */
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /** Converts a user entity to a response that excludes the password hash. */
    private UserProfile toProfile(UserEntity user) {
        return new UserProfile(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
    }

    /** Returns all users. */
    public List<UserEntity> readAll() {
        // SELECT * FROM users.
        return repo.findAll();
    }

    /** Finds a user by ID or throws when it does not exist. */
    public UserEntity readById(String id) {
        // Missing ID -> UserNotFoundException -> 404 via ApiExceptionHandler.
        return repo.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    /** Replaces the editable fields of an existing user. */
    public UserEntity update(String id, UserEntity newDetails) {
        // Load the existing row first (404 if it does not exist).
        UserEntity userEntity = readById(id);
        userEntity.setName(newDetails.getName());
        userEntity.setEmail(newDetails.getEmail());
        userEntity.setPasswordHash(newDetails.getPasswordHash());
        // save() on an existing ID runs an UPDATE.
        return repo.save(userEntity);
    }

    /** Deletes a user by ID. */
    public String delete(String id) {
        // Load first so a missing ID returns 404 instead of failing silently.
        UserEntity userEntity = readById(id);
        // DELETE FROM users WHERE user_id = ?
        repo.delete(userEntity);
        return "Successfully deleted user: " + id;
    }
}
