package ru.practicum.shareit.item.dto;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemMapperTest {

    ItemMapper itemMapper = new ItemMapperImpl();
    EasyRandom generator = new EasyRandom();

    @Test
    void toItemDto() {
        var item = generator.nextObject(Item.class);
        var itemDto = itemMapper.toItemDto(item);

        assertEquals(item.getId(),itemDto.getId());
    }

    @Test
    void toItem() {
        var itemDto = generator.nextObject(ItemDto.class);
        var item = itemMapper.toItem(itemDto);

        assertEquals(itemDto.getAvailable(), item.getAvailable());
    }

    @Test
    void toListItemDto() {
        var item = generator.nextObject(Item.class);
        var item2 = generator.nextObject(Item.class);
        var itemDtos = itemMapper.toListItemDto(List.of(item, item2));

        assertEquals(item.getId(),itemDtos.get(0).getId());
    }

    @Test
    void updateItemFromItemDto() {
        var item = generator.nextObject(Item.class);
        var itemDto = generator.nextObject(ItemDto.class);

        itemMapper.updateItemFromItemDto(itemDto,item);

        assertEquals(item.getId(),itemDto.getId());
    }
}