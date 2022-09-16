package com.halliburton.blog.controller;

import com.halliburton.blog.dto.PostDtoRequest;
import com.halliburton.blog.dto.PostDtoRequestFull;
import com.halliburton.blog.dto.PostModel;
import com.halliburton.blog.model.BlogEntity;
import com.halliburton.blog.model.PostEntity;
import com.halliburton.blog.modelassembler.PostModelAssembler;
import com.halliburton.blog.service.PostService;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class PostControllerTest {

    private static final String title = "Beauty Post";
    private static final String body = "Twin 4-month-olds slept in the shade of the palm tree while the mother tanned in the sun.";
    private static final String postConclusion = "post conclusion";
    private static final String author = "post author";
    private static final LocalDate publishedOn = LocalDate.of(2022, 9, 14);
    private final Long defaultBlogId = 1L;
    private final Long defaultPostId = 1L;
    @MockBean
    PostService postService;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private PostModelAssembler postModelAssembler;
    private PostEntity fakeEntityObject;
    private PostDtoRequest fakeDtoObject;
    private JSONObject fakeJsonObject;

    @Before
    public void setUp() throws Exception {
        fakeEntityObject = PostEntity
                .builder()
                .id(defaultPostId)
                .blog(BlogEntity.builder().id(defaultBlogId).build())
                .postTitle(title)
                .postBody(body)
                .postConclusion(postConclusion)
                .author(author)
                .publishedOn(publishedOn)
                .build();

        fakeDtoObject = PostDtoRequest
                .builder()
                .postTitle(title)
                .postBody(body)
                .postConclusion(postConclusion)
                .author(author)
                .publishedOn(publishedOn)
                .build();

        fakeJsonObject = new JSONObject();
        fakeJsonObject.put("postTitle", title);
        fakeJsonObject.put("postBody", body);
        fakeJsonObject.put("postConclusion", postConclusion);
        fakeJsonObject.put("author", author);
        fakeJsonObject.put("publishedOn", publishedOn);
    }

    @After
    public void tearDown() {
        fakeEntityObject = null;
        fakeJsonObject = null;
        fakeDtoObject = null;
    }

    @Test
    public void givenPostItem_whenGetPostByPostId_thenReturnJsonObject() throws Exception {
        PostModel postModel = postModelAssembler.toModel(fakeEntityObject);
        given(postService.getPostById(defaultPostId)).willReturn(Optional.of(postModel));

        this.mvc.perform(get("/api/v1.0.0/posts/" + defaultPostId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(7)))
                .andExpect(jsonPath("$.links", hasSize(5)))
                .andExpect(jsonPath("$.id", is(fakeEntityObject.getId().intValue())))
                .andExpect(jsonPath("$.postTitle", is(fakeEntityObject.getPostTitle())));

    }

    @Test
    public void givenPostItem_whenGetPostById_thenReturnNoContentResponse() throws Exception {
        given(postService.getPostById(defaultPostId)).willReturn(Optional.empty());

        this.mvc.perform(get("/api/v1.0.0/posts/" + defaultPostId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenPostItems_whenGetAllPosts_thenReturnJsonArray() throws Exception {
        List<PostEntity> allPosts = Collections.singletonList(fakeEntityObject);
        CollectionModel<PostModel> postModels = postModelAssembler.toCollectionModel(allPosts);
        given(postService.getAllPosts(Optional.empty())).willReturn(postModels);

        this.mvc.perform(get("/api/v1.0.0/posts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(2)))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(fakeEntityObject.getId().intValue())))
                .andExpect(jsonPath("$.content[0].postTitle", is(fakeEntityObject.getPostTitle())));

    }

    @Test
    public void givenPostItems_whenGetAllPosts_thenReturnNoContentResponse() throws Exception {
        CollectionModel<PostModel> postModels = CollectionModel.empty();
        given(postService.getAllPosts(Optional.empty())).willReturn(postModels);

        this.mvc.perform(get("/api/v1.0.0/posts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void givenPostItems_whenGetAllPosts_thenReturnInternalServerErrorResponse() throws Exception {
        given(postService.getAllPosts(Optional.empty())).willReturn(null);

        this.mvc.perform(get("/api/v1.0.0/posts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void givenPostItems_whenGetAllPostsByGivenBlog_thenReturnJsonArray() throws Exception {
        String sort = "id:desc";
        List<PostEntity> allPosts = Collections.singletonList(fakeEntityObject);
        CollectionModel<PostModel> postModels = postModelAssembler.toCollectionModel(allPosts);
        given(postService.getAllPostsByGivenBlog(defaultBlogId, new String[]{sort})).willReturn(postModels);

        this.mvc.perform(get("/api/v1.0.0/blogs/"+ defaultBlogId+"/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sort))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(2)))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(fakeEntityObject.getId().intValue())))
                .andExpect(jsonPath("$.content[0].postTitle", is(fakeEntityObject.getPostTitle())));

    }

    @Test
    public void givenPostItems_whenGetAllPostsByGivenBlog_thenReturnNoContentResponse() throws Exception {
        String sort = "id:desc";
        given(postService.getAllPostsByGivenBlog(defaultBlogId, new String[]{sort})).willReturn(CollectionModel.empty());

        this.mvc.perform(get("/api/v1.0.0/blogs/"+ defaultBlogId+"/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sort))
                .andExpect(status().isNoContent());

    }

    @Test
    public void givenPostItems_whenGetAllPostsByGivenBlog_thenReturnNotFoundResponse() throws Exception {
        String sort = "id:desc";
        given(postService.getAllPostsByGivenBlog(defaultBlogId, new String[]{sort}))
                .willThrow(EntityNotFoundException.class);

        this.mvc.perform(get("/api/v1.0.0/blogs/"+ defaultBlogId+"/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sort))
                .andExpect(status().isNotFound());

    }

    @Test(expected = NestedServletException.class)
    public void givenPostItems_whenGetAllPostsByGivenBlog_thenReturnBadRequestResponse() throws Exception {

        String sort = "id:desc";
        given(postService.getAllPostsByGivenBlog(defaultBlogId, new String[]{sort}))
                .willThrow(PropertyReferenceException.class);

        this.mvc.perform(get("/api/v1.0.0/blogs/"+ defaultBlogId+"/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sort))
                .andExpect(status().isBadRequest());

    }

    @Test
    public void givenJsonObject_whenCreatePost_thenReturnJsonObject() throws Exception {
        PostModel postModel = postModelAssembler.toModel(fakeEntityObject);

        given(postService.createPost(defaultBlogId, fakeDtoObject)).willReturn(postModel);

        this.mvc.perform(post("/api/v1.0.0/blogs/" + defaultBlogId + "/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fakeJsonObject.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.*", hasSize(7)))
                .andExpect(jsonPath("$.links", hasSize(5)))
                .andExpect(jsonPath("$.postBody", is(fakeEntityObject.getPostBody())))
                .andExpect(jsonPath("$.postTitle", is(fakeEntityObject.getPostTitle())));

    }

    @Test
    public void givenJsonObject_whenCreatePost_thenReturnInternalServerErrorResponse() throws Exception {

        given(postService.createPost(defaultBlogId, fakeDtoObject)).willReturn(null);

        this.mvc.perform(post("/api/v1.0.0/blogs/" + defaultBlogId + "/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fakeJsonObject.toString()))
                .andExpect(status().isInternalServerError());

    }

    @Test
    public void givenJsonObject_whenCreatePost_thenReturnBadRequestResponse() throws Exception {

        fakeJsonObject.put("postTitle", null);
        fakeJsonObject.put("postBody", null);
        fakeJsonObject.put("postConclusion", null);
        fakeJsonObject.put("author", null);
        fakeJsonObject.put("publishedOn", null);

        this.mvc.perform(post("/api/v1.0.0/blogs/" + defaultBlogId + "/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fakeJsonObject.toString()))
                .andExpect(status().isBadRequest());

    }

    @Test
    public void givenJsonObject_whenCreatePost_thenReturnNotFoundResponse() throws Exception {

        given(postService.createPost(defaultBlogId, fakeDtoObject))
                .willThrow(EntityNotFoundException.class);

        this.mvc.perform(post("/api/v1.0.0/blogs/" + defaultBlogId + "/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fakeJsonObject.toString()))
                .andExpect(status().isNotFound());

    }

    @Test
    public void givenJsonObject_whenUpdatePost_thenReturnJsonObject() throws Exception {
        PostModel postModel = postModelAssembler.toModel(fakeEntityObject);

        given(postService.updatePost(defaultPostId, fakeDtoObject)).willReturn(Optional.of(postModel));

        this.mvc.perform(patch("/api/v1.0.0/posts/" + defaultPostId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fakeJsonObject.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(7)))
                .andExpect(jsonPath("$.links", hasSize(5)))
                .andExpect(jsonPath("$.id", is(fakeEntityObject.getId().intValue())))
                .andExpect(jsonPath("$.postBody", is(fakeEntityObject.getPostBody())))
                .andExpect(jsonPath("$.postTitle", is(fakeEntityObject.getPostTitle())));

    }

    @Test
    public void givenJsonObject_whenUpdatePost_thenReturnBadRequestResponse() throws Exception {
        given(postService.updatePost(defaultPostId, fakeDtoObject)).willReturn(Optional.empty());

        this.mvc.perform(patch("/api/v1.0.0/posts/" + defaultPostId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fakeJsonObject.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenJsonObject_whenUpdatePost_thenReturnNotFoundResponse() throws Exception {
        given(postService.updatePost(defaultPostId, fakeDtoObject)).willThrow(EntityNotFoundException.class);

        this.mvc.perform(patch("/api/v1.0.0/posts/" + defaultPostId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fakeJsonObject.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenJsonObjectList_updatePostList_thenReturnJsonObjectList() throws Exception {
        PostDtoRequestFull postDtoRequestFull = PostDtoRequestFull
                .builder()
                .id(defaultPostId)
                .blogId(defaultBlogId)
                .postTitle(title)
                .postBody(body)
                .postConclusion(postConclusion)
                .author(author)
                .publishedOn(publishedOn)
                .build();

        fakeJsonObject.put("id", defaultPostId);
        fakeJsonObject.put("blogId", defaultBlogId);

        String jsonArray = "[" + fakeJsonObject.toString() + "]";

        CollectionModel<PostModel> postModels = postModelAssembler.toCollectionModel(
                Collections.singletonList(fakeEntityObject));

        given(postService.updatePostList(Collections.singletonList(postDtoRequestFull))).willReturn(postModels);

        this.mvc.perform(put("/api/v1.0.0/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonArray))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(fakeEntityObject.getId().intValue())))
                .andExpect(jsonPath("$.content[0].postTitle", is(fakeEntityObject.getPostTitle())));

    }

    @Test
    public void givenJsonObjectList_updatePostList_thenReturnBadRequestResponse() throws Exception {
        String jsonArray = "[" + fakeJsonObject.toString() + "]";

        this.mvc.perform(put("/api/v1.0.0/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonArray))
                .andExpect(status().isBadRequest());

    }

    @Test
    public void givenJsonObjectList_updatePostList_thenReturnBadRequestResponseFromService() throws Exception {
        PostDtoRequestFull postDtoRequestFull = PostDtoRequestFull
                .builder()
                .id(defaultPostId)
                .blogId(defaultBlogId)
                .postTitle(title)
                .postBody(body)
                .postConclusion(postConclusion)
                .author(author)
                .publishedOn(publishedOn)
                .build();

        fakeJsonObject.put("id", defaultPostId);
        fakeJsonObject.put("blogId", defaultBlogId);

        String jsonArray = "[" + fakeJsonObject.toString() + "]";

        given(postService.updatePostList(
                Collections.singletonList(postDtoRequestFull))).willThrow(RuntimeException.class);

        this.mvc.perform(put("/api/v1.0.0/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonArray))
                .andExpect(status().isBadRequest());
    }


    @Test
    public void givenPostId_whenDeletePost_thenReturnOkResponse() throws Exception {
        given(postService.deletePost(defaultPostId)).willReturn(true);

        this.mvc.perform(delete("/api/v1.0.0/posts/" + defaultPostId))
                .andExpect(status().isOk());
    }

    @Test
    public void givenPostId_whenDeletePost_thenReturnNotFoundResponse() throws Exception {
        given(postService.deletePost(defaultPostId)).willReturn(false);

        this.mvc.perform(delete("/api/v1.0.0/posts/" + defaultPostId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenEmpty_whenDeleteAllPostByBlogId_thenReturnOkResponse() throws Exception {
        given(postService.deleteAllPostByBlogId(defaultBlogId)).willReturn(true);

        this.mvc.perform(delete("/api/v1.0.0/blogs/" + defaultBlogId + "/posts"))
                .andExpect(status().isOk());
    }

    @Test
    public void givenEmpty_whenDeleteAllPostByBlogId_thenReturnBadRequestResponse() throws Exception {
        given(postService.deleteAllPostByBlogId(defaultBlogId)).willReturn(false);

        this.mvc.perform(delete("/api/v1.0.0/blogs/" + defaultBlogId + "/posts"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenEmpty_whenDeleteAllPosts_thenReturnOkResponse() throws Exception {

        this.mvc.perform(delete("/api/v1.0.0/posts"))
                .andExpect(status().isOk());
    }
}