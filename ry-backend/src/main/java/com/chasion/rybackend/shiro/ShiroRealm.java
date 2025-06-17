package com.chasion.rybackend.shiro;

import com.chasion.rybackend.entities.LoginUser;
import com.chasion.rybackend.service.IBaseCommonService;
import com.chasion.rybackend.utils.JwtUtils;
import com.chasion.rybackend.utils.RedisUtil;
import com.chasion.rybackend.utils.ServletUtils;
import com.chasion.rybackend.utils.TokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * @author 32260
 * @version 1.0
 * @description: TODO 用户登录鉴权和获取用户授权
 * @date 2025/6/17 14:39
 */
@Slf4j
@Component
public class ShiroRealm extends AuthorizingRealm {

    @Autowired
    private IBaseCommonService BaseCommonService;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 必须重写此方法，不然Shiro会报错
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    /**
     * 授权逻辑
     * 权限信息认证(包括角色以及权限)是用户访问controller的时候才进行验证(redis存储的此处权限信息)
     * @param principals 身份信息
     * @return AuthorizationInfo 权限信息
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        log.debug("===============Shiro权限认证开始============ [ roles、permissions]==========");
        String username = null;
        if (principals != null) {
            // 从身份集合中获取主身份对象，即登录用户信息
            LoginUser loginUser = (LoginUser) principals.getPrimaryPrincipal();
            username = loginUser.getUsername();
        }
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        // 设置用户拥有的角色集合，比如“admin,test”
        // 根据用户名查询用户的角色集合
        info.setRoles(BaseCommonService.queryUserRoles(username));

        // 根据用户名查询用户的权限集合
        Set<String> permissionSet = BaseCommonService.queryUserAuths(username, "");
        info.addStringPermissions(permissionSet);
        log.info("===========shiro权限认证成功============");
        return info;
    }

    /**
     * 认证逻辑
     * @param auth
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken auth) throws AuthenticationException {
        log.info("===========shiro 身份认证开始 doGetAuthenticationInfo =======");
        String token = (String) auth.getCredentials();
        HttpServletRequest req = ServletUtils.getRequest();
        if (token == null) {
            log.info("===========shiro 身份认证失败 doGetAuthenticationInfo ======= ip地址为：" + req.getRemoteAddr() + "URL:"+ req.getRequestURI());
            throw new AuthenticationException("token为空");
        }
        // 校验token的有效性
        LoginUser loginUser = null;
        try {
            loginUser =
        }
        return null;
    }

    private LoginUser checkUserTokenIsEffect(String token) throws AuthenticationException {
        String username = JwtUtils.getUsername(token);
        if (username == null){
            throw new AuthenticationException("token非法无效！");
        }

        log.debug("———校验token是否有效————checkUserTokenIsEffect——————— "+ token);
        LoginUser loginUser = TokenUtils.getLoginUser(username, BaseCommonService, redisUtil);
        if (loginUser == null){
            throw new AuthenticationException("用户不存在！");
        }
        // 判断用户状态
        if (loginUser.getStatus != 1) {

        }
    }
}
