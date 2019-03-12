package com.RhysChat;

import com.google.gson.Gson;
import java.util.Date;

class Message implements Cloneable{
    String text;
    String fromIp;
    String fromName;
    Date date;
    String[] commands;
    private transient Gson g;
    private transient EmojiFormatter ef;

    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    void init(){
        g = new Gson();
        ef = new EmojiFormatter();
        /*GsonBuilder builder = new GsonBuilder();
        g = builder.serializeNulls().create();*/
    }

    Message(String text, String fromIp, String fromName, Date date, String[] commands){
        this.text = text;
        this.fromIp = fromIp;
        this.fromName = fromName;
        this.date = date;
        this.commands = commands;

        init();
    }

    Message(){
        init();
    }

    String toJson(){
        Message m = new Message();
        try {
            m = (Message) this.clone();
        }catch(CloneNotSupportedException e){
            e.printStackTrace();
        }
        m.text = ef.toPlainText(m.text);
        return g.toJson(m);
    }

    static Message fromJson(String jsonData){
        Message m = new Gson().fromJson(jsonData, Message.class);
        m.text = new EmojiFormatter().toEmoji(m.text);
        return m;
    }
}
