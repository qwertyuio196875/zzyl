package com.zzyl.framework.mpdemo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 阶段 ② 验证实体（mp_demo_user 表）。
 *
 * 故意不继承 RuoYi 的 BaseEntity —— BaseEntity 有 searchValue / params 等
 * 非表字段，直接交给 BaseMapper 会报 "Unknown column 'search_value'"。
 */
@TableName("mp_demo_user")
public class DemoUser {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer age;
    private String email;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    @Override
    public String toString() {
        return "DemoUser{id=" + id + ", name='" + name + "', age=" + age
                + ", email='" + email + "', createTime=" + createTime + '}';
    }
}
