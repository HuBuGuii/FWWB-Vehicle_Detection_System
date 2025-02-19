package com.fwwb.vehicledetection.repository;

import com.fwwb.vehicledetection.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 用户名模糊搜索
    List<User> findByRealNameContaining(String keyword);

    // 分页查询所有用户
    @Query("SELECT  u FROM  User  u")
    Page<User> findAllUsers(Pageable pageable);
}
