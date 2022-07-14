package com.example.kapa;

public class FriendsModel {
    public String name;
    public String fb_id;
    public String Uid;

    public FriendsModel() {
    }

    public FriendsModel(String name, String fb_id, String uid) {
        this.name = name;
        this.fb_id = fb_id;
        Uid = uid;
    }

    public String getName() {
        return name;
    }

    public String getFb_id() {
        return fb_id;
    }

    public String getUid() {
        return Uid;
    }
}
