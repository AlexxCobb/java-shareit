package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemMapperImpl;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.DAO.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestMapperImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.dto.UserMapperImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.interfaces.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    private ItemRequestServiceImpl itemRequestService;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private ItemServiceImpl itemService;

    @Mock
    private UserService userService;

    private final ItemRequestMapper itemRequestMapper = new ItemRequestMapperImpl();
    private final UserMapper userMapper = new UserMapperImpl();
    private final ItemMapper itemMapper = new ItemMapperImpl();

    @BeforeEach
    void setUp() {
        itemRequestService = new ItemRequestServiceImpl(itemRequestRepository, itemRequestMapper, userMapper, itemMapper, userService, itemService);
    }

    @Test
    void createRequest_whenUserNotValid_thenThrowNotFoundException() {
        var user = createUser(1L);
        var request = createItemRequest(user);
        var requestDto = itemRequestMapper.toItemRequestDto(request);

        doThrow(NotFoundException.class).when(userService).getUserById(eq(user.getId()));

        assertThrows(NotFoundException.class, () -> itemRequestService.createRequest(requestDto, user.getId()));
        verify(itemRequestRepository, never()).save(any(ItemRequest.class));
    }

    @Test
    void createRequest_whenDataValid_thenCreateItemRequestCorrect() {
        var user = createUser(1L);
        var request = createItemRequest(user);
        var requestDto = itemRequestMapper.toItemRequestDto(request);

        when(userService.getUserById(eq(user.getId()))).thenReturn(new UserDto());
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(request);
        var result = itemRequestService.createRequest(requestDto, user.getId());

        assertEquals(requestDto.getId(), result.getId());
        verify(itemRequestRepository).save(any(ItemRequest.class));
    }

    @Test
    void getAllRequestsByUser_whenUserNotValid_thenThrowNotFoundException() {
        var user = createUser(1L);

        doThrow(NotFoundException.class).when(userService).getUserById(eq(user.getId()));

        assertThrows(NotFoundException.class, () -> itemRequestService.getAllRequestsByUser(user.getId()));
        verify(itemRequestRepository, never()).findAllRequestsByRequesterIdOrderByCreatedDesc(eq(user.getId()));
    }

    @Test
    void getAllRequestsByUser_whenDataValid_thenGetAllItemRequestCorrect() {
        var user = createUser(1L);
        var request = createItemRequest(user);
        var requests = itemRequestMapper.toItemRequestDtoList(Collections.singletonList(request));

        when(userService.getUserById(eq(user.getId()))).thenReturn(new UserDto());
        when(itemRequestRepository.findAllRequestsByRequesterIdOrderByCreatedDesc(eq(user.getId()))).thenReturn(Collections.singletonList(request));
        var result = itemRequestService.getAllRequestsByUser(user.getId());

        assertEquals(requests.get(0).getId(), result.get(0).getId());
        verify(itemRequestRepository).findAllRequestsByRequesterIdOrderByCreatedDesc(eq(user.getId()));
    }

    @Test
    void getAllRequests_whenUserNotValid_thenThrowNotFoundException() {
        var user = createUser(1L);

        doThrow(NotFoundException.class).when(userService).getUserById(eq(user.getId()));

        assertThrows(NotFoundException.class, () -> itemRequestService.getAllRequests(user.getId(), 0, 10));
        verify(itemRequestRepository, never()).findByRequesterIdNotOrderByCreatedDesc(eq(user.getId()), any(Pageable.class));
    }

    @Test
    void getAllRequests_whenDataValid_thenGetAllItemRequestCorrect() {
        var user = createUser(1L);
        var request = createItemRequest(user);

        when(userService.getUserById(eq(user.getId()))).thenReturn(new UserDto());
        when(itemRequestRepository.findByRequesterIdNotOrderByCreatedDesc(eq(user.getId()), any(Pageable.class))).thenReturn(List.of(request));
        var result = itemRequestService.getAllRequests(user.getId(), 0, 10);

        assertEquals(request.getId(), result.get(0).getId());
        verify(itemRequestRepository).findByRequesterIdNotOrderByCreatedDesc(eq(user.getId()), any(Pageable.class));
    }

    @Test
    void getRequestById_whenItemRequestNotFound_themThrowNotFoundException() {
        var user = createUser(1L);
        var itemRequest = createItemRequest(user);

        when(userService.getUserById(eq(user.getId()))).thenReturn(new UserDto());
        when(itemRequestRepository.findById(eq(itemRequest.getId()))).thenReturn(Optional.empty());

        Throwable e = assertThrows(NotFoundException.class, () ->
                itemRequestService.getRequestById(user.getId(), itemRequest.getId()));
        assertEquals("Запрос с id = " + itemRequest.getId() + " отсутствует.", e.getMessage());
    }

    @Test
    void getRequestById_whenDataValid_themReturnItemRequestCorrect() {
        var user = createUser(1L);
        var itemRequest = createItemRequest(user);

        when(userService.getUserById(eq(user.getId()))).thenReturn(new UserDto());
        when(itemRequestRepository.findById(eq(itemRequest.getId()))).thenReturn(Optional.of(itemRequest));
        var result = itemRequestService.getRequestById(user.getId(), itemRequest.getId());

        assertEquals(itemRequest.getId(), result.getId());
        verify(itemRequestRepository).findById(eq(itemRequest.getId()));
    }

    private User createUser(Long id) {
        return new User(id, "user", "user@ya.ru");
    }

    private ItemRequest createItemRequest(User user) {
        return new ItemRequest(1L, "description", user, LocalDateTime.now(), null);
    }
}