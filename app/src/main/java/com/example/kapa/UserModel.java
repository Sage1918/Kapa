package com.example.kapa;

import java.util.ArrayList;

public class UserModel {
    public int uid;
    public String uname;
    public LoginTypeModel lt;
    public String mode;
    public ArrayList<UserModel> friends;
    public int score;

    public UserModel()
    {

    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public LoginTypeModel getLt() {
        return lt;
    }

    public void setLt(LoginTypeModel lt) {
        this.lt = lt;
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

    public ArrayList<UserModel> getFriends() {
        return friends;
    }

    public void setFriends(ArrayList<UserModel> friends) {
        this.friends = friends;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
