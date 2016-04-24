package com.tominc.buthatke;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by shubham on 21/4/16.
 */
public class MessageThread implements Serializable {
    String name;
    ArrayList<Message> messages = new ArrayList<>();
    String msgToShow;

    public String getMsgToShow() {
        return msgToShow;
    }

    public void setMsgToShow(String msgToShow) {
        this.msgToShow = msgToShow;
    }

    public void addMessage(Message msg){
        messages.add(msg);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }
}
