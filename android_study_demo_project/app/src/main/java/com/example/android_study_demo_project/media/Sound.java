package com.example.android_study_demo_project.media;

public class Sound {
    String name;
    int soundId;
    String absolutePath;

    public Sound(String name, int soundId,String absolutePath) {
        this.name = name;
        this.soundId = soundId;
        this.absolutePath = absolutePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSoundId() {
        return soundId;
    }

    public void setSoundId(int soundId) {
        this.soundId = soundId;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }
}
