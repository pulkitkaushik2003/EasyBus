package com.example.ticket.Admin.Repository;

import com.example.ticket.Admin.Model.LoginAdminuser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface Adminrepo extends JpaRepository<LoginAdminuser,Integer > {
    LoginAdminuser findByUsername(String username);

}
