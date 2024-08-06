package ru.practicum.shareit.booking.dto;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookingMapperTest {

    BookingMapper bookingMapper = new BookingMapperImpl();
    EasyRandom generator = new EasyRandom();

    @Test
    void toBooking() {
        var bookingDto = generator.nextObject(BookingRequestDto.class);
        var booking = bookingMapper.toBooking(bookingDto);
        assertEquals(bookingDto.getStart(), booking.getStart());
    }

    @Test
    void toBookingResponseDto() {
        var booking = generator.nextObject(Booking.class);
        var bookingDto = bookingMapper.toBookingResponseDto(booking);
        assertEquals(booking.getStart(), bookingDto.getStart());
    }

    @Test
    void toListBookingResponseDto() {
        var booking1 = generator.nextObject(Booking.class);
        var booking2 = generator.nextObject(Booking.class);
        var bookingDto = bookingMapper.toListBookingResponseDto(List.of(booking1, booking2));
        assertEquals(2, bookingDto.size());
        assertEquals(booking1.getStart(), bookingDto.get(0).getStart());
    }

    @Test
    void toShortBooking() {
        var booking = generator.nextObject(Booking.class);
        var shortBooking = bookingMapper.toShortBooking(booking);
        assertEquals(booking.getBooker().getId(), shortBooking.getBookerId());
    }

    @Test
    void toListShortBooking() {
        var booking1 = generator.nextObject(Booking.class);
        var booking2 = generator.nextObject(Booking.class);
        var shortBooking = bookingMapper.toListShortBooking(List.of(booking1, booking2));
        assertEquals(booking1.getBooker().getId(), shortBooking.get(0).getBookerId());
    }
}