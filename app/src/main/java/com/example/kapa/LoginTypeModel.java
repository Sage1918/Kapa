package com.example.kapa;

public class LoginTypeModel {
    public String userid;
    public String loginId;
    public String loginType;

    public LoginTypeModel() {

    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String my_uid) {
        this.userid = my_uid;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }
}
