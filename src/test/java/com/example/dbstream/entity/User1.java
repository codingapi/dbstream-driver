package com.example.dbstream.entity;

import javax.persistence.*;

import lombok.Data;


@Data
@Entity
@Table(name = "m_user_1")
public class User1 {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String email;

    private String nickname;

}

