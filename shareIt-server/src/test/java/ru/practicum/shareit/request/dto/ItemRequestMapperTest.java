package ru.practicum.shareit.request.dto;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemRequestMapperTest {

    ItemRequestMapper itemRequestMapper = new ItemRequestMapperImpl();
    EasyRandom generator = new EasyRandom();

    @Test
    void toItemRequest() {
        var itemRequestDto = generator.nextObject(ItemRequestDto.class);
        var itemRequest = itemRequestMapper.toItemRequest(itemRequestDto);
        assertEquals(itemRequestDto.getDescription(), itemRequest.getDescription());
    }

    @Test
    void toItemRequestDto() {
        var itemRequest = generator.nextObject(ItemRequest.class);
        var itemRequestDto = itemRequestMapper.toItemRequestDto(itemRequest);
        assertEquals(itemRequest.getId(), itemRequestDto.getId());
        assertEquals(itemRequest.getDescription(), itemRequestDto.getDescription());
    }

    @Test
    void toItemRequestDtoList() {
        var itemRequest1 = generator.nextObject(ItemRequest.class);
        var itemRequest2 = generator.nextObject(ItemRequest.class);
        var listItems = itemRequestMapper.toItemRequestDtoList(List.of(itemRequest1, itemRequest2));
        assertEquals(2, listItems.size());
        assertEquals(itemRequest1.getItems().get(0).getId(), listItems.get(0).getItems().get(0).getId());
    }
}