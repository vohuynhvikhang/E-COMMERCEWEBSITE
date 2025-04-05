package com.poly.asm.daos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.poly.asm.entitys.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	// Tìm người dùng theo email
    User findByEmail(String email);

    // Tìm người dùng theo tên người dùng
    User findByUsername(String username);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.email = :email")
    void deleteByEmail(@Param("email") String email);

}
