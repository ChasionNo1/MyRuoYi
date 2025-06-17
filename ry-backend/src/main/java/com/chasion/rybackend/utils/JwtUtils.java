package com.chasion.rybackend.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.chasion.rybackend.resp.Result;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author 32260
 * @version 1.0
 * @description: TODO jwt 工具类
 * @date 2025/6/17 14:16
 */
public class JwtUtils {

    public static void responseError(HttpServletResponse response,Integer code, String message, String result) {
        response.setHeader("Content-type", "text/html;charset=UTF-8");
        Result jsonResult = new Result(code, message);
        OutputStream os = null;
        try {
            os = response.getOutputStream();
            response.setCharacterEncoding("UTF-8");
            response.setStatus(401);
            jsonResult.setData(result);
            os.write(new ObjectMapper().writeValueAsString(jsonResult).getBytes("UTF-8"));
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获得token中的信息无需secret解密也能获得
     *
     * @return token中包含的用户名
     */
    public static String getUsername(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("username").asString();
        } catch (JWTDecodeException e) {
            return null;
        }
    }

}
