package com.halliburton.blog.service;

import com.halliburton.blog.assembler.PostModelAssembler;
import com.halliburton.blog.controller.BlogController;
import com.halliburton.blog.controller.PostController;
import com.halliburton.blog.dao.BlogRepository;
import com.halliburton.blog.dao.PostRepository;
import com.halliburton.blog.dto.PostDtoRequest;
import com.halliburton.blog.dto.PostDtoRequestFull;
import com.halliburton.blog.dto.PostModel;
import com.halliburton.blog.model.BlogEntity;
import com.halliburton.blog.model.PostEntity;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostService {

    final
    PostRepository postRepository;
    final
    BlogRepository blogRepository;
    final
    PostModelAssembler postModelAssembler;
    private final FullTextService fullTextService;

    public PostService(PostRepository postRepository, BlogRepository blogRepository, PostModelAssembler postModelAssembler, FullTextService fullTextService) {
        this.postRepository = postRepository;
        this.blogRepository = blogRepository;
        this.postModelAssembler = postModelAssembler;
        this.fullTextService = fullTextService;
    }


    public Optional<PostModel> getPostById(Long id) {

        return postRepository.findById(id)
                .map(postModelAssembler::toModel);
    }

    public CollectionModel<PostModel> getAllPosts(Optional<String> keyword) {
        List<PostEntity> postEntities;
        if (keyword.isPresent()) {
            List<Long> results;
            try {
                results = fullTextService.search(keyword.get())
                        .stream()
                        .map(fullTextSearchResult -> Long.valueOf(fullTextSearchResult.getKeys()))
                        .collect(Collectors.toList());
            } catch (SQLException e) {
                return null;
            }
            postEntities = postRepository.findByIdIn(results);
        } else {
            postEntities = postRepository.findAll();
        }
        CollectionModel<PostModel> postModels = postModelAssembler.toCollectionModel(postEntities);

        postModels.add(WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(PostController.class)
                        .getAllPosts(Optional.empty()))
                .withSelfRel());

        postModels.add(WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(BlogController.class)
                        .getAllBlogs(null, null))
                .withRel("blogs"));

        return postModels;
    }

    private Sort getSortGroup(String[] sort) {
        Sort groupBySort = null;
        for (String sortOrder : sort) {
            Sort.Direction direction;
            String[] _sortItem = sortOrder.split(":");
            if (_sortItem.length > 1) {
                direction = _sortItem[1].contains("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            } else {
                direction = Sort.Direction.DESC;
            }
            Sort itemSort = Sort.by(direction, _sortItem[0]);
            if (groupBySort == null) {
                groupBySort = itemSort;
            } else {
                groupBySort = groupBySort.and(itemSort);
            }
        }
        return groupBySort;
    }

    public CollectionModel<PostModel> getAllPostsByGivenBlog(Long blog_id, String[] sort)
            throws EntityNotFoundException, PropertyReferenceException {

        Optional<BlogEntity> blog = blogRepository.findById(blog_id);
        if (blog.isEmpty()) {
            throw new EntityNotFoundException("Entity with id = " + blog_id + " not found");
        }
        Sort groupBySort = getSortGroup(sort);
        PostEntity postEntity = PostEntity
                .builder()
                .blog(blog.get())
                .build();

        List<PostEntity> postEntities = postRepository.findAll(Example.of(postEntity), groupBySort);
        CollectionModel<PostModel> postModels = postModelAssembler.toCollectionModel(postEntities);

        postModels.add(WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(PostController.class)
                        .getAllPostsByGivenBlog(blog_id, null))
                .withSelfRel());

        postModels.add(WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(PostController.class)
                        .getAllPosts(Optional.empty()))
                .withRel("posts"));

        postModels.add(WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(BlogController.class)
                        .getAllBlogs(null, null))
                .withRel("blogs"));

        return postModels;
    }

    public PostModel createPost(Long blog_id, PostDtoRequest post) {
        Optional<BlogEntity> blog = blogRepository.findById(blog_id);
        if (blog.isEmpty()) {
            throw new EntityNotFoundException("Entity with id = " + blog_id + " not found");
        }

        PostEntity postEntity = PostEntity
                .builder()
                .blog(blog.get())
                .postTitle(post.getPostTitle())
                .postBody(post.getPostBody())
                .postConclusion(post.getPostConclusion())
                .author(post.getAuthor())
                .publishedOn(post.getPublishedOn())
                .build();

        return postModelAssembler.toModel(postRepository.save(postEntity));
    }

    public Optional<PostModel> updatePost(Long id, PostDtoRequest postDtoRequest) {
        boolean needsToUpdate = false;

        Optional<PostEntity> postToUpdate = postRepository.findById(id);
        if (postToUpdate.isEmpty()) {
            throw new EntityNotFoundException("Entity with id = " + id + " not found");
        }

        if (postDtoRequest.getPostTitle() != null && !postDtoRequest.getPostTitle().isBlank()) {
            postToUpdate.get().setPostTitle(postDtoRequest.getPostTitle());
            needsToUpdate = true;
        }
        if (postDtoRequest.getPostBody() != null && !postDtoRequest.getPostBody().isBlank()) {
            postToUpdate.get().setPostBody(postDtoRequest.getPostBody());
            needsToUpdate = true;
        }

        if (postDtoRequest.getPostConclusion() != null && !postDtoRequest.getPostConclusion().isBlank()) {
            postToUpdate.get().setPostConclusion(postDtoRequest.getPostConclusion());
            needsToUpdate = true;
        }
        if (postDtoRequest.getAuthor() != null && !postDtoRequest.getAuthor().isBlank()) {
            postToUpdate.get().setAuthor(postDtoRequest.getAuthor());
            needsToUpdate = true;
        }
        if (postDtoRequest.getPublishedOn() != null) {
            postToUpdate.get().setPublishedOn(postDtoRequest.getPublishedOn());
            needsToUpdate = true;
        }

        if (!needsToUpdate) {
            return Optional.empty();
        }

        PostEntity updatedEntity = postRepository.save(postToUpdate.get());
        return Optional.ofNullable(postModelAssembler.toModel(updatedEntity));
    }

    public CollectionModel<PostModel> updatePostList(List<PostDtoRequestFull> postList) {
        List<PostEntity> savedList = postRepository.saveAll(postList.stream()
                .map(postDtoRequestFull -> {
                    BlogEntity blog = blogRepository.getReferenceById(postDtoRequestFull.getBlogId());
                    return PostEntity
                            .builder()
                            .id(postDtoRequestFull.getId())
                            .blog(blog)
                            .postTitle(postDtoRequestFull.getPostTitle())
                            .postBody(postDtoRequestFull.getPostBody())
                            .postConclusion(postDtoRequestFull.getPostConclusion())
                            .author(postDtoRequestFull.getAuthor())
                            .publishedOn(postDtoRequestFull.getPublishedOn())
                            .build();
                })
                .collect(Collectors.toList()));

        CollectionModel<PostModel> postModels = postModelAssembler.toCollectionModel(savedList);

        postModels.add(WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(PostController.class)
                        .getAllPosts(Optional.empty()))
                .withSelfRel());

        postModels.add(WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(BlogController.class)
                        .getAllBlogs(null, null))
                .withRel("blogs"));

        return postModels;
    }

    public void deleteAllPost() {
        postRepository.deleteAll();
    }

    public boolean deletePost(Long id) {
        Optional<PostEntity> postEntity = postRepository.findById(id);
        if (postEntity.isPresent()) {
            postRepository.deleteById(id);
            return true;
        }

        return false;
    }

    public boolean deleteAllPostByBlogId(Long blogId) {
        List<PostEntity> postsToDelete = postRepository.findByBlog_Id(blogId);
        if (postsToDelete.size() > 0) {
            postRepository.deleteAllInBatch(postsToDelete);
            return true;
        }

        return false;
    }
}
