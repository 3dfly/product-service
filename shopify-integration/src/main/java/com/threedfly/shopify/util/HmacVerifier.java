package com.threedfly.shopify.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class HmacVerifier {
    public static boolean verifyQueryString(Map<String,String> params, String secret, String hmacFromShopify){
        SortedMap<String,String> sorted = new TreeMap<>();
        for (var e: params.entrySet()) {
            if (!"hmac".equals(e.getKey())) sorted.put(e.getKey(), e.getValue());
        }
        StringBuilder sb = new StringBuilder();
        for (var it = sorted.entrySet().iterator(); it.hasNext();) {
            var e = it.next();
            sb.append(e.getKey()).append("=").append(e.getValue());
            if (it.hasNext()) sb.append("&");
        }
        String msg = sb.toString();
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(msg.getBytes(StandardCharsets.UTF_8));
            String hex = bytesToHex(raw).toLowerCase();
            String provided = URLDecoder.decode(hmacFromShopify, StandardCharsets.UTF_8).toLowerCase();
            return provided.equals(hex) || provided.equals(Base64.getEncoder().encodeToString(raw).toLowerCase());
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean verifyWebhook(byte[] rawBody, String secret, String headerHmacBase64){
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(rawBody);
            String calc = Base64.getEncoder().encodeToString(raw);
            return calc.equals(headerHmacBase64);
        } catch (Exception e) {
            return false;
        }
    }

    private static String bytesToHex(byte[] bytes){
        StringBuilder sb = new StringBuilder(bytes.length*2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
