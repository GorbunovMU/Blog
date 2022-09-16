package com.halliburton.blog.dao;

import com.halliburton.blog.model.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    List<PostEntity> findByIdIn(List<Long> results);

    List<PostEntity> findByBlog_Id(Long id);

}