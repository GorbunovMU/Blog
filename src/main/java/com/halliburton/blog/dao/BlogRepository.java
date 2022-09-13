package com.halliburton.blog.dao;

import com.halliburton.blog.model.BlogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlogRepository extends JpaRepository<BlogEntity, Long> {
    Optional<BlogEntity> findByBlogTitle(String blogTitle);
}