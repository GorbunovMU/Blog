package com.halliburton.blog.controller;

import com.halliburton.blog.dto.BlogDtoRequest;
import com.halliburton.blog.dto.BlogDtoRequestFull;
import com.halliburton.blog.dto.BlogModel;
import com.halliburton.blog.service.BlogService;
import com.halliburton.blog.utils.BlogValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Tag(name = "REST API for Blog entity")
@RestController
@RequestMapping("/api/v1.0.0")
public class BlogController {
    final
    BlogService blogService;

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @Operation(summary = "Get a blog by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the blog",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Blog not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Error",
                    content = @Content)})

    @GetMapping(value = "/blogs/{id}", produces = {"application/json"})
    public ResponseEntity<BlogModel> getBlogById(
            @Parameter(description = "id of blog to be searched")
            @PathVariable Long id) {

        return blogService.getBlogById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get list of blogs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found blogs",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "204", description = "Blogs not found",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid parameters supplied",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Error",
                    content = @Content)})

    @GetMapping(value = "/blogs", produces = {"application/json"})
    public ResponseEntity<CollectionModel<BlogModel>> getAllBlogs(
            @Parameter(description = "author of post for search")
            @RequestParam(required = false, name = "author") String author,

            @Parameter(description = "publication date for search")
            @RequestParam(required = false, name = "date")
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {

        CollectionModel<BlogModel> blogModels = blogService.getAllBlogs(author, date);
        if (blogModels.getContent().isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(blogModels);
        }
    }

    @Operation(summary = "Create a new blog")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Blog created",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = BlogDtoRequest.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Duplicate blog title",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Error",
                    content = @Content)})

    @PostMapping(value = "/blogs", produces = {"application/json"})
    public ResponseEntity<BlogModel> createBlog(
            @Parameter(description = "new blog")
            @RequestBody BlogDtoRequest blog) {

        if (!BlogValidator.validateBlogEntityDto(blog)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, BlogValidator.getNotValidFieldsForBlogEntityDto(blog));
        }
        if (blogService.isTitlePresent(blog)) {
            return new ResponseEntity<>(null, HttpStatus.CONFLICT);
        }
        BlogModel createdBlog = blogService.createBlog(blog);
        if (createdBlog == null) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            return new ResponseEntity<>(createdBlog, HttpStatus.CREATED);
        }
    }

    @Operation(summary = "Update a blog by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Blog updated",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = BlogDtoRequest.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Invalid id supplied",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Error",
                    content = @Content)})

    @PatchMapping(value = "/blogs/{id}", produces = {"application/json"})
    public ResponseEntity<BlogModel> updateBlog(
            @Parameter(description = "id of blog to be searched")
            @PathVariable Long id,
            @Parameter(description = "blog updated information")
            @RequestBody BlogDtoRequest blogDtoRequest) {

        try {
            Optional<BlogModel> updatedBlog = blogService.updateBlog(id, blogDtoRequest);
            if (updatedBlog.isEmpty()) {
                return ResponseEntity.badRequest().build();
            } else {
                return ResponseEntity.ok(updatedBlog.get());
            }
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @Operation(summary = "Update array of blogs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Blogs updated",
                    content = {@Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = BlogDtoRequestFull.class)))}),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Error",
                    content = @Content)})

    @PutMapping(value = "/blogs", produces = {"application/json"})
    public ResponseEntity<CollectionModel<BlogModel>> updateBlogList(
            @Parameter(description = "Array of blogs to update")
            @RequestBody List<BlogDtoRequestFull> blogList) {

        String message = blogList.stream()
                .filter(blogDtoRequestFull -> !BlogValidator.validateFullBlogEntityDto(blogDtoRequestFull))
                .map(BlogValidator::getNotValidFieldsForFullBlogEntityDto)
                .collect(Collectors.joining(" and "));

        if (!message.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }

        try {
            CollectionModel<BlogModel> savedList = blogService.updateBlogList(blogList);
            return ResponseEntity.ok(savedList);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @Operation(summary = "Delete blog by its id. Blog can be only be deleted when all nested posts are also deleted")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "blog deleted",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Blog not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Error",
                    content = @Content)})
    @DeleteMapping(value = "/blogs/{id}")
    public ResponseEntity<String> deleteBlogById(
            @Parameter(description = "id of blog to be deleted")
            @PathVariable Long id) {

        if (blogService.blogHasNestedObjects(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Blog has nested objects (post entity)");
        }

        if (blogService.deleteBlog(id)) {
            return ResponseEntity.ok("Blog with id = " + id + " deleted");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete all blogs. Blogs can be only be deleted when all nested posts are also deleted")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All blogs deleted",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Error",
                    content = @Content)})
    @DeleteMapping(value = "/blogs")
    public ResponseEntity<String> deleteAllBlogs() {

        if (blogService.blogHasNestedObjects()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Blog has nested objects (post entity)");
        }

        blogService.deleteAllBlog();
        return ResponseEntity.ok("All Blogs deleted");
    }
}
