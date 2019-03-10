import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Date;

class Message {
    String text;
    String fromIp;
    String fromName;
    Date date;
    String[] commands;
    private transient Gson g;

    void initGson(){
        g = new Gson();
        /*GsonBuilder builder = new GsonBuilder();
        g = builder.serializeNulls().create();*/
    }

    Message(String text, String fromIp, String fromName, Date date, String[] commands){
        this.text = text;
        this.fromIp = fromIp;
        this.fromName = fromName;
        this.date = date;
        this.commands = commands;

        initGson();
    }

    Message(){
        initGson();
    }

    String toJson(){
        return g.toJson(this);
    }

    static Message fromJson(String jsonData){
        return new Gson().fromJson(jsonData, Message.class);
    }
}
