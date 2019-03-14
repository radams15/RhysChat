package com.RhysChat;

class Globals {
    static String appName = "Super Ultimate RhysChat";
    static int port = 3000;
    static String ip = "0.0.0.0";

    static String dateFormat = "hh:mm:ss:SSS a";

    static String defaultHostname = "Anonymous";
    static String defaultIp = "Unknown";

    static class Themes{
        static String system = javax.swing.UIManager.getSystemLookAndFeelClassName();
        static String metal = javax.swing.UIManager.getCrossPlatformLookAndFeelClassName();
        static String nimbus = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
        static String aero = "com.jtattoo.plaf.aero.AeroLookAndFeel";
        static String acryl = "com.jtattoo.plaf.acryl.AcrylLookAndFeel";
        static String fast = "com.jtattoo.plaf.fast.FastLookAndFeel";
        static String hifi = "com.jtattoo.plaf.hifi.HiFiLookAndFeel";
        static String mint = "com.jtattoo.plaf.mint.MintLookAndFeel";
        static String noire = "com.jtattoo.plaf.noire.NoireLookAndFeel";
        static String smart = "com.jtattoo.plaf.smart.SmartLookAndFeel";
        static String luna = "com.jtattoo.plaf.luna.LunaLookAndFeel";
        static String aluminium = "com.jtattoo.plaf.aluminium.AluminiumLookAndFeel";
        static String texture = "com.jtattoo.plaf.texture.TextureLookAndFeel";
        static String mac = "com.jtattoo.plaf.mcwin.McWinLookAndFeel";
        static String yellow = "com.jtattoo.plaf.bernstein.BernsteinLookAndFeel";

        static String defaultTheme = texture;
    }

    static String serverTheme = Themes.texture;

    static String clientTheme =  serverTheme;

    static String interpretTheme(String[] args){
        String arg0;
        try{
            arg0 = args[0];
        }catch(IndexOutOfBoundsException e){
            arg0 = "";
        }
        String strIn = arg0.toLowerCase();
        switch(strIn){
            default:
                return Themes.defaultTheme;
            case "metal":
                return Themes.metal;
            case "nimbus":
                return Themes.nimbus;
            case "aero":
                return Themes.aero;
            case "acryl":
                return Themes.acryl;
            case "fast":
                return Themes.fast;
            case "hifi":
                return Themes.hifi;
            case "mint":
                return Themes.mint;
            case "noire":
                return Themes.noire;
            case "smart":
                return Themes.smart;
            case "luna":
                return Themes.luna;
            case "aluminium":
                return Themes.aluminium;
            case "texture":
                return Themes.texture;
            case "mac":
                return Themes.mac;
            case "yellow":
                return Themes.yellow;
        }
    }

    static int[] clientArea = {650, 500};
    static int[] serverArea = {800, 600};
}
