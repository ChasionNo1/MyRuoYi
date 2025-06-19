package com.chasion.rybackend.shiro;

import com.chasion.rybackend.commons.Constants;
import com.chasion.rybackend.entities.LoginUser;
import com.chasion.rybackend.service.IBaseCommonService;
import com.chasion.rybackend.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
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
            loginUser = this.checkUserTokenIsEffect(token);
        }catch (AuthenticationException e){
            JwtUtils.responseError(ServletUtils.getResponse(), 401, e.getMessage(), null);
            e.printStackTrace();
            return null;
        }finally {
            log.info("===========shiro 身份认证成功 doGetAuthenticationInfo ======= ip地址为：" + req.getRemoteAddr() + "URL:"+ req.getRequestURI());
        }
        return new SimpleAuthenticationInfo(loginUser, token, getName());
    }

    private LoginUser checkUserTokenIsEffect(String token) throws AuthenticationException {
        String username = JwtUtils.getUsername(token);
        if (username == null){
            throw new AuthenticationException("token非法无效！");
        }

        log.debug("———校验token是否有效————checkUserTokenIsEffect——————— "+ token);
        // todo 要确保从loginUser获取的数据不为null，也就是说在登录成功设置时，要考虑到位
        LoginUser loginUser = TokenUtils.getLoginUser(username, BaseCommonService, redisUtil);
        if (loginUser == null){
            throw new AuthenticationException("用户不存在！");
        }
        // 判断用户状态  0 正常
        if (!Constants.USER_STATUS_0.equals(loginUser.getStatus())) {
            throw new AuthenticationException("用户已被冻结！");
        }

        // 校验token是否超时失效 & 或者账号密码是否错误
        if (!jwtTokenRefresh(token, username, loginUser.getPassword())){
            // 校验未通过
            throw new AuthenticationException("token已失效！");
        }
        return loginUser;

    }
    /**
     * JWTToken刷新生命周期 （实现： 用户在线操作不掉线功能）
     * 1、登录成功后将用户的JWT生成的Token作为k、v存储到cache缓存里面(这时候k、v值一样)，缓存有效期设置为Jwt有效时间的2倍
     * 2、当该用户再次请求时，通过JWTFilter层层校验之后会进入到doGetAuthenticationInfo进行身份验证
     * 3、当该用户这次请求jwt生成的token值已经超时，但该token对应cache中的k还是存在，则表示该用户一直在操作只是JWT的token失效了，程序会给token对应的k映射的v值重新生成JWTToken并覆盖v值，该缓存生命周期重新计算
     * 4、当该用户这次请求jwt在生成的token值已经超时，并在cache中不存在对应的k，则表示该用户账户空闲超时，返回用户信息已失效，请重新登录。
     * 注意： 前端请求Header中设置Authorization保持不变，校验有效性以缓存中的token为准。
     *       用户过期时间 = Jwt有效时间 * 2。
     *
     * @param username
     * @param password
     * @return
     */
    public boolean jwtTokenRefresh(String token, String username, String password) {
        // 从redis里取数据
        String cacheToken = String.valueOf(redisUtil.get(Constants.PREFIX_USER_TOKEN + token));
        if (StringUtils.isNotEmpty(cacheToken)){
            // 如果不为空
            if (!JwtUtils.verify(cacheToken, username, password)) {
                // 校验没通过，就新生成一个 token
                String newAuthorization = JwtUtils.sign(username, password);
                redisUtil.set(Constants.PREFIX_USER_TOKEN + token, newAuthorization);
                redisUtil.expire(Constants.PREFIX_USER_TOKEN + token, JwtUtils.EXPIRE_TIME * 2 / 1000);
                log.debug("——————————用户在线操作，更新token保证不掉线—————————jwtTokenRefresh——————— "+ token);
            }
            // token通过
            return true;
        }
        // token 不存在
        return false;
    }

    /**
     * 清除当前用户的权限认证缓存
     * 重写父类的方法，这里可以做增强
     * @param principals 权限信息
     */
    @Override
    public void clearCache(PrincipalCollection principals) {
        super.clearCache(principals);
    }
}
