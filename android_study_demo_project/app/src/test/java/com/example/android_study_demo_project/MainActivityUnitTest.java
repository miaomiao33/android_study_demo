package com.example.android_study_demo_project;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import okhttp3.internal.Util;

public class MainActivityUnitTest {
    @Test
    public void setUp()
    {

    }

    @Test
    public void isEmpty()
    {
        //报错并中断
//        assertEquals(true, false);
        assertEquals(true, true);
    }
}
