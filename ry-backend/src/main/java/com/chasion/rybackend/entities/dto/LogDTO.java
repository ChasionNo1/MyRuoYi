package com.chasion.rybackend.entities.dto;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.chasion.rybackend.entities.vo.LoginUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author chasion
 * @version 1.0
 * @description: TODO 数据传输接口对象
 * @date 2025/6/15 16:10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@NoArgsConstructor
public class LogDTO implements Serializable {
    private static final long serialVersionUID = 8482720462943906924L;

    private String id;
    // 传输内容
    private String logContent;

    /**
     * 日志类型(0:操作日志;1:登录日志;2:定时任务)
     */
    private Integer logType;

    /**
     * 操作类型(1:添加;2:修改;3:删除;)
     */
    private Integer operateType;

    /**
     * 登录用户
     */
    private LoginUser loginUser;

    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String createBy;

    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Date createTime;

    private Long costTime;

    private String ip;

    /**请求参数 */
    private String requestParam;

    /**请求类型*/
    private String requestType;

    /**请求路径*/
    private String requestUrl;

    /**请求方法 */
    private String method;

    /**操作人用户名称*/
    private String username;

    /**操作人用户账户*/
    private String userid;

    public LogDTO(String logContent, Integer logType, Integer operatetype){
        this.logContent = logContent;
        this.logType = logType;
        this.operateType = operatetype;
    }

    public LogDTO(String logContent, Integer logType, Integer operatetype, LoginUser loginUser){
        this.logContent = logContent;
        this.logType = logType;
        this.operateType = operatetype;
        this.loginUser = loginUser;
    }
}