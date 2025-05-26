package com.chasion.rybackend.service;

import com.chasion.rybackend.entities.SysMenu;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ISysMenuService {

    /**
     * 根据用户查询系统菜单列表
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    public List<SysMenu> selectMenuList(Long userId);
}
