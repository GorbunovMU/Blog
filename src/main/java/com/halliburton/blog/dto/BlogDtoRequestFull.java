package com.halliburton.blog.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonRootName(value = "blogDtoFull")
public class BlogDtoRequestFull extends BlogDtoRequest {
    private Long id;

    public BlogDtoRequestFull(Long id, String blogTitle, String description) {
        super(blogTitle, description);
        this.id = id;
    }
}
