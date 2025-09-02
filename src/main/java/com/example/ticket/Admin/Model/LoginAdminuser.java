package com.example.ticket.Admin.Model;


import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Table(name="loginuserdata")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginAdminuser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private  String role;
    private String username;
    private String password;
    private String email;
    private String phone;
    
}
