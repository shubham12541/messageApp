package com.tominc.buthatke;

import java.io.Serializable;

/**
 * Created by shubham on 20/4/16.
 */
public class Message implements Serializable {
    String Name;
    String msg;
    String timestamp;
    boolean isSend;

    public boolean isSend() {
        return isSend;
    }

    public void setIsSend(boolean isSend) {
        this.isSend = isSend;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getMsg() {
        return msg;
    }


    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
