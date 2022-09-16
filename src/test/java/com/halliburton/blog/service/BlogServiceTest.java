package com.halliburton.blog.service;

import com.halliburton.blog.dao.BlogRepository;
import com.halliburton.blog.dao.PostRepository;
import com.halliburton.blog.dto.BlogDtoRequest;
import com.halliburton.blog.dto.BlogDtoRequestFull;
import com.halliburton.blog.dto.BlogModel;
import com.halliburton.blog.model.BlogEntity;
import com.halliburton.blog.model.PostEntity;
import com.halliburton.blog.modelassembler.BlogModelAssembler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.hateoas.CollectionModel;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public class BlogServiceTest {
    private final Long defaultBlogId = 1L;
    @Mock
    private BlogRepository blogRepository;
    @Mock
    private BlogModelAssembler blogModelAssembler;
    @Mock
    private PostRepository postRepository;
    @InjectMocks
    private BlogService service;
    private BlogEntity blogEntity;
    private BlogModel blogModel;
    private CollectionModel<BlogModel> blogModelCollection;
    private BlogDtoRequest blogDtoRequest;

    @Before
    public void setUp() {
        String blogTitle = "blog Title";
        String description = "blog description";

        MockitoAnnotations.openMocks(this);

        blogEntity = BlogEntity
                .builder()
                .id(defaultBlogId)
                .blogTitle(blogTitle)
                .description(description)
                .build();

        BlogModelAssembler blogModelAssemblerForTest = new BlogModelAssembler();
        blogModel = blogModelAssemblerForTest.toModel(blogEntity);
        blogModelCollection = blogModelAssemblerForTest
                .toCollectionModel(Collections.singletonList(blogEntity));

        blogDtoRequest = BlogDtoRequest
                .builder()
                .blogTitle(blogTitle)
                .description(description)
                .build();
    }

    @After
    public void tearDown() {
        blogEntity = null;
        blogModel = null;
        blogModelCollection = null;
        blogDtoRequest = null;
    }

    @Test
    public void getBlogByIdTest() {

        when(blogModelAssembler.toModel(blogEntity))
                .thenReturn(blogModel);
        when(blogRepository.findById(defaultBlogId))
                .thenReturn(Optional.ofNullable(blogEntity));

        Optional<BlogModel> actual = service.getBlogById(defaultBlogId);

        assertEquals(Optional.of(blogModel), actual);
    }

    @Test
    public void getAllBlogsTestWithParameters() {
        String author = "Neo";
        doReturn(blogModelCollection)
                .when(blogModelAssembler).toCollectionModel(Collections.emptyList());

        CollectionModel<BlogModel> actual = service.getAllBlogs(author, null);
        assertEquals(blogModelCollection, actual);
    }

    @Test
    public void getAllBlogsTestWithNullParameters() {
        when(blogRepository.findAll())
                .thenReturn(Collections.singletonList(blogEntity));
        when(blogModelAssembler.toCollectionModel(Collections.singletonList(blogEntity)))
                .thenReturn(blogModelCollection);

        CollectionModel<BlogModel> actual = service.getAllBlogs(null, null);
        assertEquals(blogModelCollection, actual);
    }

    @Test
    public void createBlogTest() {
        doReturn(blogModel)
                .when(blogModelAssembler).toModel(null);

        BlogModel actual = service.createBlog(blogDtoRequest);

        assertEquals(blogModel, actual);
    }

    @Test
    public void isTitlePresentTest() {
        when(blogRepository.findByBlogTitle(blogDtoRequest.getBlogTitle()))
                .thenReturn(Optional.ofNullable(blogEntity));

        boolean actual = service.isTitlePresent(blogDtoRequest);
        assertTrue(actual);
    }

    @Test
    public void updateBlogTest() {
        when(blogRepository.findById(defaultBlogId))
                .thenReturn(Optional.ofNullable(blogEntity));
        doReturn(blogModel)
                .when(blogModelAssembler).toModel(null);

        Optional<BlogModel> actual = service.updateBlog(defaultBlogId, blogDtoRequest);
        assertEquals(Optional.ofNullable(blogModel), actual);
    }

    @Test(expected = EntityNotFoundException.class)
    public void updateBlogTestWithEntityNotFoundException() {
        service.updateBlog(defaultBlogId, blogDtoRequest);
    }

    @Test
    public void updateBlogTestWithEmptyBlogModel() {
        blogDtoRequest.setBlogTitle("");
        blogDtoRequest.setDescription("");
        when(blogRepository.findById(defaultBlogId))
                .thenReturn(Optional.ofNullable(blogEntity));

        Optional<BlogModel> actual = service.updateBlog(defaultBlogId, blogDtoRequest);
        assertEquals(Optional.empty(), actual);
    }

    @Test
    public void updateBlogListTest() {
        BlogDtoRequestFull blogDtoRequestFull = BlogDtoRequestFull
                .builder()
                .id(defaultBlogId)
                .blogTitle(blogDtoRequest.getBlogTitle())
                .description(blogDtoRequest.getDescription())
                .build();
        List<BlogDtoRequestFull> blogDtoRequestFullList = Collections.singletonList(blogDtoRequestFull);

        doReturn(blogModelCollection)
                .when(blogModelAssembler).toCollectionModel(Collections.emptyList());

        CollectionModel<BlogModel> actual = service.updateBlogList(blogDtoRequestFullList);
        assertEquals(blogModelCollection, actual);
    }

    @Test
    public void deleteBlogTestReturnedTrue() {
        when(blogRepository.findById(defaultBlogId))
                .thenReturn(Optional.ofNullable(blogEntity));
        boolean actual = service.deleteBlog(defaultBlogId);
        assertTrue(actual);
    }

    @Test
    public void deleteBlogTestReturnedFalse() {
        when(blogRepository.findById(defaultBlogId))
                .thenReturn(Optional.empty());
        boolean actual = service.deleteBlog(defaultBlogId);
        assertFalse(actual);
    }


    @Test
    public void blogHasNestedObjectsByBlogIdReturnedTrue() {
        when(postRepository.findByBlog_Id(defaultBlogId))
                .thenReturn(Collections.singletonList(PostEntity.builder().build()));
        boolean actual = service.blogHasNestedObjects(defaultBlogId);
        assertTrue(actual);
    }

    @Test
    public void blogHasNestedObjectsByBlogIdReturnedFalse() {
        when(postRepository.findByBlog_Id(defaultBlogId))
                .thenReturn(new ArrayList<>());
        boolean actual = service.blogHasNestedObjects(defaultBlogId);
        assertFalse(actual);
    }

    @Test
    public void blogHasNestedObjectsReturnedTrue() {
        when(postRepository.findAll())
                .thenReturn(Collections.singletonList(PostEntity.builder().build()));
        boolean actual = service.blogHasNestedObjects();
        assertTrue(actual);
    }

    @Test
    public void blogHasNestedObjectsReturnedFalse() {
        when(postRepository.findAll())
                .thenReturn(new ArrayList<>());
        boolean actual = service.blogHasNestedObjects();
        assertFalse(actual);
    }
}