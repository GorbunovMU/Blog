package com.halliburton.blog.utils;

import com.halliburton.blog.dto.PostDtoRequest;
import com.halliburton.blog.dto.PostDtoRequestFull;

public class PostValidator {
    public static boolean validatePostEntityDto(PostDtoRequest model) {

        return model.getPostTitle() != null && model.getPostBody() != null
                && model.getPostConclusion() != null && model.getAuthor() != null
                && model.getPublishedOn() != null;
    }

    public static boolean validateFullPostEntityDto(PostDtoRequestFull dto) {
        return validatePostEntityDto(dto) && dto.getId() != null && dto.getBlogId() != null;
    }

    public static String getNotValidFieldsForPostEntityDto(PostDtoRequest model) {
        StringBuilder builder = new StringBuilder();
        builder.append("Not valid fields: ");

        if (model.getPostTitle() == null) {
            builder.append("field PostTitle is null; ");
        }
        if (model.getPostBody() == null) {
            builder.append("field PostBody is null; ");
        }
        if (model.getPostConclusion() == null) {
            builder.append("field PostConclusion is null; ");
        }
        if (model.getAuthor() == null) {
            builder.append("field Author is null; ");
        }
        if (model.getPublishedOn() == null) {
            builder.append("field PublishedOn is null; ");
        }

        return builder.toString();
    }

    public static String getNotValidFieldsForFullPostEntityDto(PostDtoRequestFull dto) {
        StringBuilder result = new StringBuilder();
        result.append(getNotValidFieldsForPostEntityDto(dto));
        if (dto.getId() == null) {
            result.append(" field Id is null; ");
        }
        if (dto.getBlogId() == null) {
            result.append(" BlogId Id is null; ");
        }
        result.append(" for entity with id = ").append(dto.getId());

        return result.toString();
    }

}
