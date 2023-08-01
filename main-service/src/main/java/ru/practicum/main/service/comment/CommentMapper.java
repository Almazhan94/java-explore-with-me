package ru.practicum.main.service.comment;

import ru.practicum.main.service.comment.dto.CommentDto;

import java.util.ArrayList;
import java.util.List;

public class CommentMapper {

    public static CommentDto toCommentDto(Comment comment) {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(comment.getId());
        commentDto.setText(comment.getText());
        commentDto.setAuthorName(comment.getAuthor().getName());
        commentDto.setCreated(comment.getCreated());
        commentDto.setLastUpdatedOn(comment.getLastUpdatedOn());
        return commentDto;
    }

    public static List<CommentDto> toCommentDtoList(List<Comment> commentList) {
        List<CommentDto> commentDtoList = new ArrayList<>();
        for (Comment c : commentList) {
            commentDtoList.add(CommentMapper.toCommentDto(c));
        }
        return commentDtoList;
    }
}
