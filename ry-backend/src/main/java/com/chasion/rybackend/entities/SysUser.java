package com.chasion.rybackend.entities;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 用户对象
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysUser extends BaseEntity{
    private static final long serialVersionUID = 1L;

    private Long userId;

    private Long deptId;

    @TableField("user_name")
    private String username;

    @TableField("nick_name")
    private String nickname;

    private String email;

    private String phoneNumber;

    private String sex;

    private String password;

    private String salt;

    /** 账号状态（0正常 1停用） */
    private String status;

    private String delFlag;

    private String loginIp;

    private Date loginDate;

    /** 密码最后更新时间 */
    private Date pwdUpdateDate;

    private SysDept dept;

    /** 角色对象 */
    private List<SysRole> roles;

    /** 角色组 */
    private Long[] roleIds;

    /** 岗位组 */
    private Long[] postIds;

    /** 角色ID */
    private Long roleId;

    public static boolean isAdmin(Long userId)
    {
        return userId != null && 1L == userId;
    }

    public boolean isAdmin()
    {
        return isAdmin(this.userId);
    }



}
