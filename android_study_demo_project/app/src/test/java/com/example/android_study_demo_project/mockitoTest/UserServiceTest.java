package com.example.android_study_demo_project.mockitoTest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Before
    public void setup() {
        //初始化对象
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetUserById_Success() {
        // 准备测试数据
        User mockUser = new User(1L, "testUser");

        // 设定 Mock 行为
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(mockUser));

        // 执行测试
        User result = userService.getUserById(1L);

        // 验证结果
        assertEquals("testUser", result.getName());//返回的name是testUser，故不会报错
        verify(userRepository).findById(1L);//执行过一次，不报错
    }

    @Test(expected = UserNotFoundException.class)
    public void testGetUserById_NotFound() {
        when(userRepository.findById(2L))
                .thenReturn(Optional.empty());

        userService.getUserById(2L);//抛出的是UserNotFoundException异常
    }
}