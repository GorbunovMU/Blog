package com.halliburton.blog.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@JsonRootName(value = "blogDto")
public class BlogDtoRequest implements Serializable {
    private String blogTitle;
    private String description;
}
