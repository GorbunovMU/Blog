package com.halliburton.blog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class IntegrationTest {
    @Autowired
    private MockMvc mvc;

    @Test
    public void testAllBlogs() throws Exception {
        this.mvc.perform(get("/api/v1.0.0/blogs")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(2)))
                .andExpect(jsonPath("$.links", hasSize(2)))
                .andExpect(jsonPath("$.content", hasSize(3)));
    }

    @Test
    public void testAllPosts() throws Exception {
        this.mvc.perform(get("/api/v1.0.0/posts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(2)))
                .andExpect(jsonPath("$.links", hasSize(2)))
                .andExpect(jsonPath("$.content", hasSize(3)));
    }

    @Test
    public void givenPostItems_whenGetAllPosts_thenReturnJsonArray() throws Exception {
        String keyWord = "alingly";

        this.mvc.perform(get("/api/v1.0.0/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("keyword", keyWord))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(2)))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.links", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(2)))
                .andExpect(jsonPath("$.content[1].id", is(3)));

    }

}
