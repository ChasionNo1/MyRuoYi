package com.chasion.rybackend.shiro.filters;

import com.chasion.rybackend.commons.Constants;
import com.chasion.rybackend.shiro.JwtToken;
import com.chasion.rybackend.utils.JwtUtils;
import com.chasion.rybackend.utils.StringUtils;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author 32260
 * @version 1.0
 * @description: TODO 鉴权登录拦截器
 * @date 2025/6/17 14:11
 */
public class JwtFilter extends BasicHttpAuthenticationFilter {
    /**
     * 默认开启跨域设置（使用单体）
     * 微服务情况下，此属性设置为false
     */
    private boolean allowOrigin = true;

    public JwtFilter() {
    }

    public JwtFilter(boolean allowOrigin) {
        this.allowOrigin = allowOrigin;
    }

    /**
     * 执行登录认证
     * 判断是否允许通过，如果返回true，则通过，返回false则不通过
     * @param request
     * @param response
     * @param mappedValue
     * @return
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        try {
            executeLogin(request, response);
            return true;
        }catch (Exception e) {
            JwtUtils.responseError((HttpServletResponse) response, 401, Constants.TOKEN_IS_INVALID_MSG, null);
            return false;
        }
    }

    /**
     * 执行登录
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    public boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
       // 从请求头里获取token
        HttpServletRequest req = (HttpServletRequest) request;
        String token = req.getHeader(Constants.X_ACCESS_TOKEN);
        // 如果这里token为空
        if (StringUtils.isEmpty(token)){
            // 再去请求参数里获取
            token = req.getParameter(Constants.TOKEN);
        }

        if (StringUtils.isEmpty(token)) {
            throw new Exception("token缺失，请提供有效的身份凭证");
        }
        // 创建JwtToken对象并提交给Shiro进行身份验证
        JwtToken jwtToken = new JwtToken(token);
        // 登录失败会抛出异常
        getSubject(request, response).login(jwtToken);
        // 登录成功返回true
        return true;
    }

    /**
     * 对跨域提供支持
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        if(allowOrigin){
            httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, httpServletRequest.getHeader(HttpHeaders.ORIGIN));
            // 允许客户端请求方法
            httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,OPTIONS,PUT,DELETE");
            // 允许客户端提交的Header
            String requestHeaders = httpServletRequest.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
            if (StringUtils.isNotEmpty(requestHeaders)) {
                httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, requestHeaders);
            }
            // 允许客户端携带凭证信息(是否允许发送Cookie)
            httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        }
        // 跨域时会首先发送一个option请求，这里我们给option请求直接返回正常状态
        if (RequestMethod.OPTIONS.name().equalsIgnoreCase(httpServletRequest.getMethod())) {
            httpServletResponse.setStatus(HttpStatus.OK.value());
            return false;
        }
        //update-begin-author:taoyan date:20200708 for:多租户用到
//        String tenantId = httpServletRequest.getHeader(CommonConstant.TENANT_ID);
//        TenantContext.setTenant(tenantId);
        //update-end-author:taoyan date:20200708 for:多租户用到

        return super.preHandle(request, response);
    }

}
