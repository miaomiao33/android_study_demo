package com.example.android_study_demo_project.mockitoTest;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Long id); // 模拟从数据库中获取用户信息
    void save(User user);// 模拟保存用户到数据库
    void deleteById(Long id);// 模拟删除用户
}


