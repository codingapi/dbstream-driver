package com.example.dbstream.repository;

import com.example.dbstream.entity.User2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface User2Repository extends JpaRepository<User2, Long> {

    User2 getUserById(Long id);

    @Modifying
    @Query("update User2 set password = ?1")
    int resetPassword(String password);

    @Modifying
    @Query("update User2 set password = ?1 where username = ?2")
    int updatePasswordByUsername(String password, String username);

    @Modifying
    @Query("delete from User2 where username = ?1")
    int deleteByUsername(String username);

    @Query("select count(u) from User2 as u")
    int counts();

    @Modifying
    @Query("update User2 set password = '123' where username = ?1")
    int resetPasswordByUsername1(String username);

    @Modifying
    @Query(value = "INSERT INTO m_user_2 (id,email,username,password,nickname) VALUES ((SELECT COALESCE(MAX(id), 0) + 1 FROM m_user_2),'123','123','123','123')",nativeQuery = true)
    int staticSave();


}

