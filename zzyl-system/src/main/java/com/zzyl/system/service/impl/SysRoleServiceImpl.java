package com.zzyl.system.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zzyl.common.annotation.DataScope;
import com.zzyl.common.constant.UserConstants;
import com.zzyl.common.core.domain.entity.SysRole;
import com.zzyl.common.exception.ServiceException;
import com.zzyl.common.utils.SecurityUtils;
import com.zzyl.common.utils.StringUtils;
import com.zzyl.common.utils.spring.SpringUtils;
import com.zzyl.system.domain.SysRoleDept;
import com.zzyl.system.domain.SysRoleMenu;
import com.zzyl.system.domain.SysUserRole;
import com.zzyl.system.mapper.SysRoleDeptMapper;
import com.zzyl.system.mapper.SysRoleMapper;
import com.zzyl.system.mapper.SysRoleMenuMapper;
import com.zzyl.system.mapper.SysUserRoleMapper;
import com.zzyl.system.service.ISysRoleService;

/**
 * 角色 业务层处理
 * 
 * @author ruoyi
 */
@Service
public class SysRoleServiceImpl implements ISysRoleService
{
    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysRoleMenuMapper roleMenuMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysRoleDeptMapper roleDeptMapper;

    /**
     * 根据条件分页查询角色数据
     * 
     * @param role 角色信息
     * @return 角色数据集合信息
     */
    @Override
    @DataScope(deptAlias = "d")
    public List<SysRole> selectRoleList(SysRole role)
    {
        return roleMapper.selectRoleList(role);
    }

    /**
     * 根据用户ID查询角色
     * 
     * @param userId 用户ID
     * @return 角色列表
     */
    @Override
    public List<SysRole> selectRolesByUserId(Long userId)
    {
        List<SysRole> userRoles = roleMapper.selectRolePermissionByUserId(userId);
        List<SysRole> roles = selectRoleAll();
        for (SysRole role : roles)
        {
            for (SysRole userRole : userRoles)
            {
                if (role.getRoleId().longValue() == userRole.getRoleId().longValue())
                {
                    role.setFlag(true);
                    break;
                }
            }
        }
        return roles;
    }

    /**
     * 根据用户ID查询权限
     * 
     * @param userId 用户ID
     * @return 权限列表
     */
    @Override
    public Set<String> selectRolePermissionByUserId(Long userId)
    {
        List<SysRole> perms = roleMapper.selectRolePermissionByUserId(userId);
        Set<String> permsSet = new HashSet<>();
        for (SysRole perm : perms)
        {
            if (StringUtils.isNotNull(perm))
            {
                permsSet.addAll(Arrays.asList(perm.getRoleKey().trim().split(",")));
            }
        }
        return permsSet;
    }

    /**
     * 查询所有角色
     * 
     * @return 角色列表
     */
    @Override
    public List<SysRole> selectRoleAll()
    {
        return SpringUtils.getAopProxy(this).selectRoleList(new SysRole());
    }

    /**
     * 根据用户ID获取角色选择框列表
     * 
     * @param userId 用户ID
     * @return 选中角色ID列表
     */
    @Override
    public List<Long> selectRoleListByUserId(Long userId)
    {
        return roleMapper.selectRoleListByUserId(userId);
    }

    /**
     * 通过角色ID查询角色
     * 
     * @param roleId 角色ID
     * @return 角色对象信息
     */
    @Override
    public SysRole selectRoleById(Long roleId)
    {
        return roleMapper.selectRoleById(roleId);
    }

    /**
     * 校验角色名称是否唯一
     * 
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public boolean checkRoleNameUnique(SysRole role)
    {
        Long roleId = StringUtils.isNull(role.getRoleId()) ? -1L : role.getRoleId();
        SysRole info = roleMapper.checkRoleNameUnique(role.getRoleName());
        if (StringUtils.isNotNull(info) && info.getRoleId().longValue() != roleId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验角色权限是否唯一
     * 
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public boolean checkRoleKeyUnique(SysRole role)
    {
        Long roleId = StringUtils.isNull(role.getRoleId()) ? -1L : role.getRoleId();
        SysRole info = roleMapper.checkRoleKeyUnique(role.getRoleKey());
        if (StringUtils.isNotNull(info) && info.getRoleId().longValue() != roleId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验角色是否允许操作
     * 
     * @param role 角色信息
     */
    @Override
    public void checkRoleAllowed(SysRole role)
    {
        if (StringUtils.isNotNull(role.getRoleId()) && role.isAdmin())
        {
            throw new ServiceException("不允许操作超级管理员角色");
        }
    }

    /**
     * 校验角色是否有数据权限
     * <p>
     * 优化说明（P1-5，原 DATABASE_OPTIMIZATION_RECOMMENDATIONS.md §P1-5）：
     * 原实现在内部循环里逐条查 selectRoleList，传入 N 个 roleId → N 次 SQL。
     * 现改为一次性走 params.roleIds IN (...)，由 AOP @DataScope 注入权限过滤。
     * 任一 roleId 无权限即抛错（"查完再抛"语义，业务上更优）。
     *
     * @param roleIds 一个或多个角色ID
     */
    @Override
    public void checkRoleDataScope(Long... roleIds)
    {
        if (SecurityUtils.isAdmin())
        {
            return;
        }
        if (roleIds == null || roleIds.length == 0)
        {
            return;
        }
        // 去重以兼容重复 ID
        Set<Long> idSet = new HashSet<>(Arrays.asList(roleIds));
        SysRole query = new SysRole();
        query.getParams().put("roleIds", new ArrayList<>(idSet));
        List<SysRole> roles = SpringUtils.getAopProxy(this).selectRoleList(query);
        if (roles == null || roles.size() < idSet.size())
        {
            throw new ServiceException("没有权限访问角色数据！");
        }
    }

    /**
     * 通过角色ID查询角色使用数量
     * 
     * @param roleId 角色ID
     * @return 结果
     */
    @Override
    public int countUserRoleByRoleId(Long roleId)
    {
        return userRoleMapper.countUserRoleByRoleId(roleId);
    }

    /**
     * 新增保存角色信息
     * 
     * @param role 角色信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertRole(SysRole role)
    {
        // 新增角色信息
        roleMapper.insertRole(role);
        return insertRoleMenu(role);
    }

    /**
     * 修改保存角色信息
     * 
     * @param role 角色信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateRole(SysRole role)
    {
        // 修改角色信息
        roleMapper.updateRole(role);
        // 删除角色与菜单关联
        roleMenuMapper.deleteRoleMenuByRoleId(role.getRoleId());
        return insertRoleMenu(role);
    }

    /**
     * 修改角色状态
     * 
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public int updateRoleStatus(SysRole role)
    {
        return roleMapper.updateRole(role);
    }

    /**
     * 修改数据权限信息
     * 
     * @param role 角色信息
     * @return 结果
     */
    @Override
    @Transactional
    public int authDataScope(SysRole role)
    {
        // 修改角色信息
        roleMapper.updateRole(role);
        // 删除角色与部门关联
        roleDeptMapper.deleteRoleDeptByRoleId(role.getRoleId());
        // 新增角色和部门信息（数据权限）
        return insertRoleDept(role);
    }

    /**
     * 新增角色菜单信息
     * 
     * @param role 角色对象
     */
    public int insertRoleMenu(SysRole role)
    {
        int rows = 1;
        // 新增用户与角色管理
        List<SysRoleMenu> list = new ArrayList<SysRoleMenu>();
        for (Long menuId : role.getMenuIds())
        {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(role.getRoleId());
            rm.setMenuId(menuId);
            list.add(rm);
        }
        if (list.size() > 0)
        {
            rows = roleMenuMapper.batchRoleMenu(list);
        }
        return rows;
    }

    /**
     * 新增角色部门信息(数据权限)
     *
     * @param role 角色对象
     */
    public int insertRoleDept(SysRole role)
    {
        int rows = 1;
        // 新增角色与部门（数据权限）管理
        List<SysRoleDept> list = new ArrayList<SysRoleDept>();
        for (Long deptId : role.getDeptIds())
        {
            SysRoleDept rd = new SysRoleDept();
            rd.setRoleId(role.getRoleId());
            rd.setDeptId(deptId);
            list.add(rd);
        }
        if (list.size() > 0)
        {
            rows = roleDeptMapper.batchRoleDept(list);
        }
        return rows;
    }

    /**
     * 通过角色ID删除角色
     * 
     * @param roleId 角色ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteRoleById(Long roleId)
    {
        // 删除角色与菜单关联
        roleMenuMapper.deleteRoleMenuByRoleId(roleId);
        // 删除角色与部门关联
        roleDeptMapper.deleteRoleDeptByRoleId(roleId);
        return roleMapper.deleteRoleById(roleId);
    }

    /**
     * 批量删除角色信息
     * <p>
     * 优化说明（P1-5，原 DATABASE_OPTIMIZATION_RECOMMENDATIONS.md §P1-5）：
     * 原实现在循环里逐条做 checkRoleAllowed + checkRoleDataScope +
     * selectRoleById + countUserRoleByRoleId，N 个 roleId → 3N 次 SQL。
     * 现改为：
     *   1) selectRolesByIds 一次性查全部 role（不走 AOP）
     *   2) checkRoleAllowed 循环校验（无 SQL）
     *   3) checkRoleDataScope(roleIds) 一次性 IN 校验
     *   4) countUserRoleByRoleIds 一次性 GROUP BY 统计
     *   5) 删除走原有批量 API
     *
     * @param roleIds 需要删除的角色ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteRoleByIds(Long[] roleIds)
    {
        if (roleIds == null || roleIds.length == 0)
        {
            return 0;
        }

        // IN 集合天然去重, 用 distinctIds.size() 校验, 兼容重复 ID (避免 [1,1,2] 被误判)
        Set<Long> distinctIds = new HashSet<>(Arrays.asList(roleIds));
        // 1) 一次性查全部 role（不走 AOP）
        List<SysRole> roles = roleMapper.selectRolesByIds(new ArrayList<>(distinctIds));
        if (roles.size() != distinctIds.size())
        {
            throw new ServiceException("存在无效的角色ID");
        }

        // 2) 校验 admin（无 SQL）
        for (SysRole role : roles)
        {
            checkRoleAllowed(role);
        }

        // 3) 一次性数据范围校验
        checkRoleDataScope(roleIds);

        // 4) 批量统计已分配用户数（一次 GROUP BY）
        List<Map<String, Object>> countList = userRoleMapper.countUserRoleByRoleIds(Arrays.asList(roleIds));
        Map<Long, Long> countMap = new HashMap<>();
        for (Map<String, Object> row : countList)
        {
            Object rid = row.get("role_id");
            Object cnt = row.get("cnt");
            if (rid != null && cnt != null)
            {
                countMap.put(((Number) rid).longValue(), ((Number) cnt).longValue());
            }
        }
        for (SysRole role : roles)
        {
            Long cnt = countMap.get(role.getRoleId());
            if (cnt != null && cnt > 0)
            {
                throw new ServiceException(String.format("%1$s已分配,不能删除", role.getRoleName()));
            }
        }

        // 5) 删除（批量）
        roleMenuMapper.deleteRoleMenu(roleIds);
        roleDeptMapper.deleteRoleDept(roleIds);
        return roleMapper.deleteRoleByIds(roleIds);
    }

    /**
     * 取消授权用户角色
     * 
     * @param userRole 用户和角色关联信息
     * @return 结果
     */
    @Override
    public int deleteAuthUser(SysUserRole userRole)
    {
        return userRoleMapper.deleteUserRoleInfo(userRole);
    }

    /**
     * 批量取消授权用户角色
     * 
     * @param roleId 角色ID
     * @param userIds 需要取消授权的用户数据ID
     * @return 结果
     */
    @Override
    public int deleteAuthUsers(Long roleId, Long[] userIds)
    {
        return userRoleMapper.deleteUserRoleInfos(roleId, userIds);
    }

    /**
     * 批量选择授权用户角色
     * 
     * @param roleId 角色ID
     * @param userIds 需要授权的用户数据ID
     * @return 结果
     */
    @Override
    public int insertAuthUsers(Long roleId, Long[] userIds)
    {
        // 新增用户与角色管理
        List<SysUserRole> list = new ArrayList<SysUserRole>();
        for (Long userId : userIds)
        {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            list.add(ur);
        }
        return userRoleMapper.batchUserRole(list);
    }
}
