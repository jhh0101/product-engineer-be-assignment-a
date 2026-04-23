package com.github.jhh0101.assignment.domain.user.repository;

import com.github.jhh0101.assignment.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
