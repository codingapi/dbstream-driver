package com.example.dbstream.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "m_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String username;

    private String password;

    private String email;

    private String nickname;

}

