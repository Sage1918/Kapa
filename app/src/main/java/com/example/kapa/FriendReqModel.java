package com.example.kapa;

public class FriendReqModel {
    private String toUid;
    private String fromUid;
    private String fromUname;

    FriendReqModel()
    {

    }

    FriendReqModel(String tUid,String fUid,String fUname)
    {
        this.toUid = tUid;
        this.fromUid = fUid;
        this.fromUname = fUname;
    }

    public String getToUid() {
        return toUid;
    }

    public String getFromUid() {
        return fromUid;
    }

    public String getFromUname() {
        return fromUname;
    }
}
