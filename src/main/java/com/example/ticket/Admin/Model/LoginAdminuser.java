package com.example.ticket.Admin.Model;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
@Entity
@Table(name="loginuserdata")
public class LoginAdminuser {
    @jakarta.persistence.Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.AUTO)
    private int id;
    private  String role;
    private String username;
    private String password;
    private String email;
    private String phone;
    


    public LoginAdminuser() {
    }
    

    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }


    public String getRole() {
        return role;
    }


    public void setRole(String role) {
        this.role = role;
    }


    public String getUsername() {
        return username;
    }


    public void setUsername(String username) {
        this.username = username;
    }


    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }


    public String getEmail() {
        return email;
    }


    public void setEmail(String email) {
        this.email = email;
    }


    public String getPhone() {
        return phone;
    }


    public void setPhone(String phone) {
        this.phone = phone;
    }


    public LoginAdminuser(int id, String role, String username, String password, String email, String phone) {
        this.id = id;
        this.role = role;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
    }
}