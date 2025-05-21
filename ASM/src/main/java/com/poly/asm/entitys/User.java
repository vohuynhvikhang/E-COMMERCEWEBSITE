package com.poly.asm.entitys;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String email;
    private String fullname;
    private String phone;
    private String address;
    private String role;
    private Boolean active;
<<<<<<< HEAD
}
//dit me may Long lon` nhu con cac ma hay di ra gio
=======
}
>>>>>>> e7e5a2628c6779116d58777935f00e5a64eba72c
