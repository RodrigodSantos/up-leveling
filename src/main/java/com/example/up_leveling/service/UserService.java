package com.example.up_leveling.service;

import com.example.up_leveling.dto.request.user.SaveUserDTO;
import com.example.up_leveling.dto.request.user.UpdateStatusUserDTO;
import com.example.up_leveling.dto.request.user.UpdateUserDTO;
import com.example.up_leveling.dto.response.SuccessDTO;
import com.example.up_leveling.dto.response.user.UserDTO;
import com.example.up_leveling.entity.Status;
import com.example.up_leveling.entity.User;
import com.example.up_leveling.exception.BadRequestException;
import com.example.up_leveling.exception.NotFoundException;
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

    public UserDTO findById(Integer id) {

        User user = userRepository.findById(id)
                .filter(u -> !u.getStatus().equals(Status.DELETED))
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado!"));

        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .status(user.getStatus())
                .xpTotal(userRepository.getTotalXp(user.getId()))
                .build();
    }

    public SuccessDTO save(SaveUserDTO request) {

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        userRepository.save(user);

        return new SuccessDTO("Usuário criado", UserDTO.fromEntity(user));
    }

    public SuccessDTO update(Integer id, UpdateUserDTO request) {
        User user = userRepository.findById(id)
                .filter(u -> !u.getStatus().equals(Status.DELETED))
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado!"));

        if (!request.getName().isBlank()) {
            user.setName(request.getName());
        }
        if (!request.getEmail().isBlank()) {
            user.setEmail(request.getEmail());
        }
        user.setId(id);
        userRepository.save(user);
        return new SuccessDTO("Usuário atualizado", UserDTO.fromEntity(user));
    }

    public SuccessDTO updateStatus(Integer id, UpdateStatusUserDTO request) {
        User user = userRepository.findById(id)
                .filter(u -> !u.getStatus().equals(Status.DELETED))
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado!"));

        Status status = Status.fromString(request.getStatus());
        if (status == user.getStatus()) {
            throw new BadRequestException("O novo status não pode ser igual ao anterior");
        }
        user.setStatus(status);
        userRepository.save(user);
        return new SuccessDTO("Status do usuário atualizado", UserDTO.fromEntity(user));
    }

    public void delete(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado!"));

        user.setStatus(Status.DELETED);
        userRepository.save(user);
    }
}
