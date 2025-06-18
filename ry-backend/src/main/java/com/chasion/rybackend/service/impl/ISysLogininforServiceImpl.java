package com.chasion.rybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chasion.rybackend.entities.SysLogininfor;
import com.chasion.rybackend.mappers.SysLogininforMapper;
import com.chasion.rybackend.service.ISysLogininforService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @author chasion
 * @version 1.0
 * @description: TODO
 * @date 2025/6/16 22:29
 */
@Service
public class ISysLogininforServiceImpl extends ServiceImpl<SysLogininforMapper, SysLogininfor> implements ISysLogininforService {

    @Autowired
    private SysLogininforMapper logininforMapper;

    /**
     * 新增系统登录日志
     *
     * @param logininfor 访问日志对象
     */
    @Override
    public void insertLogininfor(SysLogininfor logininfor) {
        logininforMapper.insert(logininfor);
    }

    /**
     * 查询系统登录日志集合
     *
     * @param logininfor 访问日志对象
     * @return 登录记录集合
     */
    @Override
    public List<SysLogininfor> selectLogininforList(SysLogininfor logininfor) {
        QueryWrapper<SysLogininfor> queryWrapper = new QueryWrapper<>();

        // 处理基本查询条件
        queryWrapper
                .like(logininfor.getIpaddr() != null && !logininfor.getIpaddr().isEmpty(), "ipaddr", logininfor.getIpaddr())
                .eq(logininfor.getStatus() != null && !logininfor.getStatus().isEmpty(), "status", logininfor.getStatus())
                .like(logininfor.getUserName() != null && !logininfor.getUserName().isEmpty(), "user_name", logininfor.getUserName());

        // 处理时间范围查询
        if (logininfor.getParams() != null) {
            Object beginTime = logininfor.getParams().get("beginTime");
            Object endTime = logininfor.getParams().get("endTime");

            if (beginTime != null && !"".equals(beginTime)) {
                queryWrapper.ge("login_time", beginTime);
            }

            if (endTime != null && !"".equals(endTime)) {
                queryWrapper.le("login_time", endTime);
            }
        }

        // 添加排序
        queryWrapper.orderByDesc("info_id");

        return logininforMapper.selectList(queryWrapper);
    }

    /**
     * 批量删除系统登录日志
     *
     * @param infoIds 需要删除的登录日志ID
     * @return 结果
     */
    @Override
    public int deleteLogininforByIds(Long[] infoIds) {
        return logininforMapper.deleteByIds(Arrays.asList(infoIds));
    }

    /**
     * 清空系统登录日志
     */
    @Override
    public void cleanLogininfor() {
        logininforMapper.cleanLogininfor();
    }

}