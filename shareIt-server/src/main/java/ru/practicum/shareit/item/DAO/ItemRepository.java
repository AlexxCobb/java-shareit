package ru.practicum.shareit.item.DAO;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;


public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query(" select i from Item i " +
            "where upper(i.name) like upper(concat('%', :text, '%')) " +
            " or upper(i.description) like upper(concat('%', :text, '%'))")
    List<Item> searchItemToRent(String text, Pageable pageable);

    List<Item> findAllByOwnerIdOrderById(Long userId, Pageable pageable);

    List<Item> findAllItemsByItemRequestIdIn(List<Long> ids);

    List<Item> findAllItemsByItemRequestId(Long itemRequestId);

    boolean existsByOwnerId(Long id);
}
