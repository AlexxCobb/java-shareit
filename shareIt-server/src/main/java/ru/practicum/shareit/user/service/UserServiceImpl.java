package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.DAO.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.service.interfaces.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDto addUser(UserDto userDto) {
        var user = userMapper.toUser(userDto);
        var createdUser = userRepository.save(user);
        userDto.setId(createdUser.getId());
        return userDto;
    }

    public UserDto updateUser(Long userId, UserDto userDto) {
        var user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с таким id: " + userId + ", отсутствует."));
        userMapper.updateUserFromUserDto(userDto, user);
        return userMapper.toUserDto(userRepository.save(user));
    }

    public void deleteUserById(Long userId) {
        getUserById(userId);
        userRepository.deleteById(userId);
    }

    public UserDto getUserById(Long userId) {
        var user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с таким id: " + userId + ", отсутствует."));
        return userMapper.toUserDto(user);
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }
}
