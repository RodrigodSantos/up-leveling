package com.example.up_leveling.service;

import com.example.up_leveling.dto.request.UpdateStatusDTO;
import com.example.up_leveling.dto.request.habit.SaveHabitDTO;
import com.example.up_leveling.dto.request.habit.UpdateHabitDTO;
import com.example.up_leveling.dto.response.SuccessDTO;
import com.example.up_leveling.dto.response.habit.HabitDTO;
import com.example.up_leveling.entity.Habit;
import com.example.up_leveling.entity.Status;
import com.example.up_leveling.entity.User;
import com.example.up_leveling.exception.BadRequestException;
import com.example.up_leveling.exception.NotFoundException;
import com.example.up_leveling.repository.HabitRepository;
import com.example.up_leveling.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HabitService {

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Habit> findAll() {
        return habitRepository.findAll();
    }

    public HabitDTO findById(Integer id) {
        Habit habit = habitRepository.findById(id)
                .filter(u -> !u.getStatus().equals(Status.DELETED))
                .orElseThrow(() -> new NotFoundException("Hábito não encontrado"));

        return HabitDTO.builder()
                .id(habit.getId())
                .name(habit.getName())
                .xpReward(habit.getXpReward())
                .status(habit.getStatus())
                .schedules(habit.getSchedules())
                .build();

    }

    public SuccessDTO save(SaveHabitDTO request) {
        Habit habit = new Habit();

        habit.setName(request.getName());
        habit.setXpReward(request.getXpReward());
        habit.setStatus(Status.ACTIVE);

        User user = userRepository.findById(request.getUserId())
                .filter(u -> !u.getStatus().equals(Status.DELETED))
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        habit.setUser(user);
        habitRepository.save(habit);
        return new SuccessDTO("Hábito cadastrado", habit);

    }

    public SuccessDTO update(Integer id, UpdateHabitDTO request) {
        Habit habit = habitRepository.findById(id)
                .filter(u -> !u.getStatus().equals(Status.DELETED))
                .orElseThrow(() -> new NotFoundException("Hábito não encontrado"));

        if (!request.getName().isBlank()) {
            habit.setName(request.getName());
        }
        if (request.getXpReward() != null) {
            habit.setXpReward(request.getXpReward());
        }

        habitRepository.save(habit);
        return new SuccessDTO("Hábito atualizado", habit);

    }

    public SuccessDTO updateStatus(Integer id, UpdateStatusDTO request) {
        Habit habit = habitRepository.findById(id)
                .filter(u -> !u.getStatus().equals(Status.DELETED))
                .orElseThrow(() -> new NotFoundException("Hábito não encontrado"));

        Status status = Status.fromString(request.getStatus());
        if (status == habit.getStatus()) {
            throw new BadRequestException("O novo status não pode ser igual ao anterior");
        }
        habit.setStatus(status);
        habitRepository.save(habit);
        return new SuccessDTO("Status do hábito atualizado", HabitDTO.fromEntity(habit));

    }

    public void delete(Integer id) {
        Habit habit = habitRepository.findById(id)
                .filter(u -> !u.getStatus().equals(Status.DELETED))
                .orElseThrow(() -> new NotFoundException("Hábito não encontrado"));

        habit.setStatus(Status.DELETED);
        habitRepository.save(habit);
    }

}
