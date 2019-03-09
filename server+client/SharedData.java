import javax.swing.*;

class SharedData {
    static String appName = "RhysChat";
    static int port = 3000;
    static String ip = "0.0.0.0";

    static String defaultHostname = "Anonymous";
    static String defaultIp = "Unknown";

    static String serverTheme = (
            //UIManager.getSystemLookAndFeelClassName() //System
            //UIManager.getCrossPlatformLookAndFeelClassName() //Metal
            //"com.jtattoo.plaf.aero.AeroLookAndFeel" //Aero
            //"com.jtattoo.plaf.acryl.AcrylLookAndFeel" //Acryl
            //"com.jtattoo.plaf.fast.FastLookAndFeel" //Fast
            //"com.jtattoo.plaf.hifi.HiFiLookAndFeel" //HiFi
            //"com.jtattoo.plaf.mint.MintLookAndFeel" //Mint
            //"com.jtattoo.plaf.noire.NoireLookAndFeel" //Noire
            "com.jtattoo.plaf.smart.SmartLookAndFeel" //Smart
            //"com.jtattoo.plaf.luna.LunaLookAndFeel" //Luna
            //"com.jtattoo.plaf.aluminium.AluminiumLookAndFeel" //Aluminium
            //"com.jtattoo.plaf.texture.TextureLookAndFeel" //Texture
            //"com.jtattoo.plaf.mcwin.McWinLookAndFeel" //Mac
            //"com.jtattoo.plaf.bernstein.BernsteinLookAndFeel" // Yellow
            //"com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel" //nimbus
    );

    static String clientTheme =  UIManager.getSystemLookAndFeelClassName(); //System

    static int[] clientArea = {650, 500};
    static int[] serverArea = {800, 600};
}
