package com.example.dbstream.entity;

import lombok.Data;

import jakarta.persistence.*;

@Data
@Entity
@Table(name = "m_user_2")
public class User2 {

    @Id
    private long id;

    private String username;

    private String password;

    private String email;

    private String nickname;

}

