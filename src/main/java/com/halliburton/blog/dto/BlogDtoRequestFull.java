package com.halliburton.blog.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonRootName(value = "blogDtoFull")
public class BlogDtoRequestFull extends BlogDtoRequest {
    private Long id;
}
