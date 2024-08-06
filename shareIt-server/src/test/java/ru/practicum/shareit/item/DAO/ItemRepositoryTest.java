package ru.practicum.shareit.item.DAO;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.DAO.UserRepository;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void searchItemToRentEmpty() {
        var items = itemRepository.searchItemToRent("", Pageable.ofSize(1));
        assertTrue(items.isEmpty());
    }

    @Test
    void searchItemToRent_whenSearchByName_thenReturnListItemsCorrect() {
        var user = new User(null, "name", "name@ya.ru");
        userRepository.save(user);
        var item = new Item(null, "Spoon", "description", true, user, null);
        itemRepository.save(item);

        var items = itemRepository.searchItemToRent("poon", Pageable.ofSize(1));
        assertEquals(1, items.size());
        assertEquals(item.getName(), items.get(0).getName());

    }

    @Test
    void searchItemToRent_whenSearchByDescription_thenReturnListItemsCorrect() {
        var user = new User(null, "name", "name@ya.ru");
        userRepository.save(user);
        var item = new Item(null, "Spoon", "description", true, user, null);
        itemRepository.save(item);

        var items = itemRepository.searchItemToRent("script", Pageable.ofSize(1));
        assertEquals(1, items.size());
        assertEquals(item.getDescription(), items.get(0).getDescription());
    }
}