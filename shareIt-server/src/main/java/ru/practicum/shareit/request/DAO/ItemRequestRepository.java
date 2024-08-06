package ru.practicum.shareit.request.DAO;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findAllRequestsByRequesterIdOrderByCreatedDesc(Long id);

    List<ItemRequest> findByRequesterIdNotOrderByCreatedDesc(Long id, Pageable pageable);
}
