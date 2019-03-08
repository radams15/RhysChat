import javax.swing.*;
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
    private ArrayList<PrintWriter> clientOutputStreams;
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
        
        ClientHandler(Socket clientSocket) {
            try {
                sock = clientSocket;
                InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
                reader = new BufferedReader(isReader);
                
            } catch (Exception ex) { ex.printStackTrace(); }
        }
        
        public void run() {
            String jsonData;
            try {
                while ((jsonData = reader.readLine()) != null) {
                    System.out.println(jsonData);
                    Message m = Message.fromJson(jsonData);

                    if(m.commands.length > 0){
                        for(String c : m.commands){
                            if(c.equals("leaving")){
                                incoming.append(m.fromName + "(" +m.fromIp +")" + " Has Left The Chat\n");
                                continue;
                            }else if(c.equals("joining")){
                                incoming.append(m.fromName + "(" +m.fromIp +")" + " Has Joined The Chat\n");
                                continue;
                            }else if(c.equals("null")){
                                continue;
                            }
                        }
                    }

                    //System.out.println("read " + m.text + " from " + m.from);
                    if(m.text == null){
                        continue;
                    }
                    incoming.append("[" + m.fromName + " at " + new SimpleDateFormat("hh:mm:ss a").format(m.date) + " ]: " +ef.toEmoji( m.text) + "\n");
                    tellEveryone(m);
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(SharedData.serverTheme);
        }catch(Exception e){
            e.printStackTrace();
        }
        new Server().go();
    }

    class MainWindowListener implements WindowListener {

        public void windowClosing(WindowEvent arg0) {
            Message m = new Message(null, null, null, null, new String[]{"exit"});
            tellEveryone(m);
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
        port = SharedData.port;
        ip = SharedData.ip;
        int[] windowArea = SharedData.serverArea;

        try {
            myIp = InetAddress.getLocalHost().getHostAddress();
        }catch(UnknownHostException ex){
            myIp = SharedData.defaultIp;
        }

        JLabel infoLabel = new JLabel("<html>IP: <b>"+myIp+"</b><br>Port: <b>"+port+"</b></html>");

        JFrame frame = new JFrame(SharedData.appName+" Server");
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
            tellEveryone(m);
            outgoing.setText("");
            outgoing.requestFocus();
            incoming.append("[" + m.fromName + " at " + new SimpleDateFormat("hh:mm:ss a").format(m.date) + " ]: " + ef.toEmoji(m.text) + "\n");
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
            tellEveryone(m);
        }
    }
    
    private void go() {
        clientOutputStreams = new ArrayList<>();
        ef = new EmojiFormatter();
        buildGui();
        try {
            InetAddress addr = InetAddress.getByName(ip);
            ServerSocket serverSock = new ServerSocket(port, 0, addr);
            while(true) {
                Socket clientSocket = serverSock.accept();
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
                clientOutputStreams.add(writer);
                
                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start();
                System.out.println("New Client Connected");
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void tellEveryone(Message message) {
        String json = message.toJson();
        for (Object p : clientOutputStreams){
            try {
                PrintWriter writer = (PrintWriter) p;
                writer.println(json);
                writer.flush();
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }
}
