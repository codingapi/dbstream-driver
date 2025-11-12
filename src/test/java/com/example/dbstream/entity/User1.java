package com.example.dbstream.entity;

import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "m_user_1")
public class User1 {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "user_seq", allocationSize = 50)
    private Long id;

    private String username;

    private String password;

    private String email;

    private String nickname;

}

