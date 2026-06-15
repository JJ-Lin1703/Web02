package org.example.web02.mapper;

import org.apache.ibatis.annotations.Mapper;
//import org.apache.ibatis.annotations.Param;
import org.example.web02.entity.UserHealth;

@Mapper
public interface UserHealthMapper {

    UserHealth findById(Long id);

    UserHealth findByUserId(Long userId);

    int insert(UserHealth userHealth);

    int update(UserHealth userHealth);

    int deleteById(Long id);

    int existsByUserId(Long userId);
}
