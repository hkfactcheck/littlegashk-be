package io.littlegashk.webapp;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class JwtUtils {

    public static String resolveToken(HttpServletRequest req) {
        String bearerToken = req.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    public static String getUsername(HttpServletRequest req) {
        try {
            String body = resolveToken(req).split("\\.")[1];
            String decodedJson = new String(Base64.getDecoder().decode(body), StandardCharsets.UTF_8);
            return new ObjectMapper().readTree(decodedJson).findValuesAsText("username").get(0);
        }catch(Exception e){
            return null;
        }
    }

}
