package com.example.dbstream.repository;

import com.example.dbstream.entity.User1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface User1Repository extends JpaRepository<User1, Long> {

    User1 getUserById(Long id);

    @Modifying
    @Query("update User1 set password = ?1")
    int resetPassword(String password);

    @Modifying
    @Query("update User1 set password = ?1 where username = ?2")
    int updatePasswordByUsername(String password, String username);

    @Modifying
    @Query("delete from User1 where username = ?1")
    int deleteByUsername(String username);

    @Query("select count(u) from User1 as u")
    int counts();

    @Modifying
    @Query("delete from User1 where id > ?1")
    int clearData(long id);

    @Modifying
    @Query("insert into User1(email,username,password,nickname) select u.email,u.username,u.password,u.nickname from User1 u")
    void insertIntoFromSelect();


    @Modifying
    @Query("delete from User1")
    int deleteAllData();

}

