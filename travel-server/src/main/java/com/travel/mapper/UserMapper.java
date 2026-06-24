package com.travel.mapper;

import com.travel.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户 Mapper
 */
public interface UserMapper {

    /** 根据用户名查询 */
    User selectByUsername(@Param("username") String username);

    /** 根据手机号查询 */
    User selectByPhone(@Param("phone") String phone);

    /** 根据 ID 查询 */
    User selectById(@Param("userId") Integer userId);

    /** 插入用户 */
    int insert(User user);

    /** 更新用户信息（管理员用，可修改所有字段） */
    int updateById(User user);

    /** 更新用户个人资料（仅安全字段：昵称、手机号、邮箱、头像、性别、生日） */
    int updateUserProfile(User user);

    /** 检查用户名是否存在 */
    int countByUsername(@Param("username") String username);

    /** 检查手机号是否存在 */
    int countByPhone(@Param("phone") String phone);

    /** 查询所有用户 */
    List<User> selectAll();

    /** 分页查询用户（支持关键字搜索） */
    List<User> selectAllPaged(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);
    long countAll(@Param("keyword") String keyword);

    /** 删除用户 */
    int deleteById(@Param("userId") Integer userId);
}
