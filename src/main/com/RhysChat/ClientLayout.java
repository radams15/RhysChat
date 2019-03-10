package com.RhysChat;

import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ClientLayout{
    private boolean production = true;
    private JTextPane incoming;
    private JPanel mainPanel;
    private JScrollPane qScroller;
    private JTextField outgoing;

    private BufferedReader reader;
    private PrintWriter writer;
    private String clientIp;
    private String clientHostname;
    private EmojiFormatter ef;

    private int port;
    private String serverIp;
    private JButton sendButton;

    class MainWindowListener implements WindowListener {

        public void windowClosing(WindowEvent arg0) {
            Message leaveMessage = new Message(null, clientIp, clientHostname, new Date(), new String[]{"leaving"});
            sendMessage(leaveMessage);
            System.exit(0);
        }

        public void windowOpened(WindowEvent arg0) {
            Message joinMessage = new Message(null, clientIp, clientHostname, new Date(), new String[]{"joining"});
            sendMessage(joinMessage);
        }
        public void windowClosed(WindowEvent arg0) {}
        public void windowIconified(WindowEvent arg0) {}
        public void windowDeiconified(WindowEvent arg0) {}
        public void windowActivated(WindowEvent arg0) {}
        public void windowDeactivated(WindowEvent arg0) {}

    }

    public void append(String text) {
        text = text+"\n";
        try {
            incoming.getDocument().insertString(0, text, null);
        }catch (BadLocationException ble){
            ble.printStackTrace();
        }
    }

    private void go() {
        if (production) {
            String address = JOptionPane.showInputDialog("Ip Address: (IP:PORT) [Default 0.0.0.0:3000]");
            if (address.equals("")) {
                port = Globals.port;
                serverIp = Globals.ip;
            } else {
                String[] splitAddress = address.split(":");
                serverIp = splitAddress[0];
                try {
                    port = Integer.valueOf(splitAddress[1]);
                } catch (IndexOutOfBoundsException e) {
                    port = Globals.port;
                }
            }
        }

        if(!setUpNetworking()){
            JOptionPane.showMessageDialog(null, ("No Server Running On "+serverIp+":"+port), "InfoBox: " + "No Such Server", JOptionPane.INFORMATION_MESSAGE);
            System.exit(1);
        }

        JFrame frame = new JFrame(Globals.appName+" Client");
        frame.addWindowListener(new MainWindowListener());

        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setSize(new Dimension(Globals.clientArea[0], Globals.clientArea[1]));

        Border border = mainPanel.getBorder();
        Border margin = new EmptyBorder(10,10,10,10);
        mainPanel.setBorder(new CompoundBorder(border, margin));

        sendButton.addActionListener(new SendButtonListener());

        /*incoming.setLineWrap(true);
        incoming.setWrapStyleWord(true);*/
        incoming.setEditable(false);
        incoming.setContentType("text/html");

        outgoing.setMinimumSize(new Dimension());

        DefaultCaret caret = (DefaultCaret)incoming.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

        frame.getRootPane().setDefaultButton(sendButton);
        frame.setVisible(true);

        try {
            clientHostname = InetAddress.getLocalHost().getHostName();
            clientIp = InetAddress.getLocalHost().getHostAddress();
        }catch(UnknownHostException e){
            clientHostname = Globals.defaultHostname;
            clientIp = Globals.defaultIp;
        }

        ef = new EmojiFormatter();

        Thread readerThread = new Thread(new IncomingReader());
        readerThread.start();

    }

    private boolean setUpNetworking() {
        try {
            Socket sock = new Socket(serverIp, port);
            InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
            reader = new BufferedReader(streamReader);
            writer = new PrintWriter(sock.getOutputStream());
            System.out.println("Networking Established");
            return true;
        }
        catch(IOException ex)
        {
            return false;
        }
    }

    private void sendMessage(Message m){
        String out = m.toJson();
        try {
            writer.println(out);
            writer.flush();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public class SendButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent ev) {
            String text = ef.toPlainText(outgoing.getText());
            Message m = new Message(text, clientIp, clientHostname, new Date(), new String[0]);
            sendMessage(m);
            outgoing.setText("");
            outgoing.requestFocus();
        }
    }

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(Globals.clientTheme);
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        ClientLayout app = new ClientLayout();
        app.go();
    }

    class IncomingReader implements Runnable {
        public void run() {
            String jsonData;
            try {
                while ((jsonData = reader.readLine()) != null) {
                    System.out.println(jsonData);
                    Message m = Message.fromJson(jsonData);

                    if(m.commands.length > 0){
                        for(String c : m.commands){
                            switch(c){
                                case "clear":
                                    incoming.setText(null);
                                    append("Server Cleared Chat");
                                    continue;
                                case "leaving":
                                    append(m.fromName + " Has Left The Chat");
                                    continue;
                                case "joining":
                                    append(m.fromName + " Has Joined The Chat");
                                    continue;
                                case "exit":
                                    //System.exit(0)
                            }
                        }
                    }

                    //System.out.println("client read " + m.text + " from " + m.from);
                    if(m.text == null){
                        continue;
                    }
                    append("[ " + new SimpleDateFormat(Globals.dateFormat).format(m.date) + " ] " + m.fromName + ": "+m.text);
                }
            } catch (IOException ex) {
                //ex.printStackTrace();
                append("Server Closed");
                sendButton.setEnabled(false);
                outgoing.setEnabled(false);
            }
        }
    }
}