package com.resourcity.naama.resourcity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable
{

    private String pwd;

    private String email;

    public User(){} // Firebase needs a default constructor in order to work
    public User(String email)
    {
        this.email = email;
    }


    //*******   Getters   *******//

    public String getPwd() {
        return pwd;
    }

    public String getEmail() {
        return email;
    }


    //*******   Setters   *******//

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
