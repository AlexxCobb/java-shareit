package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.DAO.UserRepository;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.dto.UserMapperImpl;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    private final UserMapper userMapper = new UserMapperImpl();

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, userMapper);
    }

    @Test
    void addUser_whenUserDtoValid_thenSaveUserCorrect() {
        var user = createUser(1L);
        var userDto = userMapper.toUserDto(user);

        when(userRepository.save(any(User.class))).thenReturn(user);
        var resultUser = userService.addUser(userDto);

        assertEquals(userDto.getId(), resultUser.getId());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_whenUserNotFound_thenThrowNotFoundException() {
        var user = createUser(1L);
        var userDto = userMapper.toUserDto(user);

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.empty());

        Throwable e = assertThrows(NotFoundException.class, () ->
                userService.updateUser(user.getId(), userDto));
        assertEquals("Пользователь с таким id: " + user.getId() + ", отсутствует.", e.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_whenDataValid_thenUpdateUserCorrect() {
        var user = createUser(1L);
        var userDto = userMapper.toUserDto(user);
        userDto.setName("updateUser");

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        var updatedUser = userService.updateUser(user.getId(), userDto);

        assertEquals(userDto.getName(), updatedUser.getName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deleteUserById_whenUserNotFound_thenThrowNotFoundException() {
        var user = createUser(1L);

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.deleteUserById(user.getId()));
        verify(userRepository, never()).deleteById(eq(user.getId()));
    }

    @Test
    void deleteUserById_whenUserFound_thenDeleteUserCorrect() {
        var user = createUser(1L);

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));
        userService.deleteUserById(user.getId());

        verify(userRepository, times(1)).deleteById(eq(user.getId()));
    }

    @Test
    void getUserById_whenUserNotFound_thenThrowNotFoundException() {
        var user = createUser(1L);

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.empty());

        Throwable e = assertThrows(NotFoundException.class, () ->
                userService.getUserById(user.getId()));
        assertEquals("Пользователь с таким id: " + user.getId() + ", отсутствует.", e.getMessage());
    }

    @Test
    void getUserById_whenUserFound_thenReturnUserDtoCorrect() {
        var user = createUser(1L);

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));
        var resultUser = userService.getUserById(user.getId());

        assertEquals(user.getId(), resultUser.getId());
        verify(userRepository).findById(eq(user.getId()));
    }

    @Test
    void getAllUsers_whenUsersExist_thenReturnListUsersCorrect() {
        var user = createUser(1L);
        var user2 = createUser(2L);

        when(userRepository.findAll()).thenReturn(List.of(user, user2));
        var users = userService.getAllUsers();

        assertEquals(2, users.size());
        assertEquals(user.getId(), users.get(0).getId());
        assertEquals(user2.getId(), users.get(1).getId());
        verify(userRepository).findAll();
    }

    private User createUser(Long id) {
        return new User(id, "user", "user@ya.ru");
    }
}