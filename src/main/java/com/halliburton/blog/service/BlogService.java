package com.halliburton.blog.service;

import com.halliburton.blog.modelassembler.BlogModelAssembler;
import com.halliburton.blog.dao.BlogRepository;
import com.halliburton.blog.dao.PostRepository;
import com.halliburton.blog.dto.BlogDtoRequest;
import com.halliburton.blog.dto.BlogDtoRequestFull;
import com.halliburton.blog.dto.BlogModel;
import com.halliburton.blog.model.BlogEntity;
import com.halliburton.blog.model.PostEntity;
import org.springframework.data.domain.Example;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BlogService {
    final
    BlogRepository blogRepository;

    final
    PostRepository postRepository;
    final
    BlogModelAssembler blogModelAssembler;

    public BlogService(BlogRepository blogRepository, PostRepository postRepository, BlogModelAssembler blogModelAssembler) {
        this.blogRepository = blogRepository;
        this.postRepository = postRepository;
        this.blogModelAssembler = blogModelAssembler;
    }

    public Optional<BlogModel> getBlogById(Long id) {

        return blogRepository.findById(id)
                .map(blogModelAssembler::toModel);
    }

    public CollectionModel<BlogModel> getAllBlogs(String author, Date date) {
        List<BlogEntity> blogEntities;
        LocalDate publishedOn = null;
        if (date != null) {
            publishedOn = date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

        if (author != null || date != null) {
            PostEntity postEntity = PostEntity
                    .builder()
                    .author(author)
                    .publishedOn(publishedOn)
                    .build();
            blogEntities = postRepository.findAll(Example.of(postEntity)).stream()
                    .map(PostEntity::getBlog)
                    .distinct()
                    .collect(Collectors.toList());
        } else {
            blogEntities = blogRepository.findAll();
        }

        return blogModelAssembler.toCollectionModel(blogEntities);
    }

    public BlogModel createBlog(BlogDtoRequest blog) {
        BlogEntity blogEntity = BlogEntity
                .builder()
                .blogTitle(blog.getBlogTitle())
                .description(blog.getDescription())
                .build();

        return blogModelAssembler.toModel(blogRepository.save(blogEntity));
    }

    public boolean isTitlePresent(BlogDtoRequest blog) {
        return blogRepository.findByBlogTitle(blog.getBlogTitle()).isPresent();
    }

    public Optional<BlogModel> updateBlog(Long id, BlogDtoRequest blogDtoRequest)
            throws EntityNotFoundException {

        boolean needsToUpdate = false;

        Optional<BlogEntity> blogToUpdate = blogRepository.findById(id);
        if (blogToUpdate.isEmpty()) {
            throw new EntityNotFoundException("Entity with id = " + id + " not found");
        }

        if (blogDtoRequest.getBlogTitle() != null && !blogDtoRequest.getBlogTitle().isBlank()) {
            blogToUpdate.get().setBlogTitle(blogDtoRequest.getBlogTitle());
            needsToUpdate = true;
        }
        if (blogDtoRequest.getDescription() != null && !blogDtoRequest.getDescription().isBlank()) {
            blogToUpdate.get().setDescription(blogDtoRequest.getDescription());
            needsToUpdate = true;
        }

        if (!needsToUpdate) {
            return Optional.empty();
        }

        BlogEntity updatedEntity = blogRepository.save(blogToUpdate.get());
        return Optional.ofNullable(blogModelAssembler.toModel(updatedEntity));

    }

    public CollectionModel<BlogModel> updateBlogList(List<BlogDtoRequestFull> blogList) {

        List<BlogEntity> savedList = blogRepository.saveAll(blogList.stream()
                .map(blogDtoRequestFull -> BlogEntity
                        .builder()
                        .id(blogDtoRequestFull.getId())
                        .blogTitle(blogDtoRequestFull.getBlogTitle())
                        .description(blogDtoRequestFull.getDescription())
                        .build())
                .collect(Collectors.toList()));
        return blogModelAssembler.toCollectionModel(savedList);
    }

    public boolean deleteBlog(Long id) {

        Optional<BlogEntity> blogEntity = blogRepository.findById(id);
        if (blogEntity.isPresent()) {
            blogRepository.deleteById(id);
            return true;
        }

        return false;
    }

    @Transactional
    public void deleteAllBlog() {
        blogRepository.deleteAll();
    }

    public boolean blogHasNestedObjects(Long blogId) {
        return postRepository.findByBlog_Id(blogId).size() > 0;
    }

    public boolean blogHasNestedObjects() {
        return postRepository.findAll().size() > 0;
    }
}
