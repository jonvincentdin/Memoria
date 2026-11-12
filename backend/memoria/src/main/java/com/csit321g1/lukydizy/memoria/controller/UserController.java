package com.csit321g1.lukydizy.memoria.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.csit321g1.lukydizy.memoria.entity.UserEntity;
import com.csit321g1.lukydizy.memoria.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    /** Creates the controller with its user service. */
    public UserController(UserService service) {
        this.service = service;
    }

    /** Creates a user. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserEntity create(@RequestBody UserEntity user) {
        return service.create(user);
    }

    /** Returns all users. */
    @GetMapping
    public List<UserEntity> readAll() {
        return service.readAll();
    }

    /** Returns the user with the given ID. */
    @GetMapping("/{id}")
    public UserEntity readById(@PathVariable("id") String id) {
        return service.readById(id);
    }

    /** Updates the user with the given ID. */
    @PutMapping("/{id}")
    public UserEntity update(@PathVariable("id") String id, @RequestBody UserEntity newDetails) {
        return service.update(id, newDetails);
    }

    /** Deletes the user with the given ID. */
    @DeleteMapping("/{id}")
    public String delete(@PathVariable("id") String id) {
        return service.delete(id);
    }
}
