import com.google.gson.Gson;

import java.util.Date;

class Message {
    String text;
    String fromIp;
    String fromName;
    Date date;
    String[] commands;
    private transient Gson g;

    Message(String text, String fromIp, String fromName, Date date, String[] commands){
        this.text = text;
        this.fromIp = fromIp;
        this.fromName = fromName;
        this.date = date;
        this.commands = commands;

        g = new Gson();
    }

    Message(){
        g = new Gson();
    }

    String toJson(){
        return g.toJson(this);
    }

    static Message fromJson(String jsonData){
        return new Gson().fromJson(jsonData, Message.class);
    }
}
