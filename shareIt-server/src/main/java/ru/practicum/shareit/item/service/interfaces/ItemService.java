package ru.practicum.shareit.item.service.interfaces;

import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;
import java.util.Map;

public interface ItemService {
    ItemDto createItem(ItemDto itemDto, Long userId);

    ItemDto updateItem(Long itemId, ItemDto itemDto, Long userId);

    ItemDto getItemById(Long userId, Long itemId);

    List<ItemDto> getUserItems(Long userId, Integer from, Integer size);

    List<ItemDto> searchItemToRent(Long userId, String text, Integer from, Integer size);

    Map<ItemRequest, List<Item>> findAllItemsByRequestIds(List<Long> requestIds);

    List<ItemDto> findAllItemsByRequestId(Long requestId);

    CommentDto createComment(CommentDto commentDto, Long itemId, Long userId);

    Boolean isUserHaveItems(Long userId);
}
