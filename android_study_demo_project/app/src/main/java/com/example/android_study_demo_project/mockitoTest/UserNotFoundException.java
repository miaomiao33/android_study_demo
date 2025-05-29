package com.example.android_study_demo_project.mockitoTest;

public class UserNotFoundException extends RuntimeException {
//用户名未找到的异常类
    public UserNotFoundException(String message) {
        super(message);
    }

}
