package service;

public class StoreUtils {
    public static String coalesce(String a, String b) {
        return a != null && !a.isBlank() ? a : b;
    }
}
