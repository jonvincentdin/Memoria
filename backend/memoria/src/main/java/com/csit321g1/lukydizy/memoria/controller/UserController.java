package com.csit321g1.lukydizy.memoria.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.csit321g1.lukydizy.memoria.entity.UserEntity;
import com.csit321g1.lukydizy.memoria.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService service;

    public UserController() {        
    }

    //CRUD

    //CREATE
    @PostMapping
    public UserEntity create(UserEntity userEntity) {
        return service.create(userEntity);
    }

    //READ
    @GetMapping
    public List<UserEntity> readAll() {
        return service.readAll();
    }
    
    @GetMapping("/{id}")
    public UserEntity readById(String id) {
        return service.readById(id);
    }

    //UPDATE
    @PutMapping("/{id}")
    public UserEntity update(String id, UserEntity newDetails) {
        return service.update(id, newDetails);
    }

    //DELETE
    @DeleteMapping("/{id}")
    public String delete(String id) {
        return service.delete(id);
    }

    @GetMapping("/test")
    public String print() {
        return "for testing, it works now";
    }

}
