package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.constants.Constant;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {


    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> createBooking(@Valid @RequestBody BookingRequestDto bookingDto,
                                                @RequestHeader(Constant.HEADER_USER_ID) Long userId) {
        log.info("Поступил POST-запрос на добавление бронирования от user с id = {}", userId);

        return bookingClient.createBooking(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> bookingStatusManagement(@PathVariable Long bookingId,
                                                          @RequestHeader(Constant.HEADER_USER_ID) Long userId,
                                                          @RequestParam Boolean approved) {
        log.info("Поступил PATCH-запрос на управление бронированием c id = {} от user c id = {}", bookingId, userId);
        return bookingClient.bookingStatusManagement(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingInfo(@PathVariable Long bookingId,
                                                 @RequestHeader(Constant.HEADER_USER_ID) Long userId) {
        log.info("Поступил GET-запрос на получение информации о бронировании booking с id = {} от user с id = {}", bookingId, userId);
        return bookingClient.getBookingById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllBookingsOfUser(@RequestHeader(Constant.HEADER_USER_ID) Long userId,
                                                       @RequestParam(defaultValue = "ALL") String state,
                                                       @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                       @RequestParam(defaultValue = "10") @Positive Integer size) {
        State.isStateValid(state);
        log.info("Поступил GET-запрос на получение списка всех бронирований всех вещей user с id = {}", userId);
        return bookingClient.getAllBookingsOfUser(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsOfAllUserItems(@RequestHeader(Constant.HEADER_USER_ID) Long userId,
                                                            @RequestParam(defaultValue = "ALL") String state,
                                                            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                            @RequestParam(defaultValue = "10") @Positive Integer size) {
        State.isStateValid(state);
        log.info("Поступил GET-запрос на получение списка бронирований всех вещей user с id = {}", userId);
        return bookingClient.getBookingsOfAllUserItems(userId, state, from, size);
    }
}
