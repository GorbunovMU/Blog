package com.halliburton.blog.service;

import com.halliburton.blog.dao.BlogRepository;
import com.halliburton.blog.dao.PostRepository;
import com.halliburton.blog.dto.PostDtoRequest;
import com.halliburton.blog.dto.PostDtoRequestFull;
import com.halliburton.blog.dto.PostModel;
import com.halliburton.blog.model.BlogEntity;
import com.halliburton.blog.model.PostEntity;
import com.halliburton.blog.modelassembler.PostModelAssembler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.hateoas.CollectionModel;

import javax.persistence.EntityNotFoundException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public class PostServiceTest {
    private final Long defaultPostId = 1L;
    private final Long defaultBlogId = 1L;
    @Mock
    private PostRepository postRepository;
    @Mock
    private PostModelAssembler postModelAssembler;
    @Mock
    private BlogRepository blogRepository;
    @Mock
    private FullTextService fullTextService;
    @InjectMocks
    private PostService service;
    private PostEntity postEntity;
    private PostModel postModel;
    private CollectionModel<PostModel> postModelCollection;
    private PostDtoRequest postDtoRequest;

    @Before
    public void setUp() {
        String title = "Beauty Post";
        String body = "Twin 4-month-olds slept in the shade of the palm tree while the mother tanned in the sun.";
        String conclusion = "post conclusion";
        String author = "post author";
        LocalDate publishedOn = LocalDate.of(2022, 9, 14);

        MockitoAnnotations.openMocks(this);

        postEntity = PostEntity
                .builder()
                .id(defaultPostId)
                .blog(BlogEntity.builder().id(1L).build())
                .postTitle(title)
                .postBody(body)
                .postConclusion(conclusion)
                .author(author)
                .publishedOn(publishedOn)
                .build();

        PostModelAssembler postModelAssemblerForTest = new PostModelAssembler();
        postModel = postModelAssemblerForTest.toModel(postEntity);
        postModelCollection = postModelAssemblerForTest
                .toCollectionModel(Collections.singletonList(postEntity));

        postDtoRequest = PostDtoRequest
                .builder()
                .postTitle(title)
                .postBody(body)
                .postConclusion(conclusion)
                .author(author)
                .publishedOn(publishedOn)
                .build();
    }

    @After
    public void tearDown() {
        postEntity = null;
        postModel = null;
        postModelCollection = null;
        postDtoRequest = null;
    }

    @Test
    public void getPostByIdTest() {

        when(postModelAssembler.toModel(postEntity))
                .thenReturn(postModel);
        when(postRepository.findById(defaultPostId))
                .thenReturn(Optional.ofNullable(postEntity));

        Optional<PostModel> actual = service.getPostById(defaultPostId);

        assertEquals(Optional.of(postModel), actual);
    }

    @Test
    public void getAllPostsTestWithParameters() throws SQLException {
        Optional<String> keyword = Optional.of("qwerty");

        doReturn(Collections.emptyList())
                .when(fullTextService).search(keyword.get());
        doReturn(postModelCollection)
                .when(postModelAssembler).toCollectionModel(Collections.emptyList());

        CollectionModel<PostModel> actual = service.getAllPosts(keyword);
        assertEquals(postModelCollection, actual);
    }

    @Test
    public void getAllPostsTestWithParametersReturnedNull() throws SQLException {
        Optional<String> keyword = Optional.of("qwerty");

        when(fullTextService.search(keyword.get()))
                .thenThrow(SQLException.class);

        CollectionModel<PostModel> actual = service.getAllPosts(keyword);
        assertNull(actual);
    }

    @Test
    public void getAllPostsTestWithEmptyParameters() {
        doReturn(Collections.singletonList(postEntity))
                .when(postRepository).findAll();
        when(postModelAssembler.toCollectionModel(Collections.singletonList(postEntity)))
                .thenReturn(postModelCollection);

        CollectionModel<PostModel> actual = service.getAllPosts(Optional.empty());
        assertEquals(postModelCollection, actual);
    }

    @Test
    public void getAllPostsByGivenBlogTest() {
        String[] sort = {"id:desc"};

        doReturn(Optional.of(BlogEntity.builder().id(defaultBlogId).build()))
                .when(blogRepository).findById(defaultBlogId);
        when(postModelAssembler.toCollectionModel(Collections.emptyList()))
                .thenReturn(postModelCollection);

        CollectionModel<PostModel> actual = service.getAllPostsByGivenBlog(defaultBlogId, sort);
        assertEquals(postModelCollection, actual);
    }

    @Test(expected = EntityNotFoundException.class)
    public void getAllPostsByGivenBlogTestWithEntityNotFoundException() {
        String[] sort = {"id:desc"};

        doReturn(Optional.empty())
                .when(blogRepository).findById(defaultBlogId);

        service.getAllPostsByGivenBlog(defaultBlogId, sort);
    }

    @Test
    public void getAllPostsByGivenBlogTestWithSortByFewFields() {
        String[] sort = {"id:desc", "postTitle:esc", "postBody"};

        doReturn(Optional.of(BlogEntity.builder().id(defaultBlogId).build()))
                .when(blogRepository).findById(defaultBlogId);
        when(postModelAssembler.toCollectionModel(Collections.emptyList()))
                .thenReturn(postModelCollection);

        CollectionModel<PostModel> actual = service.getAllPostsByGivenBlog(defaultBlogId, sort);
        assertEquals(postModelCollection, actual);
    }

    @Test
    public void createPostTest() {
        doReturn(Optional.of(BlogEntity.builder().id(defaultBlogId).build()))
                .when(blogRepository).findById(defaultBlogId);
        doReturn(postModel)
                .when(postModelAssembler).toModel(null);

        PostModel actual = service.createPost(defaultBlogId, postDtoRequest);

        assertEquals(postModel, actual);
    }

    @Test(expected = EntityNotFoundException.class)
    public void createPostTestWithEntityNotFoundException() {
        doReturn(Optional.empty())
                .when(blogRepository).findById(defaultBlogId);

        service.createPost(defaultBlogId, postDtoRequest);
    }

    @Test
    public void updatePostTest() {
        when(postRepository.findById(defaultPostId))
                .thenReturn(Optional.ofNullable(postEntity));
        doReturn(postModel)
                .when(postModelAssembler).toModel(null);

        Optional<PostModel> actual = service.updatePost(defaultPostId, postDtoRequest);
        assertEquals(Optional.ofNullable(postModel), actual);
    }

    @Test(expected = EntityNotFoundException.class)
    public void updatePostTestWithEntityNotFoundException() {
        service.updatePost(defaultPostId, postDtoRequest);
    }

    @Test
    public void updatePostTestWithEmptyPostModel() {
        postDtoRequest.setPostTitle("");
        postDtoRequest.setPostBody("");
        postDtoRequest.setPostConclusion("");
        postDtoRequest.setAuthor("");
        postDtoRequest.setPublishedOn(null);

        when(postRepository.findById(defaultPostId))
                .thenReturn(Optional.ofNullable(postEntity));

        Optional<PostModel> actual = service.updatePost(defaultPostId, postDtoRequest);
        assertEquals(Optional.empty(), actual);
    }

    @Test
    public void updatePostListTest() {
        PostDtoRequestFull postDtoRequestFull = PostDtoRequestFull
                .builder()
                .id(defaultPostId)
                .blogId(defaultBlogId)
                .postTitle(postDtoRequest.getPostTitle())
                .postBody(postDtoRequest.getPostBody())
                .postConclusion(postDtoRequest.getPostConclusion())
                .author(postDtoRequest.getAuthor())
                .publishedOn(postDtoRequest.getPublishedOn())
                .build();
        List<PostDtoRequestFull> postDtoRequestFullList = Collections.singletonList(postDtoRequestFull);

        doReturn(postModelCollection)
                .when(postModelAssembler).toCollectionModel(Collections.emptyList());

        CollectionModel<PostModel> actual = service.updatePostList(postDtoRequestFullList);
        assertEquals(postModelCollection, actual);
    }

    @Test
    public void deletePostByPostIdTestReturnedTrue() {
        when(postRepository.findById(defaultPostId))
                .thenReturn(Optional.ofNullable(postEntity));
        boolean actual = service.deletePost(defaultPostId);
        assertTrue(actual);
    }

    @Test
    public void deletePostByPostIdTestReturnedFalse() {
        when(postRepository.findById(defaultPostId))
                .thenReturn(Optional.empty());
        boolean actual = service.deletePost(defaultPostId);
        assertFalse(actual);
    }

    @Test
    public void deleteAllPostsByBlogIdTestReturnedTrue() {
        doReturn(Collections.singletonList(postEntity))
                .when(postRepository).findByBlog_Id(defaultBlogId);

        boolean actual = service.deleteAllPostByBlogId(defaultBlogId);
        assertTrue(actual);
    }

    @Test
    public void deleteAllPostsByBlogIdTestReturnedFalse() {
        doReturn(Collections.emptyList())
                .when(postRepository).findByBlog_Id(defaultBlogId);

        boolean actual = service.deleteAllPostByBlogId(defaultBlogId);
        assertFalse(actual);
    }

}