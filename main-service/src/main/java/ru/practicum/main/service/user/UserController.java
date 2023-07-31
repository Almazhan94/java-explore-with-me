package ru.practicum.main.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.service.user.dto.CreateUserDto;
import ru.practicum.main.service.user.dto.UserDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/admin/users")
@Validated
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody @Valid CreateUserDto createUserDto) {
        log.info("Добавляется пользователь: {}", createUserDto);
        return userService.createUser(createUserDto);
    }

    @GetMapping
    public List<UserDto> find(@RequestParam(value = "ids", required = false) List<Integer> ids,
                              @PositiveOrZero @RequestParam(value = "from", required = false, defaultValue = "0") Integer from,
                              @Positive @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        List<UserDto> allUsers = userService.find(ids, from, size);
        log.info("Количество пользователей в текущий момент: {}", allUsers.size());
        return allUsers;
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int userId) {
        log.info("Удаляется пользователь с идентификатором: {}", userId);
        userService.delete(userId);
    }


}
