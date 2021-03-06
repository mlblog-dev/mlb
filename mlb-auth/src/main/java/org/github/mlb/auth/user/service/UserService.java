package org.github.mlb.auth.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.github.mlb.auth.user.entity.UserEntity;

/**
 * @author JiHongYuan
 * @date 2022/1/21 11:26
 */
public interface UserService extends IService<UserEntity> {

    /**
     * 根据 {@code acces} 查询用户实体
     *
     * @param access 第三方表示
     * @return user entity
     */
    UserEntity getByAccess(String access);

}
