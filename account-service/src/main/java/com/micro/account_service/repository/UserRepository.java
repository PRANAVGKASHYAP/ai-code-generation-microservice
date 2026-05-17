package com.micro.account_service.repository;

import com.micro.account_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.nio.channels.FileChannel;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User , Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameIgnoreCase(String mail);
}
