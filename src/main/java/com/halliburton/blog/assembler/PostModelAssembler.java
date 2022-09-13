package com.halliburton.blog.assembler;

import com.halliburton.blog.controller.BlogController;
import com.halliburton.blog.controller.PostController;
import com.halliburton.blog.dto.PostModel;
import com.halliburton.blog.model.PostEntity;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
public class PostModelAssembler extends RepresentationModelAssemblerSupport<PostEntity, PostModel> {

    public PostModelAssembler() {
        super(PostController.class, PostModel.class);
    }

    @Override
    public PostModel toModel(PostEntity entity) {
        PostModel postModel = instantiateModel(entity);

        postModel.setId(entity.getId());
        postModel.setPostTitle(entity.getPostTitle());
        postModel.setPostBody(entity.getPostBody());
        postModel.setPostConclusion(entity.getPostConclusion());
        postModel.setPublishedOn(entity.getPublishedOn());
        postModel.setAuthor(entity.getAuthor());

        postModel.add(WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(PostController.class)
                        .getPostById(entity.getId()))
                .withSelfRel());
        postModel.add(WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(PostController.class)
                        .getAllPosts(Optional.empty()))
                .withRel("posts"));
        postModel.add(WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(PostController.class)
                        .getAllPostsByGivenBlog(entity.getBlog().getId(), null))
                .withRel("posts"));
        postModel.add(WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(BlogController.class)
                        .getAllBlogs(null, null))
                .withRel("blogs"));
        postModel.add(WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(BlogController.class)
                        .getBlogById(entity.getBlog().getId()))
                .withRel("blogs"));
        return postModel;
    }
}
