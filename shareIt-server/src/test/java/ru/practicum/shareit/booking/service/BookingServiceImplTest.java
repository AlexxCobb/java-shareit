package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.DAO.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingMapperImpl;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.dto.UserMapperImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.interfaces.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {


    private BookingServiceImpl bookingService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserService userService;

    private final BookingMapper bookingMapper = new BookingMapperImpl();

    private final UserMapper userMapper = new UserMapperImpl();

    Item item;
    User booker;
    User owner;
    BookingRequestDto bookingRequestDto;
    Booking booking;

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImpl(bookingRepository, userService, bookingMapper, userMapper);
        booker = new User(1L, "booker", "booker@ya.ru");
        owner = new User(2L, "owner", "owner@ya.ru");
        item = new Item(1L, "Spoon", "description", true, owner, null);
        LocalDateTime start = LocalDateTime.of(2022, 7, 10, 10, 10);
        LocalDateTime end = LocalDateTime.of(2022, 7, 11, 10, 10);
        bookingRequestDto = new BookingRequestDto(1L, start, end);
        booking = new Booking(1L, start, end, Status.WAITING, booker, item);
    }

    @Test
    void createBookingByUser_thenCreateBooking() { // без условия
        when(userService.getUserById(eq(booker.getId()))).thenReturn(new UserDto());
        when(bookingRepository.save(any())).thenReturn(booking);
        var result = bookingService.createBookingByUser(bookingRequestDto, booker.getId(), item);

        assertNotNull(result);
        assertEquals(result.getItem().getId(), bookingRequestDto.getItemId());

        verify(userService, times(1)).getUserById(booker.getId());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void createBookingByUser_whenStartDateAndEndDateNotValid_thenThrowBadRequestException() {
        bookingRequestDto.setStart(LocalDateTime.now());
        bookingRequestDto.setEnd(LocalDateTime.now().minusHours(1));

        Throwable e = assertThrows(BadRequestException.class, () ->
                bookingService.createBookingByUser(bookingRequestDto, booker.getId(), item));
        assertEquals("Указано неправильное время начала и конца бронирования", e.getMessage());

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBookingByUser_whenBookerIsOwner_thenThrowNotFoundException() {
        Throwable e = assertThrows(NotFoundException.class, () ->
                bookingService.createBookingByUser(bookingRequestDto, owner.getId(), item));
        assertEquals("Владелец не может бронировать собственную вещь, userId = "
                + owner.getId() + " itemId = " + item.getId(), e.getMessage());

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void managingBookingStatus_whenBookingNotFound_thenThrowNotFoundException() {
        when(bookingRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.managingBookingStatus(1L, 1L, true));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void managingBookingStatus_whenUserNotOwner_thenThrowBadRequestException() {
        when(bookingRepository.findById(any())).thenReturn(Optional.of(booking));

        Throwable e = assertThrows(BadRequestException.class, () ->
                bookingService.managingBookingStatus(booking.getId(), booker.getId(), true));
        assertEquals("Указанный пользователь c userId = " + booker.getId() +
                " не является владельцем вещи c itemId = " + booking.getItem().getId(), e.getMessage());

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void managingBookingStatus_whenStatusNotWaiting_thenThrowBadRequestException() {
        when(bookingRepository.findById(any())).thenReturn(Optional.of(booking));
        booking.setStatus(Status.APPROVED);

        Throwable e = assertThrows(BadRequestException.class, () ->
                bookingService.managingBookingStatus(booking.getId(), owner.getId(), true));
        assertEquals("Бронирование уже было переведено из статуса WAITING", e.getMessage());

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void managingBookingStatus_whenApprovedTrue_thenStatusApproved() {
        when(bookingRepository.findById(any())).thenReturn(Optional.of(booking));

        var savedBooking = bookingService.managingBookingStatus(booking.getId(), owner.getId(), true);
        assertEquals(Status.APPROVED, savedBooking.getStatus());

        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void managingBookingStatus_whenApprovedFalse_thenStatusRejected() {
        when(bookingRepository.findById(any())).thenReturn(Optional.of(booking));

        var savedBooking = bookingService.managingBookingStatus(booking.getId(), owner.getId(), false);
        assertEquals(Status.REJECTED, savedBooking.getStatus());

        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void getBookingById_whenBookingNotFound_thenThrowNotFoundException() {
        when(userService.getUserById(any())).thenReturn(new UserDto());
        when(bookingRepository.findById(any())).thenReturn(Optional.empty());

        Throwable e = assertThrows(NotFoundException.class, () ->
                bookingService.getBookingById(booker.getId(), booking.getId()));
        assertEquals("Бронирование с таким id: " + booking.getId() + ", отсутствует.", e.getMessage());
    }

    @Test
    void getBookingById_whenUserNotOwnerOrNorBooker_thenThrowNotFoundException() {
        when(userService.getUserById(any())).thenReturn(new UserDto());
        when(bookingRepository.findById(any())).thenReturn(Optional.of(booking));
        var user = new User();
        user.setId(5L);

        Throwable e = assertThrows(NotFoundException.class, () ->
                bookingService.getBookingById(user.getId(), booking.getId()));
        assertEquals("Указанный пользователь c userId = " + user.getId() +
                " не является автором бронирования или владельцем вещи c itemId = " + booking.getItem().getId(), e.getMessage());
    }

    @Test
    void getBookingById_whenUserIdValid_thenReturnBookingDto() {
        when(userService.getUserById(any())).thenReturn(new UserDto());
        when(bookingRepository.findById(any())).thenReturn(Optional.of(booking));

        var actualBookingDto = bookingService.getBookingById(owner.getId(), booking.getId());
        assertNotNull(actualBookingDto);
        assertEquals(booking.getId(), actualBookingDto.getId());
        assertEquals(booking.getItem().getId(), actualBookingDto.getItem().getId());
    }

    @Test
    void getAllBookingsOfUser_whenStateNotValid_thenThrowBadRequestException() {
        String state = "NON_FICTION";
        Throwable e = assertThrows(BadRequestException.class, () ->
                bookingService.getAllBookingsOfUser(booker.getId(), state, 0, 10));
        assertEquals(String.format("Unknown state: %s", state), e.getMessage());
    }

    @Test
    void getAllBookingsOfUser_whenStateALL_thenReturnAllBookings() {
        when(userService.getUserById(any())).thenReturn(new UserDto());
        when(bookingRepository.findAllBookingsByBookerIdOrderByStartDesc(any(), any(Pageable.class))).thenReturn(List.of(booking));

        var allBookings = bookingService.getAllBookingsOfUser(owner.getId(), "ALL", 0, 10);

        assertEquals(booking.getId(), allBookings.get(0).getId());
        verify(bookingRepository).findAllBookingsByBookerIdOrderByStartDesc(any(), any(Pageable.class));
    }

    @Test
    void getAllBookingsOfUser_whenStateCURRENT_thenReturnCurrentBookings() {
        when(userService.getUserById(any())).thenReturn(new UserDto());
        when(bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(any(), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class))).thenReturn(List.of(booking));
        var currentBookings = bookingService.getAllBookingsOfUser(owner.getId(), "CURRENT", 0, 10);

        assertEquals(booking.getId(), currentBookings.get(0).getId());
        verify(bookingRepository).findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(any(), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void getAllBookingsOfUser_whenStatePAST_thenReturnPastBookings() {
        when(userService.getUserById(any())).thenReturn(new UserDto());
        when(bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(any(), any(LocalDateTime.class), any(Pageable.class))).thenReturn(List.of(booking));
        var pastBookings = bookingService.getAllBookingsOfUser(owner.getId(), "PAST", 0, 10);

        assertEquals(booking.getId(), pastBookings.get(0).getId());
        verify(bookingRepository).findByBookerIdAndEndBeforeOrderByStartDesc(any(), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void getAllBookingsOfUser_whenStateFUTURE_thenReturnFutureBookings() {


        when(userService.getUserById(any())).thenReturn(new UserDto());
        when(bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(any(), any(LocalDateTime.class), any(Pageable.class))).thenReturn(List.of(booking));
        var futureBookings = bookingService.getAllBookingsOfUser(owner.getId(), "FUTURE", 0, 10);

        assertEquals(booking.getId(), futureBookings.get(0).getId());
        verify(bookingRepository).findByBookerIdAndStartAfterOrderByStartDesc(any(), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void getAllBookingsOfUser_whenStateWAITING_thenReturnWaitingBookings() {
        when(userService.getUserById(any())).thenReturn(new UserDto());
        when(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(any(), any(Status.class), any(Pageable.class))).thenReturn(List.of(booking));

        var waitingBookings = bookingService.getAllBookingsOfUser(owner.getId(), "WAITING", 0, 10);

        assertEquals(booking.getId(), waitingBookings.get(0).getId());
        verify(bookingRepository).findByBookerIdAndStatusOrderByStartDesc(any(), any(Status.class), any(Pageable.class));
    }

    @Test
    void getAllBookingsOfUser_whenStateREJECTED_thenReturnRejectedBookings() {
        when(userService.getUserById(any())).thenReturn(new UserDto());
        when(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(any(), any(Status.class), any(Pageable.class))).thenReturn(List.of(booking));

        var rejectedBookings = bookingService.getAllBookingsOfUser(owner.getId(), "REJECTED", 0, 10);

        assertEquals(booking.getId(), rejectedBookings.get(0).getId());
        verify(bookingRepository).findByBookerIdAndStatusOrderByStartDesc(any(), any(Status.class), any(Pageable.class));
    }

    @Test
    void getAllBookingsOfAllUserItems_whenStateNotValid_thenThrowBadRequestException() {
        String state = "NON_FICTION";
        Throwable e = assertThrows(BadRequestException.class, () ->
                bookingService.getAllBookingsOfAllUserItems(booker.getId(), state, 0, 10)); // почему здесь не могу использовать матчеры
        assertEquals(String.format("Unknown state: %s", state), e.getMessage());
    }


    @Test
    void getAllBookingsOfAllUserItems_whenStateALL_thenReturnAllBookings() {
        when(bookingRepository.findAllBookingsByItemOwnerIdOrderByStartDesc(any(), any(Pageable.class))).thenReturn(List.of(booking));
        var allBookings = bookingService.getAllBookingsOfAllUserItems(owner.getId(), "ALL", 0, 10);

        assertEquals(booking.getId(), allBookings.get(0).getId());
        verify(bookingRepository).findAllBookingsByItemOwnerIdOrderByStartDesc(any(), any(Pageable.class));
    }

    @Test
    void getAllBookingsOfAllUserItems_whenStateCURRENT_thenReturnCurrentBookings() {
        when(bookingRepository.findAllBookingsByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(any(), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class))).thenReturn(List.of(booking));
        var currentBookings = bookingService.getAllBookingsOfAllUserItems(owner.getId(), "CURRENT", 0, 10);

        assertEquals(booking.getId(), currentBookings.get(0).getId());
        verify(bookingRepository).findAllBookingsByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(any(), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void getAllBookingsOfAllUserItems_whenStatePAST_thenReturnPastBookings() {
        when(bookingRepository.findAllBookingsByItemOwnerIdAndEndBeforeOrderByStartDesc(any(), any(LocalDateTime.class), any(Pageable.class))).thenReturn(List.of(booking));
        var pastBookings = bookingService.getAllBookingsOfAllUserItems(owner.getId(), "PAST", 0, 10);

        assertEquals(booking.getId(), pastBookings.get(0).getId());
        verify(bookingRepository).findAllBookingsByItemOwnerIdAndEndBeforeOrderByStartDesc(any(), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void getAllBookingsOfAllUserItems_whenStateFUTURE_thenReturnFutureBookings() {
        when(bookingRepository.findAllBookingsByItemOwnerIdAndStartAfterOrderByStartDesc(any(), any(LocalDateTime.class), any(Pageable.class))).thenReturn(List.of(booking));
        var futureBookings = bookingService.getAllBookingsOfAllUserItems(owner.getId(), "FUTURE", 0, 10);

        assertEquals(booking.getId(), futureBookings.get(0).getId());
        verify(bookingRepository).findAllBookingsByItemOwnerIdAndStartAfterOrderByStartDesc(any(), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void getAllBookingsOfAllUserItems_whenStateWAITING_thenReturnWaitingBookings() {
        when(bookingRepository.findAllBookingsByItemOwnerIdAndStatusOrderByStartDesc(any(), any(Status.class), any(Pageable.class))).thenReturn(List.of(booking));
        var waitingBookings = bookingService.getAllBookingsOfAllUserItems(owner.getId(), "WAITING", 0, 10);

        assertEquals(booking.getId(), waitingBookings.get(0).getId());
        verify(bookingRepository).findAllBookingsByItemOwnerIdAndStatusOrderByStartDesc(any(), any(Status.class), any(Pageable.class));
    }

    @Test
    void getAllBookingsOfAllUserItems_whenStateREJECTED_thenReturnRejectedBookings() {
        when(bookingRepository.findAllBookingsByItemOwnerIdAndStatusOrderByStartDesc(any(), any(Status.class), any(Pageable.class))).thenReturn(List.of(booking));
        var rejectedBookings = bookingService.getAllBookingsOfAllUserItems(owner.getId(), "REJECTED", 0, 10);

        assertEquals(booking.getId(), rejectedBookings.get(0).getId());
        verify(bookingRepository).findAllBookingsByItemOwnerIdAndStatusOrderByStartDesc(any(), any(Status.class), any(Pageable.class));
    }

    @Test
    void findLastBookingByItemId() {
        when(bookingRepository.findLastBookingByItemId(any(), any(LocalDateTime.class), any(Status.class), any(Pageable.class))).thenReturn(List.of(booking));
        var shortDto = bookingMapper.toListShortBooking(List.of(booking));
        var result = bookingService.findLastBookingByItemId(item.getId());

        assertTrue(result.isPresent());
        assertEquals(shortDto.get(0).getId(), result.get().getId());

        verify(bookingRepository).findLastBookingByItemId(any(), any(LocalDateTime.class), any(Status.class), any(Pageable.class));
    }

    @Test
    void findFutureBookingByItemId() {
        when(bookingRepository.findFutureBookingByItemId(any(), any(LocalDateTime.class), any(Status.class), any(Pageable.class))).thenReturn(List.of(booking));
        var shortDto = bookingMapper.toListShortBooking(List.of(booking));
        var result = bookingService.findFutureBookingByItemId(item.getId());

        assertTrue(result.isPresent());
        assertEquals(shortDto.get(0).getId(), result.get().getId());

        verify(bookingRepository).findFutureBookingByItemId(any(), any(LocalDateTime.class), any(Status.class), any(Pageable.class));

    }

    @Test
    void findAllBookingsByItemIds() {
        var itemIds = List.of(1L, 2L, 3L);
        when(bookingRepository.findAllBookingsByItemIdInAndStatusNotOrderByStartDesc(any(List.class), any(Status.class))).thenReturn(List.of(booking));
        var bookingMap = Stream.of(booking).collect(Collectors.groupingBy(Booking::getItem));
        var result = bookingService.findAllBookingsByItemIds(itemIds);

        assertFalse(result.isEmpty());
        assertEquals(bookingMap.get(item), result.get(item));
    }
}