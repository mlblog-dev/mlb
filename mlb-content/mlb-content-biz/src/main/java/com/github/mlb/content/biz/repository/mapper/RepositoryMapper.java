package com.github.mlb.content.biz.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.mlb.content.api.repository.entity.RepositoryEntity;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * @author JiHongYuan
 * @date 2021/9/18 9:40
 */
@Repository
public interface RepositoryMapper extends BaseMapper<RepositoryEntity> {

    @Select("select * from b_content_repository where slug = #{slug}")
    RepositoryEntity selectBySlug(String slug);

}
