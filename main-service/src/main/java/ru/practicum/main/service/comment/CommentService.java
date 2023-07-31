package ru.practicum.main.service.comment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.main.service.error.ObjectNotFoundException;
import ru.practicum.main.service.event.Event;
import ru.practicum.main.service.event.EventRepository;
import ru.practicum.main.service.request.Request;
import ru.practicum.main.service.request.RequestRepository;
import ru.practicum.main.service.request.RequestStatus;
import ru.practicum.main.service.user.User;
import ru.practicum.main.service.user.UserRepository;

import java.util.List;

@Service
public class CommentService {

    private final UserRepository userRepository;

    private final EventRepository eventRepository;

    private final CommentRepository commentRepository;

    private final RequestRepository requestRepository;

    @Autowired
    public CommentService(UserRepository userRepository, EventRepository eventRepository, CommentRepository commentRepository,
                          RequestRepository requestRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.commentRepository = commentRepository;
        this.requestRepository = requestRepository;
    }

    public CommentDto addComment(int userId, int eventId, AddCommentDto addCommentDto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ObjectNotFoundException("Пользователь с userId = " + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ObjectNotFoundException("Событие с eventId = " + eventId + " не найдено"));

        Request request = requestRepository.findByRequesterIdAndEventIdAndStatus(userId, eventId, RequestStatus.CONFIRMED);
        if (request == null) {
            throw new ObjectNotFoundException("Пользователь с userId = " + userId +
                " не найден в списке подтвержденных заявок на участии в событии с eventId = " + eventId);
        }
        Comment comment = new Comment();
        comment.setText(addCommentDto.getText());
        comment.setAuthor(user);
        comment.setEvent(event);
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    public CommentDto patchByUser(int userId, int commentId, int eventId, UpdateCommentDto updateCommentDto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ObjectNotFoundException("Пользователь с userId = " + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ObjectNotFoundException("Событие с eventId = " + eventId + " не найдено"));

        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new ObjectNotFoundException("Комментарий с commentId = " + commentId + " не найден"));

        if (updateCommentDto.getText() != null) {
            comment.setText(updateCommentDto.getText());
        }
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    public List<CommentDto> getComment(int userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ObjectNotFoundException("Пользователь с userId = " + userId + " не найден"));

        List<Comment> commentList = commentRepository.findByAuthorId(userId);
        return CommentMapper.toCommentDtoList(commentList);
    }
}