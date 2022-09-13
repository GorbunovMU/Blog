package com.halliburton.blog.assembler;

import com.halliburton.blog.controller.BlogController;
import com.halliburton.blog.controller.PostController;
import com.halliburton.blog.dto.BlogModel;
import com.halliburton.blog.model.BlogEntity;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
public class BlogModelAssembler extends RepresentationModelAssemblerSupport<BlogEntity, BlogModel> {

    public BlogModelAssembler() {
        super(BlogController.class, BlogModel.class);
    }

    @Override
    public BlogModel toModel(BlogEntity entity) {
        BlogModel blogModel = instantiateModel(entity);

        blogModel.setId(entity.getId());
        blogModel.setBlogTitle(entity.getBlogTitle());
        blogModel.setDescription(entity.getDescription());

        blogModel.add(WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(BlogController.class)
                        .getBlogById(entity.getId()))
                .withSelfRel());
        blogModel.add(WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(BlogController.class)
                        .getAllBlogs(null, null))
                .withRel("blogs"));

        blogModel.add(WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(PostController.class)
                        .getAllPosts(Optional.empty()))
                .withRel("posts"));

        blogModel.add(WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(PostController.class)
                        .getAllPostsByGivenBlog(entity.getId(), null))
                .withRel("posts"));

        return blogModel;
    }

    @Override
    public CollectionModel<BlogModel> toCollectionModel(Iterable<? extends BlogEntity> entities) {
        CollectionModel<BlogModel> blogModels = super.toCollectionModel(entities);
        blogModels.add(WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(BlogController.class)
                        .getAllBlogs(null, null))
                .withSelfRel());
        blogModels.add(WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(PostController.class)
                        .getAllPosts(Optional.empty()))
                .withRel("posts"));

        return blogModels;
    }
}
