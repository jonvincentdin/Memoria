package com.csit321g1.lukydizy.memoria.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    //READ
    @GetMapping
    public List<UserEntity> readAll() {
        return service.readAll();
    }
    
    @GetMapping("/{id}")
    public UserEntity readById(@PathVariable("id") String id) {
        return service.readById(id);
    }

    //UPDATE
    @PutMapping("/{id}")
    public UserEntity update(@PathVariable("id") String id, @RequestBody UserEntity newDetails) {
        return service.update(id, newDetails);
    }

    //DELETE
    @DeleteMapping("/{id}")
    public String delete(@PathVariable("id") String id) {
        return service.delete(id);
    }

    @GetMapping("/test")
    public String print() {
        return "for testing, it works now";
    }

}
