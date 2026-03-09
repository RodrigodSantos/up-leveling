package com.example.up_leveling.controller;

import com.example.up_leveling.dto.request.habit.SaveHabitDTO;
import com.example.up_leveling.dto.response.SuccessDTO;
import com.example.up_leveling.dto.response.habit.HabitDTO;
import com.example.up_leveling.entity.Habit;
import com.example.up_leveling.service.HabitService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/habit")
public class HabitController {

    @Autowired
    private HabitService habitService;

    @GetMapping()
    public List<Habit> findAll() {
        return habitService.findAll();
    }

    @GetMapping("/{id}")
    public HabitDTO findById(@PathVariable Integer id) {
        return habitService.findById(id);
    }

    @PostMapping("")
    public SuccessDTO save(@Valid @RequestBody SaveHabitDTO request) {
        return habitService.save(request);
    }
}
