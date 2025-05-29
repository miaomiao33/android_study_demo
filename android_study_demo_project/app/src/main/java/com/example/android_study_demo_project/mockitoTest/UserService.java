package com.example.android_study_demo_project.mockitoTest;

import java.util.Optional;

public class UserService {
    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public void createUser(User user) {
        userRepository.save(user); // 调用 UserRepository 的方法 [^1]
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id); // 调用 UserRepository 的方法 [^1]
    }
}
