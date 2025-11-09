package com.example.dbstream.repository;

import com.example.dbstream.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {

    User getUserById(Long id);

    @Modifying
    @Query("update User set password = ?1")
    int resetPassword(String password);

    @Modifying
    @Query("update User set password = ?1 where username = ?2")
    int updatePasswordByUsername(String password, String username);

    @Modifying
    @Query("delete from User where username = ?1")
    int deleteByUsername(String username);

    @Query("select count(u) from User as u")
    int counts();

    @Modifying
    @Query("insert into User(email,username,password,nickname) select u.email,u.username,u.password,u.nickname from User u")
    int insertIntoFromSelect();
}

