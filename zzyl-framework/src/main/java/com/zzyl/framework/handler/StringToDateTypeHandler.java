package com.zzyl.framework.handler;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import com.zzyl.common.utils.DateUtils;

/**
 * MyBatis TypeHandler: String ↔ java.sql.Date
 * <p>
 * 配套 visit_record.visit_date varchar(20) → DATE 的改造:
 *   - 写入: Java String "yyyy-MM-dd"  / "" → NULL  → java.sql.Date
 *   - 读取: java.sql.Date             → Java String "yyyy-MM-dd" / NULL
 * <p>
 * 见 DATABASE_OPTIMIZATION_RECOMMENDATIONS.md §P0-3 / sql/patch_20260724_visit_record_types.sql
 *
 * @author zzyl
 */
@MappedTypes(String.class)
public class StringToDateTypeHandler extends BaseTypeHandler<String>
{
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException
    {
        // 空串归 NULL（与 visit.sql DDL default null 保持一致）
        if (parameter == null || parameter.trim().isEmpty())
        {
            ps.setNull(i, java.sql.Types.DATE);
            return;
        }
        ps.setDate(i, Date.valueOf(parameter.trim()));
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException
    {
        Date d = rs.getDate(columnName);
        return d == null ? null : DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, d);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException
    {
        Date d = rs.getDate(columnIndex);
        return d == null ? null : DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, d);
    }

    @Override
    public String getNullableResult(java.sql.CallableStatement cs, int columnIndex) throws SQLException
    {
        Date d = cs.getDate(columnIndex);
        return d == null ? null : DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, d);
    }
}
