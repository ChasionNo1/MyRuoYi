package com.chasion.rybackend.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.chasion.rybackend.entities.LoginUser;
import com.chasion.rybackend.resp.Result;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

/**
 * @author 32260
 * @version 1.0
 * @description: TODO jwt 工具类
 * @date 2025/6/17 14:16
 */
public class JwtUtils {

    /**
     * Token有效期为1年（Token在reids中缓存时间为两倍）
     */
    public static final long EXPIRE_TIME = 31104000000L;

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

    /**
     * 校验token是否正确
     *
     * @param token  密钥
     * @param secret 用户的密码
     * @return 是否正确
     */
    public static boolean verify(String token, String username, String secret) {
        try {
            // 根据密码生成JWT效验器
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).withClaim("username", username).build();
            // 效验TOKEN
            DecodedJWT jwt = verifier.verify(token);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    /**
     * 生成签名,5min后过期
     *
     * @param username 用户名
     * @param secret   用户的密码
     * @return 加密的token
     */
    public static String sign(String username, String secret) {
        Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
        Algorithm algorithm = Algorithm.HMAC256(secret);
        // 附带username信息
        return JWT.create().withClaim("username", username).withExpiresAt(date).sign(algorithm);
    }

    /**
     * 生成签名,5min后过期
     *
     * @param loginUser 用户名
     * @param secret    用户的密码
     * @return 加密的token
     */
    public static String sign(LoginUser loginUser, String loginType, String secret) {
        Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
        Algorithm algorithm = Algorithm.HMAC256(secret);
        // 附带username信息
        return JWT.create()
                .withClaim(LambdaUtils.getColumeName(LoginUser::getUserId), loginUser.getUserId())
                .withClaim(LambdaUtils.getColumeName(LoginUser::getUsername), loginUser.getUsername())
                .withClaim(LambdaUtils.getColumeName(LoginUser::getDeptId), loginUser.getDeptId())
                .withClaim("loginType", loginType).withExpiresAt(date).sign(algorithm);
    }


}
