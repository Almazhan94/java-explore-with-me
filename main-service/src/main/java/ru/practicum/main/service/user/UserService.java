package ru.practicum.main.service.user;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.service.error.ObjectNotFoundException;
import ru.practicum.main.service.user.dto.CreateUserDto;
import ru.practicum.main.service.user.dto.UserDto;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserDto createUser(CreateUserDto createUserDto) {
        User userFromDto = UserMapper.toUser(createUserDto);
        User user = userRepository.save(userFromDto);
        return UserMapper.toUserDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto> find(List<Integer> ids, Integer from, Integer size) {
        List<UserDto> userDtoList;
        Pageable pageable = pageRequest(from, size);
        if (ids == null) {
            Page<User> userPage = userRepository.findAll(pageable);
            List<User> userList = userPage.getContent();
            userDtoList = UserMapper.toUserDtoList(userList);
        } else {
            List<User> userList = userRepository.findByIdIn(ids, pageable);
            userDtoList = UserMapper.toUserDtoList(userList);
        }
        return userDtoList;
    }

    @Transactional
    public void delete(int userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ObjectNotFoundException("Пользователь с userId = " + userId + " не найден"));
        userRepository.deleteById(userId);
    }

    private Pageable pageRequest(Integer from, Integer size) {
        int page = from / size;
        return PageRequest.of(page, size);
    }
}
