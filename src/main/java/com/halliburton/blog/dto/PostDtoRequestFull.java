package com.halliburton.blog.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonRootName(value = "postDtoFull")
public class PostDtoRequestFull extends PostDtoRequest {
    private Long id;
    private Long blogId;

    public PostDtoRequestFull(Long id, Long blogId, String postTitle, String postBody, String postConclusion,
                              String author, LocalDate publishedOn) {
        super(postTitle, postBody, postConclusion, author, publishedOn);
        this.id = id;
        this.blogId = blogId;
    }
}
