package com.example.up_leveling.controller;

import com.example.up_leveling.dto.request.user.SaveUserDTO;
import com.example.up_leveling.dto.request.user.UpdateStatusUserDTO;
import com.example.up_leveling.dto.request.user.UpdateUserDTO;
import com.example.up_leveling.entity.User;
import com.example.up_leveling.service.UserService;
import jakarta.validation.Path;
import jakarta.validation.Valid;
import org.hibernate.sql.model.PreparableMutationOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public List<User> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable Integer id) {
        return userService.findById(id);
    }

    @PostMapping("")
    public User save(@Valid @RequestBody SaveUserDTO request) {
        return userService.save(request);
    }

    @PutMapping("/{id}")
    public User update(
            @PathVariable(required = true) Integer id,
            @Valid @RequestBody UpdateUserDTO request
            ) {
        return userService.update(id, request);
    }

    @PatchMapping("/{id}")
    public User updateStatus(
            @PathVariable(required = true) Integer id,
            @RequestBody @Valid UpdateStatusUserDTO request
    ) {
        return userService.updateStatus(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        userService.delete(id);
    }
}
