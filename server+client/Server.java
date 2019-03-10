import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;


public class Server
{
    private HashMap<String[], PrintWriter> clientOutputStreams; // String[] is ip then host
    private EmojiFormatter ef;

    private JTextArea incoming;
    private JTextField outgoing;

    private int port;
    private String ip;

    private String myName = "SERVER";
    private String myIp;
    
    public class ClientHandler implements Runnable {
        BufferedReader reader;
        Socket sock;
        String[] ips;
        boolean nameSet = false;
        String preferredName;
        
        ClientHandler(String[] ips, Socket clientSocket) {
            try {
                this.ips = ips;

                this.sock = clientSocket;
                InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
                reader = new BufferedReader(isReader);

                Message joinMessage = new Message(null, this.ips[0], this.ips[1], new Date(), new String[]{"joining"});
                broadcastMessage(joinMessage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        public void run() {
            String jsonData;
            append(this.ips[1] + " (" + this.ips[0] + ")" + " Has Joined The Chat");
            try {
                while ((jsonData = reader.readLine()) != null) {
                    System.out.println(jsonData);
                    Message m = Message.fromJson(jsonData);

                    m.commands = new String[0]; // removes all commands from the client's message, don't want them clearing tha chat!

                    m.fromIp = this.ips[0]; // set ip to their actual ip

                    if(!nameSet) {
                        //preferredName = this.ips[1]; //rename client's preferred name to their hostname
                        preferredName = m.fromName; // set preferred name to their first message's from name
                        nameSet = true;
                    }
                    m.fromName = preferredName;

                    m.date = new Date();

                    if (m.text == null) {
                        continue;
                    }

                    //System.out.println("read " + m.text + " from " + m.from);
                    append("[ " + new SimpleDateFormat(Globals.dateFormat).format(m.date) + " ] " + m.fromName + ": "+m.text);
                    broadcastMessage(m);
                }
            }catch(SocketException se){
                /*probably leaving*/
                //se.printStackTrace();
                Message joinMessage = new Message(null, this.ips[0], this.ips[1], new Date(), new String[]{"leaving"});
                broadcastMessage(joinMessage);
                append(this.ips[1] + " (" + this.ips[0] + ") " + " Has Left The Chat");
                clientOutputStreams.remove(ips);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(Globals.serverTheme);
        }catch(Exception e){
            e.printStackTrace();
        }
        new Server().go();
    }

    class MainWindowListener implements WindowListener {

        public void windowClosing(WindowEvent arg0) {
            Message m = new Message(null, null, null, null, new String[]{"exit"});
            broadcastMessage(m);
            System.exit(0);
        }
        public void windowOpened(WindowEvent arg0) {}
        public void windowClosed(WindowEvent arg0) {}
        public void windowIconified(WindowEvent arg0) {}
        public void windowDeiconified(WindowEvent arg0) {}
        public void windowActivated(WindowEvent arg0) {}
        public void windowDeactivated(WindowEvent arg0) {}

    }

    private void buildGui(){
        port = Globals.port;
        ip = Globals.ip;
        int[] windowArea = Globals.serverArea;

        try {
            myIp = InetAddress.getLocalHost().getHostAddress();
        }catch(UnknownHostException ex){
            myIp = Globals.defaultIp;
        }

        JLabel infoLabel = new JLabel("<html>IP: <b>"+myIp+"</b><br>Port: <b>"+port+"</b></html>");

        JFrame frame = new JFrame(Globals.appName+" Server");
        frame.addWindowListener(new MainWindowListener());
        JPanel mainPanel = new JPanel();
        incoming = new JTextArea(15, 50);
        incoming.setLineWrap(true);
        incoming.setWrapStyleWord(true);
        incoming.setEditable(false);
        JScrollPane qScroller = new JScrollPane(incoming);
        qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        outgoing = new JTextField(20);

        JButton sendButton = new JButton("Send");
        JButton clearClientsButton = new JButton("Clear Clients");
        JButton clearServerButton = new JButton("Clear This");

        sendButton.addActionListener(new SendButtonListener());
        clearClientsButton.addActionListener(new ClearClientsListener());
        clearServerButton.addActionListener(new ClearDisplayListener());

        frame.getRootPane().setDefaultButton(sendButton);

        DefaultCaret caret = (DefaultCaret)incoming.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

        mainPanel.add(infoLabel);
        mainPanel.add(qScroller);
        mainPanel.add(outgoing);
        mainPanel.add(sendButton);
        mainPanel.add(clearClientsButton);
        mainPanel.add(clearServerButton);

        frame.getContentPane().add(BorderLayout.CENTER, mainPanel);

        frame.setSize(windowArea[0], windowArea[1]);
        frame.setVisible(true);
    }

    public class SendButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent ev) {
            String text = outgoing.getText();
            Message m = new Message(text, myIp, myName, new Date(), new String[0]);
            broadcastMessage(m);
            outgoing.setText("");
            outgoing.requestFocus();
            append("[ " + new SimpleDateFormat(Globals.dateFormat).format(m.date) + " ] " + m.fromName + ": "+m.text);
        }
    }

    public class ClearDisplayListener implements ActionListener {
        public void actionPerformed(ActionEvent ev) {
            incoming.setText(null);
        }
    }

    public class ClearClientsListener implements ActionListener {
        public void actionPerformed(ActionEvent ev) {
            Message m = new Message(null, null, null, null, new String[]{"clear"});
            broadcastMessage(m);
        }
    }

    private void append(String text){
        text = text+"\n";
        try {
            incoming.getDocument().insertString(0, text, null);
        }catch (BadLocationException ble){
            ble.printStackTrace();
        }
    }
    
    private void go() {
        clientOutputStreams = new HashMap<>();
        ef = new EmojiFormatter();
        buildGui();
        try {
            InetAddress addr = InetAddress.getByName(ip);
            ServerSocket serverSock = new ServerSocket(port, 0, addr);
            while(true) {
                Socket clientSocket = serverSock.accept();
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());

                String ipConnected = clientSocket.getInetAddress().toString().substring(1);
                String hostConnected = clientSocket.getInetAddress().getHostName();

                String[] outString = new String[]{ipConnected, hostConnected};

                clientOutputStreams.put(outString, writer);
                
                Thread t = new Thread(new ClientHandler(outString, clientSocket));
                t.start();
                System.out.println("New Client Connected");
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void broadcastMessage(Message message) {
        broadcastMessage(message, this.clientOutputStreams.keySet());
    }

    private void broadcastMessage(Message message, Set<String[]> recipients){
        String json = message.toJson();
        broadcastMessage(json, recipients);
    }

    private void broadcastMessage(String msg){
        broadcastMessage(msg, this.clientOutputStreams.keySet());
    }

    private void broadcastMessage(String msg, Set<String[]> recipients){
        for (String[] ips : clientOutputStreams.keySet()){
            try {
                if(recipients.contains(ips)) {
                    PrintWriter writer = clientOutputStreams.get(ips);
                    writer.println(msg);
                    writer.flush();
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }
}
