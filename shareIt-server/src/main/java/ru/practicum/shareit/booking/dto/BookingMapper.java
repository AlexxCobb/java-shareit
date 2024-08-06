package ru.practicum.shareit.booking.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    Booking toBooking(BookingRequestDto bookingRequestDto);

    BookingResponseDto toBookingResponseDto(Booking booking);

    List<BookingResponseDto> toListBookingResponseDto(List<Booking> bookings);

    @Mapping(source = "booker.id", target = "bookerId")
    ShortBookingItemDto toShortBooking(Booking booking);

    List<ShortBookingItemDto> toListShortBooking(List<Booking> bookings);
}
