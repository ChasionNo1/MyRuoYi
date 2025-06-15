package com.chasion.rybackend.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.chasion.rybackend.aspectj.lang.annotation.RateLimiter;
import com.chasion.rybackend.entities.SysUser;
import com.chasion.rybackend.resp.Result;
import com.chasion.rybackend.resp.ResultCode;
import com.chasion.rybackend.service.ISysUserService;
import com.chasion.rybackend.utils.MailClient;
import com.chasion.rybackend.utils.RedisKeyUtil;
import com.chasion.rybackend.utils.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * 注册接口
 * */
@RestController
@Tag(name = "注册接口", description = "注册相关操作")
@RequestMapping("/sys")
public class SysRegisterController extends BaseController{
    @Autowired
    private ISysUserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MailClient mailClient;

    @PostMapping("/auth/register")
    @Operation(summary = "响应注册请求")
    public Result register(@RequestBody HashMap<String, Object> map) {
        // 判断是否为空
        String username = (String) map.get("username");
        String password = (String) map.get("password");
        String email = (String) map.get("email");
        String code = (String) map.get("code");
        String uuid = (String) map.get("uuid");
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password) || StringUtils.isEmpty(email) || StringUtils.isEmpty(code) || StringUtils.isEmpty(uuid)) {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), ResultCode.BAD_REQUEST.getMessage());
        }
        // 校验验证码
        String key = RedisKeyUtil.getPrefixVerifyCode(email);
        String saveCode = (String) redisTemplate.opsForValue().get(key);
        if (StringUtils.isEmpty(saveCode)) {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), "验证码失效");
        }
        if (!saveCode.equals(code)) {
           return Result.error(ResultCode.BAD_REQUEST.getCode(), "验证码错误");
        }
        // 调用service
        SysUser sysUser = new SysUser();
        sysUser.setUsername(username);
        sysUser.setPassword(password);
        sysUser.setEmail(email);
        return userService.registerUserWithRole(sysUser, "");
    }

    /**
     * @description: 获取邮箱验证码，限流在jeecg里是通过注解来的，这里可不可以呢？
     * @param: email
     * @returns: com.chasion.rybackend.resp.Result<java.lang.String>
     * @author chasion
     * @date: 2025/6/14 14:39
     */ 
    @GetMapping("/auth/verify/email")
    @Operation(summary = "响应邮箱注册请求")
    @RateLimiter(key = "rate_limit:verify:email", time = 60, count = 1)
    public Result<String> sendEmailCode(@RequestParam("email") String email) {
//        // 防刷策略：60s内不能重复发送
//        String lockKey = RedisKeyUtil.getPrefixVerifyCodeLock(email);
//        // 有锁，说明已经在请求了
//        if (redisTemplate.hasKey(lockKey)){
//            throw new BusinessException(ResultCode.T00_MANY_REQUEST.getCode(), ResultCode.T00_MANY_REQUEST.getMessage());
//        }
//        redisTemplate.opsForValue().set(lockKey, "1", 60, TimeUnit.SECONDS);
        // 生成验证码
        String code = String.format("%06d",RandomUtil.randomInt(0, 999999));
        // 生成uuid
        String uuid = IdUtil.simpleUUID();
        String key = RedisKeyUtil.getPrefixVerifyCode(email);
        redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);
        String template = "<html lang=\"zh-CN\"><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><title>验证码</title><style>* { margin: 0; padding: 0; box-sizing: border-box; font-family: \"Microsoft YaHei\", Arial, sans-serif; }body { background-color: #f9f9f9; color: #333; line-height: 1.6; }.email-container { max-width: 500px; margin: 30px auto; padding: 25px; background: white; border-radius: 6px; box-shadow: 0 1px 6px rgba(0,0,0,0.05); }.email-content { padding: 10px 0; }.greeting { color: #555; font-size: 15px; margin-bottom: 15px; }.instruction { color: #555; font-size: 15px; margin-bottom: 25px; }.code-box { margin: 25px 0; text-align: center; }.code-value { display: inline-block; padding: 18px 35px; background-color: #f5f7fa; border: 1px solid #e1e4e8; border-radius: 6px; font-size: 32px; font-weight: bold; color: #e74c3c; letter-spacing: 8px; }.tips { margin-top: 20px; color: #666; font-size: 14px; line-height: 1.7; }.highlight { color: #e74c3c; font-weight: 500; }.footer-note { margin-top: 25px; color: #999; font-size: 13px; }</style></head><body><div class=\"email-container\"><div class=\"email-content\"><p class=\"greeting\">尊敬的用户，您好：</p><p class=\"instruction\">您正在申请验证码，此验证码用于身份验证：</p><div class=\"code-box\"><div class=\"code-value\">${code}</div></div><div class=\"tips\"><p>• 验证码有效期为<span class=\"highlight\">5分钟</span>，逾期失效</p><p>• 请勿向任何人泄露此验证码</p><p>• 如非本人操作，请忽略此邮件</p></div><p class=\"footer-note\">系统自动发送，无需回复</p></div></div></body></html>";
        String emailContent = template.replace("${code}", code);
        mailClient.sendMail(email, "注册验证码", emailContent);
        return Result.success("发送成功", uuid);
    }



}
