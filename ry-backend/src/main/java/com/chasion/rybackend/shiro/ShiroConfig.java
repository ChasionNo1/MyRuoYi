package com.chasion.rybackend.shiro;

import com.chasion.rybackend.shiro.filters.JwtFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * @author 32260
 * @version 1.0
 * @description: TODO shiro配置类
 * @date 2025/6/17 13:49
 */
@Slf4j
@Configuration
public class ShiroConfig {

    /**
     * 配置 Shiro 过滤器
     * @param securityManager
     * @return
     */
    @Bean("shiroFilterFactoryBean")
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager) {
        CustomShiroFilterFactoryBean shiroFilterFactoryBean = new CustomShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        // 拦截器
        LinkedHashMap<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        // 配置不会被拦截的链接 顺序判断
        // 配置 Shiro 过滤规则，"anon" 表示该路径无需认证即可访问（匿名访问）
//        filterChainDefinitionMap.put("/**", "anon"); // 临时测试
        filterChainDefinitionMap.put("/sys/auth/login", "anon");
        filterChainDefinitionMap.put("/sys/auth/register", "anon");
        filterChainDefinitionMap.put("/sys/auth/verify/email", "anon");
        filterChainDefinitionMap.put("/", "anon");
        filterChainDefinitionMap.put("/doc.html", "anon");
        filterChainDefinitionMap.put("/**/*.js", "anon");
        filterChainDefinitionMap.put("/**/*.css", "anon");
        filterChainDefinitionMap.put("/**/*.html", "anon");
        filterChainDefinitionMap.put("/**/*.svg", "anon");
        filterChainDefinitionMap.put("/**/*.pdf", "anon");
        filterChainDefinitionMap.put("/**/*.jpg", "anon");
        filterChainDefinitionMap.put("/**/*.png", "anon");
        filterChainDefinitionMap.put("/**/*.ico", "anon");

        // update-begin--Author:sunjianlei Date:20190813 for：排除字体格式的后缀
        filterChainDefinitionMap.put("/**/*.ttf", "anon");
        filterChainDefinitionMap.put("/**/*.woff", "anon");
        filterChainDefinitionMap.put("/**/*.woff2", "anon");

        // 添加自己的过滤器并且取名为jwt
        HashMap<String, Filter> filterMap = new HashMap<>();
        // 注册jwt过滤器
        filterMap.put("jwt", new JwtFilter(true));
        shiroFilterFactoryBean.setFilters(filterMap);
        // <!-- 过滤链定义，从上向下顺序执行，一般将/**放在最为下边
        filterChainDefinitionMap.put("/**", "jwt");

        // todo 未授权页面返回json

        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;

    }

    @Bean("securityManager")
    public DefaultWebSecurityManager securityManager(ShiroRealm myRealm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(myRealm);

        // 配置SubjectDAO以禁用Shiro的会话存储功能，适用于无状态应用（如使用JWT）
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator sessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        sessionStorageEvaluator.setSessionStorageEnabled(false); // 禁用会话存储
        subjectDAO.setSessionStorageEvaluator(sessionStorageEvaluator);
        securityManager.setSubjectDAO(subjectDAO); // 将配置好的SubjectDAO设置到SecurityManager中
        // todo 自定义缓存实现，使用redis

        return securityManager;
    }

    /**
     * 下面的代码是添加注解支持
     * @return
     */
    @Bean
    @DependsOn("lifecycleBeanPostProcessor")
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        defaultAdvisorAutoProxyCreator.setProxyTargetClass(true);
        /**
         * 解决重复代理问题 github#994
         * 添加前缀判断 不匹配 任何Advisor
         */
        defaultAdvisorAutoProxyCreator.setUsePrefix(true);
        defaultAdvisorAutoProxyCreator.setAdvisorBeanNamePrefix("_no_advisor");
        return defaultAdvisorAutoProxyCreator;
    }

    @Bean
    public static LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    /**
     * 该方法用于定义和配置一个 Spring Bean，类型为 `AuthorizationAttributeSourceAdvisor`，它的主要作用是启用 Shiro 框架中基于注解的权限控制功能（如 `@RequiresPermissions`, `@RequiresRoles` 等），从而实现对方法级别的访问控制。
     *
     * ### 方法详细说明：
     *
     * ```java
     * @Bean
     * public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
     *     AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
     *     advisor.setSecurityManager(securityManager);
     *     return advisor;
     * }
     * ```
     *
     *
     * #### 1. 注解 `@Bean`
     * - 表示该方法返回的对象将被注册为 Spring 容器中的一个 Bean。
     * - 默认情况下，Bean 的名称与方法名相同（即 `authorizationAttributeSourceAdvisor`）。
     *
     * #### 2. 参数 `SecurityManager securityManager`
     * - 这是一个由 Spring 容器自动注入的参数，通常是由其他配置方法创建的 Shiro 的核心安全管理器。
     * - 用于提供权限判断所需的用户身份和权限信息。
     *
     * #### 3. 创建 `AuthorizationAttributeSourceAdvisor` 实例
     * - `AuthorizationAttributeSourceAdvisor` 是 Shiro 提供的一个 AOP 切面类，它会扫描使用了 Shiro 注解的方法，并在调用这些方法时进行权限验证。
     * - 示例注解包括：
     *   - `@RequiresAuthentication`：要求用户必须已认证。
     *   - `@RequiresUser`：要求用户必须是“记住我”或已认证。
     *   - `@RequiresGuest`：要求用户必须是访客。
     *   - `@RequiresRoles("admin")`：要求用户具有指定角色。
     *   - `@RequiresPermissions("user:create")`：要求用户具有指定权限。
     *
     * #### 4. 设置 SecurityManager
     * - 通过 `advisor.setSecurityManager(securityManager)` 将安全管理者注入到切面中，使得其可以获取当前用户的权限信息来进行校验。
     *
     * #### 5. 返回值
     * - 返回配置好的 `AuthorizationAttributeSourceAdvisor` 实例，供 Spring 在 AOP 机制中使用，以拦截并处理带有 Shiro 注解的方法。
     *
     * ---
     *
     * ### 总结
     *
     * 此方法的作用是 **启用 Shiro 基于注解的权限控制**，使得你可以在 Controller 或 Service 层的方法上直接使用如 `@RequiresPermissions`、`@RequiresRoles` 等注解来限制访问权限。
     * 这属于 Shiro 与 Spring 集成的关键配置之一。
     * @param securityManager
     * @return
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(DefaultWebSecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }
}
