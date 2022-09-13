package com.halliburton.blog.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonRootName(value = "blog")
public class BlogModel extends RepresentationModel<BlogModel> {
    private Long id;
    private String blogTitle;
    private String description;
}
