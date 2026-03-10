package com.example.up_leveling.controller;

import com.example.up_leveling.dto.request.UpdateStatusDTO;
import com.example.up_leveling.dto.request.user.SaveUserDTO;
import com.example.up_leveling.dto.request.user.UpdateUserDTO;
import com.example.up_leveling.dto.response.SuccessDTO;
import com.example.up_leveling.dto.response.user.UserDTO;
import com.example.up_leveling.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public List<UserDTO> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public UserDTO findById(@PathVariable Integer id) {
        return userService.findById(id);
    }

    @PostMapping("")
    public SuccessDTO save(@Valid @RequestBody SaveUserDTO request) {
        return userService.save(request);
    }

    @PutMapping("/{id}")
    public SuccessDTO update(
            @PathVariable(required = true) Integer id,
            @Valid @RequestBody UpdateUserDTO request
            ) {
        return userService.update(id, request);
    }

    @PatchMapping("/{id}")
    public SuccessDTO updateStatus(
            @PathVariable(required = true) Integer id,
            @RequestBody @Valid UpdateStatusDTO request
    ) {
        return userService.updateStatus(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        userService.delete(id);
    }
}
