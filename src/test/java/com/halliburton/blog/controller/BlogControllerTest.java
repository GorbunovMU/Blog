package com.halliburton.blog.controller;

import com.halliburton.blog.dto.BlogDtoRequest;
import com.halliburton.blog.dto.BlogDtoRequestFull;
import com.halliburton.blog.dto.BlogModel;
import com.halliburton.blog.model.BlogEntity;
import com.halliburton.blog.modelassembler.BlogModelAssembler;
import com.halliburton.blog.service.BlogService;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.persistence.EntityNotFoundException;
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
public class BlogControllerTest {

    private final Long defaultId = 1L;
    @MockBean
    BlogService blogService;
    private static final String title = "Beauty Blog";
    private static final String description = "Twin 4-month-olds slept in the shade of the palm tree while the mother tanned in the sun.";
    @Autowired
    private MockMvc mvc;
    @Autowired
    private BlogModelAssembler blogModelAssembler;
    private BlogEntity fakeEntityObject;
    private BlogDtoRequest fakeDtoObject;
    private JSONObject fakeJsonObject;

    @Before
    public void setUp() throws Exception {
        fakeEntityObject = BlogEntity
                .builder()
                .id(defaultId)
                .blogTitle(title)
                .description(description)
                .build();

        fakeDtoObject = new BlogDtoRequest(title, description);

        fakeJsonObject = new JSONObject();
        fakeJsonObject.put("blogTitle", title);
        fakeJsonObject.put("description", description);

    }

    @After
    public void tearDown() {
        fakeEntityObject = null;
        fakeJsonObject = null;
        fakeDtoObject = null;
    }

    @Test
    public void testFor404Error() throws Exception {
        this.mvc.perform(get("/"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void givenBlogItem_whenGetBlogById_thenReturnJsonObject() throws Exception {
        BlogModel blogModel = blogModelAssembler.toModel(fakeEntityObject);
        given(blogService.getBlogById(defaultId)).willReturn(Optional.of(blogModel));

        this.mvc.perform(get("/api/v1.0.0/blogs/" + defaultId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(4)))
                .andExpect(jsonPath("$.links", hasSize(4)))
                .andExpect(jsonPath("$.id", is(fakeEntityObject.getId().intValue())))
                .andExpect(jsonPath("$.blogTitle", is(fakeEntityObject.getBlogTitle())));

    }

    @Test
    public void givenBlogItem_whenGetBlogById_thenReturnNoContentResponse() throws Exception {
        given(blogService.getBlogById(defaultId)).willReturn(Optional.empty());

        this.mvc.perform(get("/api/v1.0.0/blogs/" + defaultId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenBlogItems_whenGetAllBlogs_thenReturnJsonArray() throws Exception {
        List<BlogEntity> allBlogs = Collections.singletonList(fakeEntityObject);
        CollectionModel<BlogModel> blogModel = blogModelAssembler.toCollectionModel(allBlogs);
        given(blogService.getAllBlogs(null, null)).willReturn(blogModel);

        this.mvc.perform(get("/api/v1.0.0/blogs")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(2)))
                .andExpect(jsonPath("$.links", hasSize(2)))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(fakeEntityObject.getId().intValue())))
                .andExpect(jsonPath("$.content[0].blogTitle", is(fakeEntityObject.getBlogTitle())));

    }

    @Test
    public void givenBlogItems_whenGetAllBlogs_thenReturnNoContentResponse() throws Exception {
        CollectionModel<BlogModel> blogModel = CollectionModel.empty();
        given(blogService.getAllBlogs(null, null)).willReturn(blogModel);

        this.mvc.perform(get("/api/v1.0.0/blogs")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }


    @Test
    public void givenJsonObject_whenCreateBlog_thenReturnJsonObject() throws Exception {
        BlogModel blogModel = blogModelAssembler.toModel(fakeEntityObject);

        given(blogService.isTitlePresent(fakeDtoObject)).willReturn(false);
        given(blogService.createBlog(fakeDtoObject)).willReturn(blogModel);

        this.mvc.perform(post("/api/v1.0.0/blogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fakeJsonObject.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.*", hasSize(4)))
                .andExpect(jsonPath("$.links", hasSize(4)))
                .andExpect(jsonPath("$.description", is(fakeEntityObject.getDescription())))
                .andExpect(jsonPath("$.blogTitle", is(fakeEntityObject.getBlogTitle())));

    }

    @Test
    public void givenJsonObject_whenCreateBlog_thenReturnInternalServerErrorResponse() throws Exception {

        given(blogService.isTitlePresent(fakeDtoObject)).willReturn(false);
        given(blogService.createBlog(fakeDtoObject)).willReturn(null);

        this.mvc.perform(post("/api/v1.0.0/blogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fakeJsonObject.toString()))
                .andExpect(status().isInternalServerError());

    }

    @Test
    public void givenJsonObject_whenCreateBlog_thenReturnConflictResponse() throws Exception {

        given(blogService.isTitlePresent(fakeDtoObject)).willReturn(true);

        this.mvc.perform(post("/api/v1.0.0/blogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fakeJsonObject.toString()))
                .andExpect(status().isConflict());

    }

    @Test
    public void givenJsonObject_whenCreateBlog_thenReturnBadRequestResponse() throws Exception {

        fakeJsonObject.put("description", null);
        fakeJsonObject.put("blogTitle", null);

        this.mvc.perform(post("/api/v1.0.0/blogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fakeJsonObject.toString()))
                .andExpect(status().isBadRequest());

    }

    @Test
    public void givenJsonObject_whenUpdateBlog_thenReturnJsonObject() throws Exception {
        BlogModel blogModel = blogModelAssembler.toModel(fakeEntityObject);

        given(blogService.updateBlog(defaultId, fakeDtoObject)).willReturn(Optional.of(blogModel));

        this.mvc.perform(patch("/api/v1.0.0/blogs/" + defaultId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fakeJsonObject.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(4)))
                .andExpect(jsonPath("$.links", hasSize(4)))
                .andExpect(jsonPath("$.id", is(fakeEntityObject.getId().intValue())))
                .andExpect(jsonPath("$.description", is(fakeEntityObject.getDescription())))
                .andExpect(jsonPath("$.blogTitle", is(fakeEntityObject.getBlogTitle())));

    }

    @Test
    public void givenJsonObject_whenUpdateBlog_thenReturnBadRequestResponse() throws Exception {
        given(blogService.updateBlog(defaultId, fakeDtoObject)).willReturn(Optional.empty());

        this.mvc.perform(patch("/api/v1.0.0/blogs/" + defaultId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fakeJsonObject.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenJsonObject_whenUpdateBlog_thenReturnNotFoundResponse() throws Exception {
        given(blogService.updateBlog(defaultId, fakeDtoObject)).willThrow(EntityNotFoundException.class);

        this.mvc.perform(patch("/api/v1.0.0/blogs/" + defaultId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fakeJsonObject.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenJsonObjectList_updateBlogList_thenReturnJsonObjectList() throws Exception {
        BlogDtoRequestFull blogDtoRequestFull = BlogDtoRequestFull
                .builder()
                .id(defaultId)
                .blogTitle(title)
                .description(description)
                .build();

        fakeJsonObject.put("id", defaultId);

        String jsonArray = "[" + fakeJsonObject.toString() + "]";

        CollectionModel<BlogModel> blogModels = blogModelAssembler.toCollectionModel(
                Collections.singletonList(fakeEntityObject));

        given(blogService.updateBlogList(Collections.singletonList(blogDtoRequestFull))).willReturn(blogModels);

        this.mvc.perform(put("/api/v1.0.0/blogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonArray))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(2)))
                .andExpect(jsonPath("$.links", hasSize(2)))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(fakeEntityObject.getId().intValue())))
                .andExpect(jsonPath("$.content[0].blogTitle", is(fakeEntityObject.getBlogTitle())));

    }

    @Test
    public void givenJsonObjectList_updateBlogList_thenReturnBadRequestResponse() throws Exception {
        String jsonArray = "[" + fakeJsonObject.toString() + "]";

        this.mvc.perform(put("/api/v1.0.0/blogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonArray))
                .andExpect(status().isBadRequest());

    }

    @Test
    public void givenJsonObjectList_updateBlogList_thenReturnBadRequestResponseFromService() throws Exception {
        BlogDtoRequestFull blogDtoRequestFull = BlogDtoRequestFull
                .builder()
                .id(defaultId)
                .blogTitle(title)
                .description(description)
                .build();

        fakeJsonObject.put("id", defaultId);

        String jsonArray = "[" + fakeJsonObject.toString() + "]";

        given(blogService.updateBlogList(
                Collections.singletonList(blogDtoRequestFull))).willThrow(RuntimeException.class);

        this.mvc.perform(put("/api/v1.0.0/blogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonArray))
                .andExpect(status().isBadRequest());
    }


    @Test
    public void givenBlogId_whenDeleteBlog_thenReturnOkResponse() throws Exception {
        given(blogService.blogHasNestedObjects(defaultId)).willReturn(false);
        given(blogService.deleteBlog(defaultId)).willReturn(true);

        this.mvc.perform(delete("/api/v1.0.0/blogs/" + defaultId))
                .andExpect(status().isOk());
    }

    @Test
    public void givenBlogId_whenDeleteBlog_thenReturnNotFoundResponse() throws Exception {
        given(blogService.blogHasNestedObjects(defaultId)).willReturn(false);
        given(blogService.deleteBlog(defaultId)).willReturn(false);

        this.mvc.perform(delete("/api/v1.0.0/blogs/" + defaultId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenBlogId_whenDeleteBlog_thenReturnBadRequestResponse() throws Exception {
        given(blogService.blogHasNestedObjects(defaultId)).willReturn(true);

        this.mvc.perform(delete("/api/v1.0.0/blogs/" + defaultId))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenEmpty_whenDeleteAllBlog_thenReturnOkResponse() throws Exception {
        given(blogService.blogHasNestedObjects()).willReturn(false);

        this.mvc.perform(delete("/api/v1.0.0/blogs"))
                .andExpect(status().isOk());
    }

    @Test
    public void givenEmpty_whenDeleteAllBlog_thenReturnBadRequestResponse() throws Exception {
        given(blogService.blogHasNestedObjects()).willReturn(true);

        this.mvc.perform(delete("/api/v1.0.0/blogs"))
                .andExpect(status().isBadRequest());
    }
}