package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.DAO.BookingRepository;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.DAO.ItemRepository;
import ru.practicum.shareit.item.comment.DAO.CommentRepository;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.interfaces.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.dto.UserMapperImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.interfaces.UserService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ItemServiceImplIntegrationTest {

    private final ItemService itemService;

    private final UserService userService;

    private final ItemRepository itemRepository;

    private final CommentRepository commentRepository;

    private final BookingRepository bookingRepository;

    private final UserMapper userMapper = new UserMapperImpl();

    @Test
    void getUserItems() {
        var bookerDto = createUserDto("name@ya.ru");
        var ownerDto = createUserDto("othername@ya.ru");
        var createdBooker = userService.addUser(bookerDto);
        var createdOwner = userService.addUser(ownerDto);
        var booker = userMapper.toUser(createdBooker);
        var owner = userMapper.toUser(createdOwner);
        var item = createItem(owner);
        var item2 = createItem(owner);
        var createdItem = itemRepository.save(item);
        var createdItem2 = itemRepository.save(item2);
        var bookingNext = createBooking(LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2), booker, createdItem);
        var bookingLast = createBooking(LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(1), booker, createdItem);
        var createdBookingNext = bookingRepository.save(bookingNext);
        var createdBookingLast = bookingRepository.save(bookingLast);
        createdBookingNext.setStatus(Status.APPROVED);
        createdBookingLast.setStatus(Status.APPROVED);
        var comment = createComment(booker, createdItem);
        var createdComment = commentRepository.save(comment);

        var result = itemService.getUserItems(owner.getId(), 0, 10);

        assertEquals(2, result.size());
        assertEquals(createdBookingLast.getId(), result.get(0).getLastBooking().getId());
        assertEquals(createdBookingNext.getId(), result.get(0).getNextBooking().getId());
        assertEquals(createdItem2.getId(), result.get(1).getId());
    }

    private UserDto createUserDto(String mail) {
        return new UserDto(null, "user", mail);
    }

    private Item createItem(User user) {
        return new Item(null, "Spoon", "description", true, user, null);
    }

    private Comment createComment(User user, Item item) {
        return new Comment(null, "comment", item, user, LocalDateTime.now());
    }

    private Booking createBooking(LocalDateTime start, LocalDateTime end, User booker, Item item) {
        return new Booking(null, start, end, Status.WAITING, booker, item);
    }
}