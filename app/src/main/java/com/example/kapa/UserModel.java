package com.example.kapa;

public class UserModel {
    public String userStrId;
    public String uname;
    public String mode;
    public int score;

    public UserModel()
    {

    }

    public String getUserStrId() {
        return userStrId;
    }

    public void setUserStrId(String userStrId) {
        this.userStrId = userStrId;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
