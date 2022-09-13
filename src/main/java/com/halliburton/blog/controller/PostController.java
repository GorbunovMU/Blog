package com.halliburton.blog.controller;

import com.halliburton.blog.dto.PostDtoRequest;
import com.halliburton.blog.dto.PostDtoRequestFull;
import com.halliburton.blog.dto.PostModel;
import com.halliburton.blog.service.PostService;
import com.halliburton.blog.utils.PostValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Tag(name = "REST API for Post entity")
@RestController
@RequestMapping("/api/v1.0.0")
public class PostController {

    final
    PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Operation(summary = "Get a post by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the post",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Post not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Error",
                    content = @Content)})

    @GetMapping(value = "/posts/{id}", produces = {"application/json"})
    public ResponseEntity<PostModel> getPostById(
            @Parameter(description = "id of post to be searched")
            @PathVariable Long id) {

        return postService.getPostById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get list of posts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found posts",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "204", description = "Posts not found",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid parameters supplied",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Error",
                    content = @Content)})

    @GetMapping(value = "/posts", produces = {"application/json"})
    public ResponseEntity<CollectionModel<PostModel>> getAllPosts(
            @Parameter(description = "text for search in the title or the body")
            @RequestParam(name = "keyword") Optional<String> keyword) {


        CollectionModel<PostModel> postModel = postService.getAllPosts(keyword);
        if (postModel == null) {
            return ResponseEntity.internalServerError().build();
        } else if (postModel.getContent().isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(postModel);
        }
    }

    @Operation(summary = "Get a list of Posts from a given blog sorted by default by id in descending order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found posts",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "204", description = "Posts not found",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid parameters supplied",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Blog not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Error",
                    content = @Content)})
    @GetMapping(value = "/blogs/{id}/posts", produces = {"application/json"})
    public ResponseEntity<CollectionModel<PostModel>> getAllPostsByGivenBlog(
            @Parameter(description = "id of blog to be searched")
            @PathVariable("id") Long blog_id,
            @Parameter(description = "array of fields to sort by direction (asc or desc)")
            @RequestParam(required = false, defaultValue = "id:desc") String[] sort) {

        try {
            CollectionModel<PostModel> postModel = postService.getAllPostsByGivenBlog(blog_id, sort);
            if (postModel.getContent().isEmpty()) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.ok(postModel);
            }
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (PropertyReferenceException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @Operation(summary = "Create a new post into a blog")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Post created",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = PostDtoRequest.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Blog not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Error",
                    content = @Content)})

    @PostMapping(value = "/blogs/{id}/posts", produces = {"application/json"})
    public ResponseEntity<PostModel> createPost(
            @Parameter(description = "id of the blog where the post is being inserted")
            @PathVariable("id") Long blog_id,
            @Parameter(description = "new post")
            @RequestBody PostDtoRequest post) {

        if (!PostValidator.validatePostEntityDto(post)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    PostValidator.getNotValidFieldsForPostEntityDto(post));
        }
        try {
            PostModel createdPost = postService.createPost(blog_id, post);
            if (createdPost == null) {
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
            }
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @Operation(summary = "Update a post by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post updated",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = PostDtoRequest.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Invalid id supplied",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Error",
                    content = @Content)})

    @PatchMapping(value = "/posts/{id}", produces = {"application/json"})
    public ResponseEntity<PostModel> updatePost(
            @Parameter(description = "id of post to be searched")
            @PathVariable Long id,
            @Parameter(description = "post updated information")
            @RequestBody PostDtoRequest postDtoRequest) {

        try {
            Optional<PostModel> updatedPost = postService.updatePost(id, postDtoRequest);
            if (updatedPost.isEmpty()) {
                return ResponseEntity.badRequest().build();
            } else {
                return ResponseEntity.ok(updatedPost.get());
            }
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @Operation(summary = "Update array of posts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts updated",
                    content = {@Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PostDtoRequestFull.class)))}),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Error",
                    content = @Content)})

    @PutMapping(value = "/posts", produces = {"application/json"})
    public ResponseEntity<CollectionModel<PostModel>> updatePostList(
            @Parameter(description = "Array of posts to update")
            @RequestBody List<PostDtoRequestFull> postList) {

        String message = postList.stream()
                .filter(postDtoRequestFull -> !PostValidator.validateFullPostEntityDto(postDtoRequestFull))
                .map(PostValidator::getNotValidFieldsForFullPostEntityDto)
                .collect(Collectors.joining(" and "));

        if (!message.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }

        try {
            CollectionModel<PostModel> savedList = postService.updatePostList(postList);
            return ResponseEntity.ok(savedList);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @Operation(summary = "Delete all posts.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All posts deleted",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Error",
                    content = @Content)})
    @DeleteMapping(value = "/posts")
    public ResponseEntity<String> deleteAllPosts() {
        postService.deleteAllPost();
        return ResponseEntity.ok("All Posts deleted");
    }

    @Operation(summary = "Delete post by its id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post deleted",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Post not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Error",
                    content = @Content)})

    @DeleteMapping(value = "/posts/{id}")
    public ResponseEntity<String> deletePostById(
            @Parameter(description = "id of post to be deleted")
            @PathVariable Long id) {

        if (postService.deletePost(id)) {
            return ResponseEntity.ok("Post with id = " + id + " deleted");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete all post from a given blog by blogId.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts deleted",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Blog not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Error",
                    content = @Content)})

    @DeleteMapping(value = "/blogs/{id}/posts")
    public ResponseEntity<String> deleteAllPostByBlogId(
            @Parameter(description = "id of the blog where all posts should be deleted")
            @PathVariable(name = "id") Long blogId) {

        if (postService.deleteAllPostByBlogId(blogId)) {
            return ResponseEntity.ok("All posts with blogId = " + blogId + " deleted");
        } else {
            return ResponseEntity.notFound().build();
        }
    }


}
