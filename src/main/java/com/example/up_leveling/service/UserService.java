package com.example.up_leveling.service;

import com.example.up_leveling.dto.request.user.SaveUserDTO;
import com.example.up_leveling.dto.request.user.UpdateStatusUserDTO;
import com.example.up_leveling.dto.request.user.UpdateUserDTO;
import com.example.up_leveling.entity.Status;
import com.example.up_leveling.entity.User;
import com.example.up_leveling.repository.UserRepository;
import org.hibernate.sql.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Integer id) {
        return userRepository.findById(id)
                .filter(u -> !u.getStatus().equals(Status.DELETED))
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado!"));
    }

    public User save(SaveUserDTO request) {

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());

        return userRepository.save(user);
    }

    public User update(Integer id, UpdateUserDTO request) {
        User user = userRepository.findById(id)
                .filter(u -> !u.getStatus().equals(Status.DELETED))
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado!"));

        if (!request.getName().isBlank()) {
            user.setName(request.getName());
        }
        if (!request.getEmail().isBlank()) {
            user.setEmail(request.getEmail());
        }
        user.setId(id);
        return userRepository.save(user);
    }

    public User updateStatus(Integer id, UpdateStatusUserDTO request) {
        User user = userRepository.findById(id)
                .filter(u -> !u.getStatus().equals(Status.DELETED))
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado!"));

        Status status = Status.fromString(request.getStatus());
        user.setStatus(status);
        return userRepository.save(user);
    }

    public void delete(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado!"));

        user.setStatus(Status.DELETED);
        userRepository.save(user);
    }
}
