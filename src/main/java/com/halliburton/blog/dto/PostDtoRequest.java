package com.halliburton.blog.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@JsonRootName(value = "postDto")
public class PostDtoRequest implements Serializable {
    private String postTitle;
    private String postBody;
    private String postConclusion;
    private String author;
    private LocalDate publishedOn;
}
