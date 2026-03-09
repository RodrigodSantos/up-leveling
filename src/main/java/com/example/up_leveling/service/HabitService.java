package com.example.up_leveling.service;

import com.example.up_leveling.dto.request.habit.SaveHabitDTO;
import com.example.up_leveling.dto.response.SuccessDTO;
import com.example.up_leveling.dto.response.habit.HabitDTO;
import com.example.up_leveling.entity.Habit;
import com.example.up_leveling.entity.StatusType;
import com.example.up_leveling.entity.User;
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

    public List<Habit> findAll(){
        return habitRepository.findAll();
    }

    public HabitDTO findById(Integer id){
        Habit habit = habitRepository.findById(id)
                .filter(u -> !u.getStatus().equals(StatusType.DELETED))
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

        User user = userRepository.findById(request.getUserId())
                .filter(u -> !u.getStatus().equals(StatusType.DELETED))
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        habit.setUser(user);
        habitRepository.save(habit);
        return new SuccessDTO("Hábito cadastrado", habit);

    }
}
