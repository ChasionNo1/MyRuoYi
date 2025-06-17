package com.chasion.rybackend.shiro;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * @author 32260
 * @version 1.0
 * @description: TODO jwt token
 * @date 2025/6/17 14:32
 */
public class JwtToken implements AuthenticationToken {

    private static final long serialVersionUID = 1L;

    private String token;

    public JwtToken(String token) {
        this.token = token;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}
