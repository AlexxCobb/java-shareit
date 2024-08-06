package ru.practicum.shareit.booking.DAO;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.DAO.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.DAO.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;


    @Test
    void findLastBookingByItemIdEmpty() {
        var bookings = bookingRepository.findLastBookingByItemId(1L, LocalDateTime.now(), Status.REJECTED, Pageable.ofSize(1));
        assertTrue(bookings.isEmpty());
    }

    @Test
    void findLastBookingByItemId_whenDataValid_thenReturnBookingCorrect() {
        var user = new User(null, "name", "name@ya.ru");
        var booker = new User(null, "booker", "booker@ya.ru");
        var bookerCreated = userRepository.save(booker);
        var userCreated = userRepository.save(user);
        var item = new Item(null, "Spoon", "description", true, userCreated, null);
        var itemCreated = itemRepository.save(item);
        var booking = new Booking(null, LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(1), Status.APPROVED, bookerCreated, itemCreated);
        var bookingCreated = bookingRepository.save(booking);

        var bookings = bookingRepository.findLastBookingByItemId(itemCreated.getId(), LocalDateTime.now(), Status.REJECTED, Pageable.ofSize(1));

        assertEquals(bookingCreated.getStart(), bookings.get(0).getStart());
        assertEquals(bookingCreated.getId(), bookings.get(0).getId());
    }

    @Test
    void findFutureBookingByItemIdEmpty() {
        var bookings = bookingRepository.findFutureBookingByItemId(1L, LocalDateTime.now().plusHours(1), Status.REJECTED, Pageable.ofSize(1));
        assertTrue(bookings.isEmpty());
    }

    @Test
    void findFutureBookingByItemId_whenDataValid_thenReturnBookingCorrect() {
        var user = new User(null, "name", "name@ya.ru");
        var booker = new User(null, "booker", "booker@ya.ru");
        var bookerCreated = userRepository.save(booker);
        var userCreated = userRepository.save(user);
        var item = new Item(null, "Spoon", "description", true, userCreated, null);
        var itemCreated = itemRepository.save(item);
        var booking = new Booking(null, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2), Status.APPROVED, bookerCreated, itemCreated);
        var bookingCreated = bookingRepository.save(booking);

        var bookings = bookingRepository.findFutureBookingByItemId(itemCreated.getId(), LocalDateTime.now(), Status.REJECTED, Pageable.ofSize(1));

        assertEquals(bookingCreated.getStart(), bookings.get(0).getStart());
        assertEquals(bookingCreated.getId(), bookings.get(0).getId());
    }
}