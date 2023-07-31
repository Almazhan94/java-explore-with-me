package ru.practicum.main.service.comment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping
@Validated
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/users/{userId}/events/{eventId}/comment")
    public CommentDto addComment(@PathVariable int userId,@PathVariable int eventId, @RequestBody AddCommentDto addCommentDto) {
        log.info("Добавляется комментарий: {}", addCommentDto);
        return commentService.addComment(userId, eventId, addCommentDto);
    }

    @PatchMapping("/users/{userId}/comment/{commentId}")
    public CommentDto patchRequestByUser(@PathVariable int userId, @PathVariable int commentId,
                                                           @RequestBody UpdateCommentDto updateCommentDto) {

        log.info("Пользователь с userId = {} обновляет комментарий с commentId = {} " +
            "для события requestStatusUpdateDto = {}", userId, commentId, updateCommentDto);

        return commentService.patchByUser(userId, commentId, updateCommentDto);
    }

    @GetMapping("/users/{userId}/comment")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> getRequestList(@PathVariable int userId) {
        log.info("Пользователь с userId = {} ищет комментарии", userId);
        return commentService.getComment(userId);
    }
}
