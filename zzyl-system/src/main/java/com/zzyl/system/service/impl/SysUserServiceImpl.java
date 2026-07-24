package com.zzyl.system.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import com.zzyl.common.annotation.DataScope;
import com.zzyl.common.constant.UserConstants;
import com.zzyl.common.core.domain.entity.SysRole;
import com.zzyl.common.core.domain.entity.SysUser;
import com.zzyl.common.exception.ServiceException;
import com.zzyl.common.utils.SecurityUtils;
import com.zzyl.common.utils.StringUtils;
import com.zzyl.common.utils.bean.BeanValidators;
import com.zzyl.common.utils.spring.SpringUtils;
import com.zzyl.system.domain.SysPost;
import com.zzyl.system.domain.SysUserPost;
import com.zzyl.system.domain.SysUserRole;
import com.zzyl.system.mapper.SysPostMapper;
import com.zzyl.system.mapper.SysRoleMapper;
import com.zzyl.system.mapper.SysUserMapper;
import com.zzyl.system.mapper.SysUserPostMapper;
import com.zzyl.system.mapper.SysUserRoleMapper;
import com.zzyl.system.service.ISysConfigService;
import com.zzyl.system.service.ISysDeptService;
import com.zzyl.system.service.ISysUserService;

/**
 * 用户 业务层处理
 * 
 * @author ruoyi
 */
@Service
public class SysUserServiceImpl implements ISysUserService
{
    private static final Logger log = LoggerFactory.getLogger(SysUserServiceImpl.class);

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysPostMapper postMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysUserPostMapper userPostMapper;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private ISysDeptService deptService;

    @Autowired
    protected Validator validator;

    /**
     * 根据条件分页查询用户列表
     * 
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    @DataScope(deptAlias = "d", userAlias = "u")
    public List<SysUser> selectUserList(SysUser user)
    {
        return userMapper.selectUserList(user);
    }

    /**
     * 根据条件分页查询已分配用户角色列表
     * 
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    @DataScope(deptAlias = "d", userAlias = "u")
    public List<SysUser> selectAllocatedList(SysUser user)
    {
        return userMapper.selectAllocatedList(user);
    }

    /**
     * 根据条件分页查询未分配用户角色列表
     * 
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    @DataScope(deptAlias = "d", userAlias = "u")
    public List<SysUser> selectUnallocatedList(SysUser user)
    {
        return userMapper.selectUnallocatedList(user);
    }

    /**
     * 通过用户名查询用户
     * 
     * @param userName 用户名
     * @return 用户对象信息
     */
    @Override
    public SysUser selectUserByUserName(String userName)
    {
        return userMapper.selectUserByUserName(userName);
    }

    /**
     * 通过用户ID查询用户
     * 
     * @param userId 用户ID
     * @return 用户对象信息
     */
    @Override
    public SysUser selectUserById(Long userId)
    {
        return userMapper.selectUserById(userId);
    }

    /**
     * 查询用户所属角色组
     * 
     * @param userName 用户名
     * @return 结果
     */
    @Override
    public String selectUserRoleGroup(String userName)
    {
        List<SysRole> list = roleMapper.selectRolesByUserName(userName);
        if (CollectionUtils.isEmpty(list))
        {
            return StringUtils.EMPTY;
        }
        return list.stream().map(SysRole::getRoleName).collect(Collectors.joining(","));
    }

    /**
     * 查询用户所属岗位组
     * 
     * @param userName 用户名
     * @return 结果
     */
    @Override
    public String selectUserPostGroup(String userName)
    {
        List<SysPost> list = postMapper.selectPostsByUserName(userName);
        if (CollectionUtils.isEmpty(list))
        {
            return StringUtils.EMPTY;
        }
        return list.stream().map(SysPost::getPostName).collect(Collectors.joining(","));
    }

    /**
     * 校验用户名称是否唯一
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public boolean checkUserNameUnique(SysUser user)
    {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = userMapper.checkUserNameUnique(user.getUserName());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验手机号码是否唯一
     *
     * @param user 用户信息
     * @return
     */
    @Override
    public boolean checkPhoneUnique(SysUser user)
    {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = userMapper.checkPhoneUnique(user.getPhonenumber());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验email是否唯一
     *
     * @param user 用户信息
     * @return
     */
    @Override
    public boolean checkEmailUnique(SysUser user)
    {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = userMapper.checkEmailUnique(user.getEmail());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验用户是否允许操作
     * 
     * @param user 用户信息
     */
    @Override
    public void checkUserAllowed(SysUser user)
    {
        if (StringUtils.isNotNull(user.getUserId()) && user.isAdmin())
        {
            throw new ServiceException("不允许操作超级管理员用户");
        }
    }

    /**
     * 校验用户是否有数据权限
     * 
     * @param userId 用户id
     */
    @Override
    public void checkUserDataScope(Long userId)
    {
        if (!SecurityUtils.isAdmin())
        {
            SysUser user = new SysUser();
            user.setUserId(userId);
            List<SysUser> users = SpringUtils.getAopProxy(this).selectUserList(user);
            if (StringUtils.isEmpty(users))
            {
                throw new ServiceException("没有权限访问用户数据！");
            }
        }
    }

    /**
     * 新增保存用户信息
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertUser(SysUser user)
    {
        // 新增用户信息
        int rows = userMapper.insertUser(user);
        // 新增用户岗位关联
        insertUserPost(user);
        // 新增用户与角色管理
        insertUserRole(user);
        return rows;
    }

    /**
     * 注册用户信息
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public boolean registerUser(SysUser user)
    {
        return userMapper.insertUser(user) > 0;
    }

    /**
     * 修改保存用户信息
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateUser(SysUser user)
    {
        Long userId = user.getUserId();
        // 删除用户与角色关联
        userRoleMapper.deleteUserRoleByUserId(userId);
        // 新增用户与角色管理
        insertUserRole(user);
        // 删除用户与岗位关联
        userPostMapper.deleteUserPostByUserId(userId);
        // 新增用户与岗位管理
        insertUserPost(user);
        return userMapper.updateUser(user);
    }

    /**
     * 用户授权角色
     * 
     * @param userId 用户ID
     * @param roleIds 角色组
     */
    @Override
    @Transactional
    public void insertUserAuth(Long userId, Long[] roleIds)
    {
        userRoleMapper.deleteUserRoleByUserId(userId);
        insertUserRole(userId, roleIds);
    }

    /**
     * 修改用户状态
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int updateUserStatus(SysUser user)
    {
        return userMapper.updateUserStatus(user.getUserId(), user.getStatus());
    }

    /**
     * 修改用户基本信息
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int updateUserProfile(SysUser user)
    {
        return userMapper.updateUser(user);
    }

    /**
     * 修改用户头像
     * 
     * @param userId 用户ID
     * @param avatar 头像地址
     * @return 结果
     */
    @Override
    public boolean updateUserAvatar(Long userId, String avatar)
    {
        return userMapper.updateUserAvatar(userId, avatar) > 0;
    }

    /**
     * 更新用户登录信息（IP和登录时间）
     * 
     * @param userId 用户ID
     * @param loginIp 登录IP地址
     * @param loginDate 登录时间
     * @return 结果
     */
    public void updateLoginInfo(Long userId, String loginIp, Date loginDate)
    {
        userMapper.updateLoginInfo(userId, loginIp, loginDate);
    }

    /**
     * 重置用户密码
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int resetPwd(SysUser user)
    {
        return userMapper.resetUserPwd(user.getUserId(), user.getPassword());
    }

    /**
     * 重置用户密码
     * 
     * @param userId 用户ID
     * @param password 密码
     * @return 结果
     */
    @Override
    public int resetUserPwd(Long userId, String password)
    {
        return userMapper.resetUserPwd(userId, password);
    }

    /**
     * 新增用户角色信息
     * 
     * @param user 用户对象
     */
    public void insertUserRole(SysUser user)
    {
        this.insertUserRole(user.getUserId(), user.getRoleIds());
    }

    /**
     * 新增用户岗位信息
     * 
     * @param user 用户对象
     */
    public void insertUserPost(SysUser user)
    {
        Long[] posts = user.getPostIds();
        if (StringUtils.isNotEmpty(posts))
        {
            // 新增用户与岗位管理
            List<SysUserPost> list = new ArrayList<SysUserPost>(posts.length);
            for (Long postId : posts)
            {
                SysUserPost up = new SysUserPost();
                up.setUserId(user.getUserId());
                up.setPostId(postId);
                list.add(up);
            }
            userPostMapper.batchUserPost(list);
        }
    }

    /**
     * 新增用户角色信息
     * 
     * @param userId 用户ID
     * @param roleIds 角色组
     */
    public void insertUserRole(Long userId, Long[] roleIds)
    {
        if (StringUtils.isNotEmpty(roleIds))
        {
            // 新增用户与角色管理
            List<SysUserRole> list = new ArrayList<SysUserRole>(roleIds.length);
            for (Long roleId : roleIds)
            {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                list.add(ur);
            }
            userRoleMapper.batchUserRole(list);
        }
    }

    /**
     * 通过用户ID删除用户
     * 
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteUserById(Long userId)
    {
        // 删除用户与角色关联
        userRoleMapper.deleteUserRoleByUserId(userId);
        // 删除用户与岗位表
        userPostMapper.deleteUserPostByUserId(userId);
        return userMapper.deleteUserById(userId);
    }

    /**
     * 批量删除用户信息
     * 
     * @param userIds 需要删除的用户ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteUserByIds(Long[] userIds)
    {
        for (Long userId : userIds)
        {
            checkUserAllowed(new SysUser(userId));
            checkUserDataScope(userId);
        }
        // 删除用户与角色关联
        userRoleMapper.deleteUserRole(userIds);
        // 删除用户与岗位关联
        userPostMapper.deleteUserPost(userIds);
        return userMapper.deleteUserByIds(userIds);
    }

    /**
     * 导入用户数据
     * <p>
     * 优化说明（P0-2，原 DATABASE_OPTIMIZATION_RECOMMENDATIONS.md §P0-2 / 文档 §P0-2）：
     * 原实现每次循环逐条 selectUserByUserName + 逐条 insert/update，导入 1000 条约 2000+ 次 SQL。
     * 现改为：
     *   1) selectConfigByKey(initPassword) 提到循环外（1 次）
     *   2) userMapper.selectUsersByUserNames(userNames) 一次性查已存在集合（1 次）
     *   3) 按是否存在 / isUpdateSupport 分类成 toInsert / toUpdate 列表
     *   4) 1 次 batchInsertUser + 1 次 batchUpdateUser（CASE WHEN 原子更新）
     *   5) 若批量语句整体异常，降级逐条执行以保留原版的失败粒度
     * <p>
     * 外部行为（successMsg/failureMsg 文案、isUpdateSupport 语义、operName 行为）保持不变。
     *
     * @param userList 用户数据列表
     * @param isUpdateSupport 是否更新支持，如果已存在，则进行更新数据
     * @param operName 操作用户
     * @return 结果
     */
    @Override
    public String importUser(List<SysUser> userList, Boolean isUpdateSupport, String operName)
    {
        if (StringUtils.isNull(userList) || userList.size() == 0)
        {
            throw new ServiceException("导入用户数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();

        // 1) 一次性取初始化密码（不再每条都查一次）
        String initPassword = configService.selectConfigByKey("sys.user.initPassword");

        // 2) 一次性查所有已存在用户，构建 map
        List<String> userNames = userList.stream()
                .map(SysUser::getUserName)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toList());
        Map<String, SysUser> existingMap = new HashMap<>();
        if (!userNames.isEmpty())
        {
            for (SysUser u : userMapper.selectUsersByUserNames(userNames))
            {
                existingMap.put(u.getUserName(), u);
            }
        }

        // 3) 分类：toInsert / toUpdate（带校验 / 权限校验，失败计入 failureMsg 不抛出）
        List<SysUser> toInsert = new ArrayList<>();
        List<SysUser> toUpdate = new ArrayList<>();
        for (SysUser user : userList)
        {
            SysUser existing = existingMap.get(user.getUserName());
            try
            {
                BeanValidators.validateWithException(validator, user);
                deptService.checkDeptDataScope(user.getDeptId());
                if (existing == null)
                {
                    // 新增分支
                    user.setPassword(SecurityUtils.encryptPassword(initPassword));
                    user.setCreateBy(operName);
                    toInsert.add(user);
                }
                else if (Boolean.TRUE.equals(isUpdateSupport))
                {
                    // 更新分支：复用已有 userId / deptId
                    checkUserAllowed(existing);
                    checkUserDataScope(existing.getUserId());
                    deptService.checkDeptDataScope(user.getDeptId());
                    user.setUserId(existing.getUserId());
                    user.setDeptId(existing.getDeptId());
                    user.setUpdateBy(operName);
                    toUpdate.add(user);
                }
                else
                {
                    failureNum++;
                    failureMsg.append("<br/>").append(failureNum).append("、账号 ").append(user.getUserName()).append(" 已存在");
                }
            }
            catch (Exception e)
            {
                failureNum++;
                String msg = "<br/>" + failureNum + "、账号 " + user.getUserName() + " 导入失败：";
                failureMsg.append(msg).append(e.getMessage());
                log.error(msg, e);
            }
        }

        // 4) 批量 insert（异常时降级逐条，保持原错误粒度）
        if (!toInsert.isEmpty())
        {
            try
            {
                userMapper.batchInsertUser(toInsert);
                for (SysUser u : toInsert)
                {
                    successNum++;
                    successMsg.append("<br/>").append(successNum).append("、账号 ").append(u.getUserName()).append(" 导入成功");
                }
            }
            catch (Exception batchErr)
            {
                log.error("批量插入用户失败，降级逐条处理", batchErr);
                for (SysUser u : toInsert)
                {
                    try
                    {
                        userMapper.insertUser(u);
                        successNum++;
                        successMsg.append("<br/>").append(successNum).append("、账号 ").append(u.getUserName()).append(" 导入成功");
                    }
                    catch (Exception singleErr)
                    {
                        failureNum++;
                        String msg = "<br/>" + failureNum + "、账号 " + u.getUserName() + " 导入失败：";
                        failureMsg.append(msg).append(singleErr.getMessage());
                        log.error(msg, singleErr);
                    }
                }
            }
        }

        // 5) 批量 update（异常时降级逐条）
        if (!toUpdate.isEmpty())
        {
            try
            {
                userMapper.batchUpdateUser(toUpdate);
                for (SysUser u : toUpdate)
                {
                    successNum++;
                    successMsg.append("<br/>").append(successNum).append("、账号 ").append(u.getUserName()).append(" 更新成功");
                }
            }
            catch (Exception batchErr)
            {
                log.error("批量更新用户失败，降级逐条处理", batchErr);
                for (SysUser u : toUpdate)
                {
                    try
                    {
                        userMapper.updateUser(u);
                        successNum++;
                        successMsg.append("<br/>").append(successNum).append("、账号 ").append(u.getUserName()).append(" 更新成功");
                    }
                    catch (Exception singleErr)
                    {
                        failureNum++;
                        String msg = "<br/>" + failureNum + "、账号 " + u.getUserName() + " 更新失败：";
                        failureMsg.append(msg).append(singleErr.getMessage());
                        log.error(msg, singleErr);
                    }
                }
            }
        }

        // 6) 拼装返回文（与原版完全一致）
        if (failureNum > 0)
        {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new ServiceException(failureMsg.toString());
        }
        else
        {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }
}
