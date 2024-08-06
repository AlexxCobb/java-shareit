package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingMapperImpl;
import ru.practicum.shareit.booking.dto.ShortBookingItemDto;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.interfaces.BookingService;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.DAO.ItemRepository;
import ru.practicum.shareit.item.comment.DAO.CommentRepository;
import ru.practicum.shareit.item.comment.dto.CommentMapper;
import ru.practicum.shareit.item.comment.dto.CommentMapperImpl;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemMapperImpl;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.DAO.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.dto.UserMapperImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.interfaces.UserService;
import ru.practicum.shareit.utils.PaginationServiceClass;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {


    private ItemServiceImpl itemService;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserService userService;

    @Mock
    private BookingService bookingService;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    private final ItemMapper itemMapper = new ItemMapperImpl();
    private final UserMapper userMapper = new UserMapperImpl();
    private final CommentMapper commentMapper = new CommentMapperImpl();
    private final BookingMapper bookingMapper = new BookingMapperImpl();

    @BeforeEach
    void setUp() {
        itemService = new ItemServiceImpl(itemRepository, commentRepository, itemMapper, userMapper, commentMapper, bookingMapper, userService, bookingService, itemRequestRepository);
    }

    @Test
    void createItem_whenItemDtoWithoutRequestId_thenSaveItemWithoutRequest() {
        var user = createUser(1L);
        var item = createItem(user);
        var itemDto = itemMapper.toItemDto(item);

        when(userService.getUserById(eq(user.getId()))).thenReturn(new UserDto());
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        var result = itemService.createItem(itemDto, user.getId());
        itemMapper.toItem(result);

        assertEquals(itemDto.getId(), result.getId());
        assertNull(result.getRequestId());
        verify(itemRequestRepository, never()).findById(eq(user.getId()));
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void createItem_whenItemDtoWithRequestId_thenSaveItemWithRequest() {
        var user = createUser(1L);
        var item = createItem(user);
        var request = createItemRequest(user);
        item.setItemRequest(request);
        var itemDto = itemMapper.toItemDto(item);

        when(userService.getUserById(eq(user.getId()))).thenReturn(new UserDto());
        when(itemRequestRepository.findById(eq(request.getId()))).thenReturn(Optional.of(request));
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        var result = itemService.createItem(itemDto, user.getId());

        assertNotNull(result.getRequestId());
        assertEquals(itemDto.getRequestId(), result.getRequestId());
        verify(itemRequestRepository).findById(eq(user.getId()));
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void updateItem_whenItemNotFound_thenThrowNotFoundException() {
        var user = createUser(1L);
        var item = createItem(user);
        var itemDto = itemMapper.toItemDto(item);

        when(userService.getUserById(eq(user.getId()))).thenReturn(new UserDto());
        when(itemRepository.findById(eq(item.getId()))).thenReturn(Optional.empty());

        Throwable e = assertThrows(NotFoundException.class, () ->
                itemService.updateItem(item.getId(), itemDto, user.getId()));
        assertEquals("Вещь с таким id: " + item.getId() + ", отсутствует.", e.getMessage());
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void updateItem_whenUserNotOwner_thenThrowNotFoundException() {
        var owner = createUser(1L);
        var user = createUser(3L);
        var item = createItem(owner);
        var itemDto = itemMapper.toItemDto(item);

        when(userService.getUserById(eq(user.getId()))).thenReturn(new UserDto());
        when(itemRepository.findById(eq(item.getId()))).thenReturn(Optional.of(item));

        Throwable e = assertThrows(NotFoundException.class, () ->
                itemService.updateItem(item.getId(), itemDto, user.getId()));
        assertEquals("Данный пользователь " + user.getId() + " не является владельцем вещи с id: " + item.getId(), e.getMessage());
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void updateItem_whenValidParam_thenUpdateItem() {
        var owner = createUser(1L);
        var item = createItem(owner);
        var itemDto = itemMapper.toItemDto(item);
        itemDto.setAvailable(false);

        when(userService.getUserById(eq(owner.getId()))).thenReturn(new UserDto());
        when(itemRepository.findById(eq(item.getId()))).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        var resultItem = itemService.updateItem(item.getId(), itemDto, owner.getId());
        assertEquals(itemDto.getAvailable(), resultItem.getAvailable());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void getItemById_whenItemNotFound_thenThrowNotFoundException() {
        var user = createUser(1L);
        var item = createItem(user);

        when(userService.getUserById(eq(user.getId()))).thenReturn(new UserDto());
        when(itemRepository.findById(eq(item.getId()))).thenReturn(Optional.empty());

        Throwable e = assertThrows(NotFoundException.class, () ->
                itemService.getItemById(user.getId(), item.getId()));
        assertEquals("Вещь с таким id: " + item.getId() + ", отсутствует.", e.getMessage());
        verify(commentRepository, never()).findAllByItemId(eq(item.getId()));
    }

    @Test
    void getItemById_whenUserIsOwner_thenReturnItemDtoWithBookings() {
        var owner = createUser(1L);
        var item = createItem(owner);

        when(userService.getUserById(eq(owner.getId()))).thenReturn(new UserDto());
        when(itemRepository.findById(eq(item.getId()))).thenReturn(Optional.of(item));
        when(bookingService.findLastBookingByItemId(eq(item.getId()))).thenReturn(Optional.of(new ShortBookingItemDto()));
        when(bookingService.findFutureBookingByItemId(eq(item.getId()))).thenReturn(Optional.of(new ShortBookingItemDto()));
        var result = itemService.getItemById(owner.getId(), item.getId());

        assertNotNull(result.getLastBooking());
        assertNotNull(result.getNextBooking());
        verify(bookingService).findLastBookingByItemId(eq(item.getId()));
        verify(bookingService).findFutureBookingByItemId(eq(item.getId()));
        verify(commentRepository).findAllByItemId(eq(item.getId()));
    }

    @Test
    void getItemById_whenUserNotOwner_thenReturnItemDtoWithoutBookings() {
        var owner = createUser(1L);
        var user = createUser(3L);
        var item = createItem(owner);

        when(userService.getUserById(eq(user.getId()))).thenReturn(new UserDto());
        when(itemRepository.findById(eq(item.getId()))).thenReturn(Optional.of(item));
        var result = itemService.getItemById(user.getId(), item.getId());

        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        verify(bookingService, never()).findLastBookingByItemId(eq(item.getId()));
        verify(bookingService, never()).findFutureBookingByItemId(eq(item.getId()));
        verify(commentRepository).findAllByItemId(eq(item.getId()));
    }

    @Test
    void getItemById_whenItemHasComments_thenReturnItemDtoWithComments() {
        var owner = createUser(1L);
        var user = createUser(3L);
        var item = createItem(owner);
        var comment = createComment(user, item);

        when(userService.getUserById(eq(user.getId()))).thenReturn(new UserDto());
        when(itemRepository.findById(eq(item.getId()))).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemId(eq(item.getId()))).thenReturn(List.of(comment));
        var result = itemService.getItemById(user.getId(), item.getId());

        assertEquals(comment.getId(), result.getComments().get(0).getId());
        verify(bookingService, never()).findLastBookingByItemId(eq(item.getId()));
        verify(bookingService, never()).findFutureBookingByItemId(eq(item.getId()));
        verify(commentRepository).findAllByItemId(eq(item.getId()));
    }

    @Test
    void getUserItems_whenUserHasNotItems_thenReturnEmptyList() {
        var owner = createUser(1L);

        when(userService.getUserById(eq(owner.getId()))).thenReturn(new UserDto());
        when(itemRepository.findAllByOwnerIdOrderById(eq(owner.getId()), any(Pageable.class))).thenReturn(new ArrayList<>());
        var result = itemService.getUserItems(owner.getId(), 0, 10);

        assertEquals(0, result.size());
        verify(bookingService, never()).findAllBookingsByItemIds(any(List.class));
        verify(commentRepository, never()).findAllCommentsByItemIdInOrderByCreatedDesc(any(List.class));
    }

    @Test
    void getUserItems_whenUserHasItems_thenReturnListItemDto() {
        var owner = createUser(1L);
        var item = createItem(owner);

        when(userService.getUserById(eq(owner.getId()))).thenReturn(new UserDto());
        when(itemRepository.findAllByOwnerIdOrderById(eq(owner.getId()), any(Pageable.class))).thenReturn(List.of(item));
        when(bookingService.findAllBookingsByItemIds(List.of(item.getId()))).thenReturn(new HashMap<>());
        when(commentRepository.findAllCommentsByItemIdInOrderByCreatedDesc(List.of(item.getId()))).thenReturn(new ArrayList<>());
        var result = itemService.getUserItems(owner.getId(), 0, 10);

        assertEquals(1, result.size());
        assertEquals(item.getId(), result.get(0).getId());
        verify(bookingService).findAllBookingsByItemIds(any(List.class));
        verify(commentRepository).findAllCommentsByItemIdInOrderByCreatedDesc(any(List.class));
    }

    @Test
    void getUserItems_whenUserHasItemsWithBookings_thenReturnListItemDtoWithBookings() {
        var owner = createUser(1L);
        var item = createItem(owner);
        var booker = createUser(2L);
        var bookingLast = createBooking(LocalDateTime.now().minusHours(1), LocalDateTime.now(), booker, item);
        var bookingNext = createBooking(LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2), booker, item);
        bookingNext.setId(2L);

        when(userService.getUserById(eq(owner.getId()))).thenReturn(new UserDto());
        when(itemRepository.findAllByOwnerIdOrderById(eq(owner.getId()), any(Pageable.class))).thenReturn(List.of(item));
        when(bookingService.findAllBookingsByItemIds(List.of(item.getId()))).thenReturn(Map.of(item, List.of(bookingLast, bookingNext)));
        when(commentRepository.findAllCommentsByItemIdInOrderByCreatedDesc(List.of(item.getId()))).thenReturn(new ArrayList<>());

        var result = itemService.getUserItems(owner.getId(), 0, 10);
        assertEquals(1, result.size());
        assertEquals(item.getId(), result.get(0).getId());
    }

    @Test
    void getUserItems_whenUserHasItemsWithComments_thenReturnListItemDtoWithComments() {
        var owner = createUser(1L);
        var user = createUser(2L);
        var item = createItem(owner);
        var item2 = createItem(owner);
        var comment = createComment(user, item);
        var comment2 = createComment(user, item2);
        var commentList = List.of(comment, comment2);

        when(userService.getUserById(eq(owner.getId()))).thenReturn(new UserDto());
        when(itemRepository.findAllByOwnerIdOrderById(eq(owner.getId()), any(PaginationServiceClass.class))).thenReturn(List.of(item, item2));
        when(bookingService.findAllBookingsByItemIds(any(List.class))).thenReturn(new HashMap<>());
        when(commentRepository.findAllCommentsByItemIdInOrderByCreatedDesc(any(List.class))).thenReturn(commentList);
        var result = itemService.getUserItems(owner.getId(), 0, 10);

        assertEquals(comment.getItem().getId(), result.get(0).getId());
    }

    @Test
    void searchItemToRent_whenSearchTextIsNull_thenThrowBadRequestException() {
        when(userService.getUserById(anyLong())).thenReturn(new UserDto());
        Throwable e = assertThrows(BadRequestException.class, () ->
                itemService.searchItemToRent(1L, null, 1, 10));

        assertEquals("Параметр для поиска вещи пустой.", e.getMessage());
        verify(itemRepository, never()).searchItemToRent(anyString(), any(Pageable.class));
    }

    @Test
    void searchItemToRent_whenSearchTextIsBlank_thenReturnEmptyList() {
        when(userService.getUserById(anyLong())).thenReturn(new UserDto());
        var items = itemService.searchItemToRent(1L, " ", 1, 10);

        assertTrue(items.isEmpty());
        verify(itemRepository, never()).searchItemToRent(anyString(), any(Pageable.class));
    }

    @Test
    void searchItemToRent_whenTextIsValid_thenReturnCorrectItemDto() {
        var user = createUser(1L);
        var item = createItem(user);
        var itemDto = itemMapper.toItemDto(item);

        when(userService.getUserById(anyLong())).thenReturn(new UserDto());
        when(itemRepository.searchItemToRent(anyString(), any(Pageable.class))).thenReturn(List.of(item));
        var result = itemService.searchItemToRent(user.getId(), "poon", 0, 10);

        assertEquals(itemDto.getId(), result.get(0).getId());
        verify(itemRepository).searchItemToRent(anyString(), any(Pageable.class));
    }

    @Test
    void createComment_whenDataCorrect_thenSaveComment() {
        var user = createUser(1L);
        var owner = createUser(2L);
        var item = createItem(owner);
        var comment = createComment(user, item);
        var commentDto = commentMapper.toCommentDto(comment);
        var booking = createBooking(LocalDateTime.now(), LocalDateTime.now().plusHours(1), user, item);
        var bookingDto = bookingMapper.toListBookingResponseDto(List.of(booking));

        when(userService.getUserById(eq(user.getId()))).thenReturn(userMapper.toUserDto(user));
        when(itemRepository.findById(eq(item.getId()))).thenReturn(Optional.of(item));
        when(bookingService.getAllBookingsOfUser(eq(user.getId()), any(String.class), any(), any())).thenReturn(bookingDto);
        var result = itemService.createComment(commentDto, item.getId(), user.getId());

        assertEquals(commentDto.getId(), result.getId());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void createComment_whenItemNotFound_thenThrowNotFoundException() {
        var user = createUser(1L);
        var owner = createUser(2L);
        var item = createItem(owner);
        var comment = createComment(user, item);
        var commentDto = commentMapper.toCommentDto(comment);


        when(userService.getUserById(eq(user.getId()))).thenReturn(userMapper.toUserDto(user));
        when(itemRepository.findById(eq(item.getId()))).thenReturn(Optional.empty());

        Throwable e = assertThrows(NotFoundException.class, () ->
                itemService.createComment(commentDto, item.getId(), user.getId()));
        assertEquals("Вещь с таким id: " + item.getId() + ", отсутствует.", e.getMessage());
        verify(bookingService, never()).getAllBookingsOfUser(eq(user.getId()), any(String.class), any(), any());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void createComment_whenUserIsOwner_thenThrowBadRequestException() {
        var owner = createUser(2L);
        var item = createItem(owner);
        var comment = createComment(owner, item);
        var commentDto = commentMapper.toCommentDto(comment);


        when(userService.getUserById(eq(owner.getId()))).thenReturn(new UserDto());
        when(itemRepository.findById(eq(item.getId()))).thenReturn(Optional.of(item));

        Throwable e = assertThrows(BadRequestException.class, () ->
                itemService.createComment(commentDto, item.getId(), owner.getId()));
        assertEquals("Данный пользователь " + owner.getId() + " является владельцем вещи с id: " + item.getId(), e.getMessage());
        verify(bookingService, never()).getAllBookingsOfUser(eq(owner.getId()), any(String.class), any(), any());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void createComment_whenUserHasNotBookItem_thenThrowBadRequestException() {
        var user = createUser(1L);
        var owner = createUser(2L);
        var item = createItem(owner);
        var comment = createComment(user, item);
        var commentDto = commentMapper.toCommentDto(comment);

        when(userService.getUserById(eq(user.getId()))).thenReturn(new UserDto());
        when(itemRepository.findById(eq(item.getId()))).thenReturn(Optional.of(item));
        when(bookingService.getAllBookingsOfUser(eq(user.getId()), any(String.class), any(), any())).thenReturn(new ArrayList<>());

        Throwable e = assertThrows(BadRequestException.class, () ->
                itemService.createComment(commentDto, item.getId(), user.getId()));
        assertEquals("Пользователь c userId - " + user.getId() + " не брал вещь в аренду c itemId -  " + item.getId(), e.getMessage());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void createComment_whenUserHasNotValidBookingOfItem_thenThrowBadRequestException() {
        var user = createUser(1L);
        var owner = createUser(2L);
        var item = createItem(owner);
        var comment = createComment(user, item);
        var commentDto = commentMapper.toCommentDto(comment);
        var booking = createBooking(LocalDateTime.now(), LocalDateTime.now().plusHours(1), user, item);
        booking.setStatus(Status.REJECTED);
        var bookingDto = bookingMapper.toListBookingResponseDto(List.of(booking));

        when(userService.getUserById(eq(user.getId()))).thenReturn(new UserDto());
        when(itemRepository.findById(eq(item.getId()))).thenReturn(Optional.of(item));
        when(bookingService.getAllBookingsOfUser(eq(user.getId()), any(String.class), any(), any())).thenReturn(bookingDto);

        Throwable e = assertThrows(BadRequestException.class, () ->
                itemService.createComment(commentDto, item.getId(), user.getId()));
        assertEquals("Пользователь c userId - " + user.getId() + " не брал вещь в аренду c itemId - " + item.getId(), e.getMessage());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void isUserHaveItems() {
        when(itemRepository.existsByOwnerId(anyLong())).thenReturn(true);

        var result = itemService.isUserHaveItems(1L);
        assertTrue(result);
    }

    private User createUser(Long id) {
        return new User(id, "user", "user@ya.ru");
    }

    private Item createItem(User user) {
        return new Item(1L, "Spoon", "description", true, user, null);
    }

    private ItemRequest createItemRequest(User user) {
        return new ItemRequest(1L, "description", user, LocalDateTime.now(), null);
    }

    private Comment createComment(User user, Item item) {
        return new Comment(1L, "comment", item, user, LocalDateTime.now());
    }

    private Booking createBooking(LocalDateTime start, LocalDateTime end, User booker, Item item) {
        return new Booking(1L, start, end, Status.APPROVED, booker, item);
    }
}