package com.example.arducontrol;

public class TempData {

    //public static final String url = "http://asdo-proyect.freesite.online/arducontrol_sql";
    public static final String url = "http://192.168.20.4/arducontrol_sql";
    private static String username = "usuarioRandom";
    private static String email = "correo@gmail.com";
    private static String title = "ensamblePrueba";
    private static String version = "versionPrueba";

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        TempData.username = username;
    }

    public static String getEmail() {
        return email;
    }

    public static void setEmail(String email) {
        TempData.email = email;
    }

    public static String getTitle() {
        return title;
    }

    public static void setTitle(String title) {
        TempData.title = title;
    }

    public static String getVersion() {
        return version;
    }

    public static void setVersion(String version) {
        TempData.version = version;
    }

    public static void defaultValues(){
        setUsername("usuarioRandom");
        setEmail("correo@gmail.com");
        setTitle("ensamblePrueba");
        setVersion("versionPrueba");
    }
}
