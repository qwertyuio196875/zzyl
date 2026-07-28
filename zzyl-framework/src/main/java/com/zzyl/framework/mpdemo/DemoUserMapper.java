package com.zzyl.framework.mpdemo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus 阶段 ② 验证 Mapper。
 *
 * 继承 BaseMapper<DemoUser> 后自动获得：
 *   - insert(T entity)
 *   - deleteById(Serializable id) / delete(Wrapper<T>) / deleteBatchIds(...)
 *   - updateById(T entity) / update(T entity, Wrapper<T>)
 *   - selectById(Serializable id) / selectList(Wrapper<T>) / selectCount(Wrapper<T>)
 *   - selectPage(Page<T>, Wrapper<T>) （需要额外装配分页插件）
 */
@Mapper
public interface DemoUserMapper extends BaseMapper<DemoUser> {
}
