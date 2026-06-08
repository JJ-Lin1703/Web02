package org.example.web02.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.web02.entity.User;

import java.util.Date;
import java.util.List;

@Mapper
public interface UserMapper {

    User findById(Long id);

    User findByUsername(String username);

    int insert(User user);

    int update(User user);

    int updateLoginFailCount(@Param("id") Long id, @Param("failCount") Integer failCount, @Param("lockUntil") Date lockUntil);

    int updatePassword(@Param("id") Long id, @Param("password") String password);

    List<User> findAllUsers();

    int resetPassword(@Param("id") Long id, @Param("password") String password);
}