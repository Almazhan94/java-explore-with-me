package ru.practicum.main.service.user;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
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


    public UserDto createUser(CreateUserDto createUserDto) {
        User userFromDto = UserMapper.toUser(createUserDto);
        User user = userRepository.save(userFromDto);
        return UserMapper.toUserDto(user);
    }

    public List<UserDto> findAllUser(Integer from, Integer size) {
        return null;
    }

    public List<UserDto> findUserById(int userId, Integer from, Integer size) {
        return null;
    }

    public List<UserDto> find(List<Integer> ids, Integer from, Integer size) {
        List<UserDto> userDtoList;
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);
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

    public void delete(int userId) {

    }

    private Pageable pageRequest(Integer from, Integer size, Sort sort) {
        int page = from / size;
        return PageRequest.of(page, size, sort);
    }
}
