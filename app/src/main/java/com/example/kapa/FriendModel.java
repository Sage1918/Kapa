package com.example.kapa;

public class FriendModel {
    String user_id;
    String name;

    public FriendModel(String user_id, String name) {
        this.user_id = user_id;
        this.name = name;
    }

    FriendModel()
    {

    }


    public String getUser_id() {
        return user_id;
    }

    public String getName() {
        return name;
    }
}
