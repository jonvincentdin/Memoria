package com.csit321g1.lukydizy.memoria.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.csit321g1.lukydizy.memoria.entity.UserEntity;
import com.csit321g1.lukydizy.memoria.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository repo;

    public UserService() {        
    }

    //CRUD

    //CREATE
    public UserEntity create(UserEntity userEntity) {
        return repo.save(userEntity);
    }

    //READ
    public List<UserEntity> readAll() {
        return repo.findAll();
    }
    public UserEntity readById(String id) {
        return repo.findById(id).orElseThrow(() ->
            new RuntimeException("User not found with id:")
        );
    }

    //UPDATE
    public UserEntity update(String id, UserEntity newDetails) {
        UserEntity userEntity = readById(id);
        userEntity.setName(newDetails.getName());
        userEntity.setEmail(newDetails.getEmail());
        userEntity.setPasswordHash(newDetails.getPasswordHash());
        return repo.save(userEntity);
    }

    //DELETE
    public String delete(String id) {
        UserEntity userEntity = readById(id);
        repo.delete(userEntity);
        return "Succesfully deleted user: " + id;
    }

}
