package com.zzyl.framework.handler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import com.zzyl.common.utils.DateUtils;

/**
 * MyBatis TypeHandler: String ↔ java.sql.Time
 * <p>
 * 配套 visit_record.visit_time varchar(20) → TIME 的改造:
 *   - 写入: Java String "HH:mm:ss" / "" → NULL  → java.sql.Time
 *   - 读取: java.sql.Time             → Java String "HH:mm:ss" / NULL
 * <p>
 * 见 DATABASE_OPTIMIZATION_RECOMMENDATIONS.md §P0-3 / sql/patch_20260724_visit_record_types.sql
 *
 * @author zzyl
 */
@MappedTypes(String.class)
public class StringToTimeTypeHandler extends BaseTypeHandler<String>
{
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException
    {
        // 空串归 NULL（与 visit.sql DDL default null 保持一致）
        if (parameter == null || parameter.trim().isEmpty())
        {
            ps.setNull(i, java.sql.Types.TIME);
            return;
        }
        ps.setTime(i, Time.valueOf(parameter.trim()));
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException
    {
        Time t = rs.getTime(columnName);
        return t == null ? null : DateUtils.parseDateToStr("HH:mm:ss", t);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException
    {
        Time t = rs.getTime(columnIndex);
        return t == null ? null : DateUtils.parseDateToStr("HH:mm:ss", t);
    }

    @Override
    public String getNullableResult(java.sql.CallableStatement cs, int columnIndex) throws SQLException
    {
        Time t = cs.getTime(columnIndex);
        return t == null ? null : DateUtils.parseDateToStr("HH:mm:ss", t);
    }
}
