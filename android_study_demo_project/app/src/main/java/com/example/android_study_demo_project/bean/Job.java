package com.example.android_study_demo_project.bean;

/**
 * Copyright 2023 json.cn
 */

import androidx.annotation.NonNull;

/**
 * Auto-generated: 2023-02-19 18:19:58
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
//序列化和反序列化bean类
public class Job {

    private String jobName;
    private int salary;
    public Job(String jobName, int salary)
    {
        this.jobName = jobName;
        this.salary = salary;

    }
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }
    public String getJobName() {
        return jobName;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }
    public int getSalary() {
        return salary;
    }

    @NonNull
    @Override
    public String toString() {
        return "jobName"+this.jobName+"salary"+this.salary;
    }
}