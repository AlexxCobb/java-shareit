package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.ItemBookingValidationService;
import ru.practicum.shareit.booking.service.interfaces.BookingService;
import ru.practicum.shareit.constants.Constant;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final ItemBookingValidationService itemBookingValidationService;
    private final BookingService bookingService;

    @PostMapping
    public BookingResponseDto createBooking(@RequestBody BookingRequestDto bookingDto,
                                            @RequestHeader(Constant.HEADER_USER_ID) Long userId) {
        log.info("Поступил POST-запрос на добавление бронирования от user с id = {}", userId);
        var item = itemBookingValidationService.isItemAvailable(bookingDto.getItemId(), userId);
        return bookingService.createBookingByUser(bookingDto, userId, item);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto bookingStatusManagement(@PathVariable Long bookingId,
                                                      @RequestHeader(Constant.HEADER_USER_ID) Long userId,
                                                      @RequestParam Boolean approved) {
        log.info("Поступил PATCH-запрос на управление бронированием c id = {} от user c id = {}", bookingId, userId);
        return bookingService.managingBookingStatus(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getBookingInfo(@PathVariable Long bookingId,
                                             @RequestHeader(Constant.HEADER_USER_ID) Long userId) {
        log.info("Поступил GET-запрос на получение информации о бронировании booking с id = {} от user с id = {}", bookingId, userId);
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingResponseDto> getAllBookingsOfUser(@RequestHeader(Constant.HEADER_USER_ID) Long userId,
                                                         @RequestParam(defaultValue = "ALL") String state,
                                                         @RequestParam(defaultValue = "0") Integer from,
                                                         @RequestParam(defaultValue = "10") Integer size) {
        log.info("Поступил GET-запрос на получение списка всех бронирований всех вещей user с id = {}", userId);
        return bookingService.getAllBookingsOfUser(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getBookingsOfAllUserItems(@RequestHeader(Constant.HEADER_USER_ID) Long userId,
                                                              @RequestParam(defaultValue = "ALL") String state,
                                                              @RequestParam(defaultValue = "0") Integer from,
                                                              @RequestParam(defaultValue = "10") Integer size) {
        log.info("Поступил GET-запрос на получение списка бронирований всех вещей user с id = {}", userId);
        itemBookingValidationService.isUserHaveItems(userId);
        return bookingService.getAllBookingsOfAllUserItems(userId, state, from, size);
    }
}
