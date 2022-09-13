package com.halliburton.blog.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonRootName(value = "post")
public class PostModel extends RepresentationModel<PostModel> {
    private Long id;
    private String postTitle;
    private String postBody;
    private String postConclusion;
    private String author;
    private LocalDate publishedOn;
}
