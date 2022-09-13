package com.halliburton.blog.utils;

import com.halliburton.blog.dto.BlogDtoRequest;
import com.halliburton.blog.dto.BlogDtoRequestFull;

public class BlogValidator {
    public static boolean validateBlogEntityDto(BlogDtoRequest dto) {
        return dto.getBlogTitle() != null && dto.getDescription() != null;
    }

    public static boolean validateFullBlogEntityDto(BlogDtoRequestFull dto) {
        return validateBlogEntityDto(dto) && dto.getId() != null;
    }

    public static String getNotValidFieldsForBlogEntityDto(BlogDtoRequest dto) {
        StringBuilder builder = new StringBuilder();
        builder.append("Not valid fields: ");

        if (dto.getBlogTitle() == null) {
            builder.append("field BlogTitle is null; ");
        }
        if (dto.getDescription() == null) {
            builder.append("field Description is null; ");
        }

        return builder.toString();
    }

    public static String getNotValidFieldsForFullBlogEntityDto(BlogDtoRequestFull dto) {
        StringBuilder result = new StringBuilder();
        result.append(getNotValidFieldsForBlogEntityDto(dto));

        if (dto.getId() == null) {
            result.append(" field Id is null ");
        }
        result.append(" for entity with id = ").append(dto.getId());

        return result.toString();
    }
}
