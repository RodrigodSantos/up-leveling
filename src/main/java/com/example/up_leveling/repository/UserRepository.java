package com.example.up_leveling.repository;

import com.example.up_leveling.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("SELECT COALESCE(SUM(t.xpAmount),0) FROM XpTransaction t WHERE t.user.id = :userId")
    Integer getTotalXp(@Param("userId") Integer userId);
}
