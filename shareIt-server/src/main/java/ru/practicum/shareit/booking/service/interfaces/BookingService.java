package ru.practicum.shareit.booking.service.interfaces;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.ShortBookingItemDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BookingService {

    BookingResponseDto createBookingByUser(BookingRequestDto bookingRequestDto, Long userId, Item item);

    BookingResponseDto managingBookingStatus(Long bookingId, Long userId, Boolean approved);

    BookingResponseDto getBookingById(Long userId, Long bookingId);

    List<BookingResponseDto> getAllBookingsOfUser(Long userId, String state, Integer from, Integer size);

    List<BookingResponseDto> getAllBookingsOfAllUserItems(Long userId, String state, Integer from, Integer size);

    Optional<ShortBookingItemDto> findLastBookingByItemId(Long itemId);

    Optional<ShortBookingItemDto> findFutureBookingByItemId(Long itemId);

    Map<Item, List<Booking>> findAllBookingsByItemIds(List<Long> itemIds);
}
