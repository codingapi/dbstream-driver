package com.example.dbstream.repository;

import com.example.dbstream.entity.User3;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface User3Repository extends JpaRepository<User3, Long> {

    @Modifying
    @Query(value = "insert into m_user_3(id,email,username,password,nickname) values(default,?1,?1,?1,NOW())", nativeQuery = true)
    void insertNowValue(String name);


}

