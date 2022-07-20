package com.example.kapa;

public class FriendsModel {
    public String name;
    public String fb_id;
    public String friendUid;

    public FriendsModel() {
    }

    public FriendsModel(String name, String fb_id, String fruid) {
        this.name = name;
        this.fb_id = fb_id;
        friendUid = fruid;
    }

    public String getName() {
        return name;
    }

    public String getFb_id() {
        return fb_id;
    }

    public String getFriendUid() {
        return friendUid;
    }
}
