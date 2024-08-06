package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.constants.Constant;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @MockBean
    private ItemClient itemClient;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private final EasyRandom generator = new EasyRandom();

    @Test
    void createItem_whenNameIsBlank_thenException() throws Exception {
        var itemDto = generator.nextObject(ItemDto.class);
        itemDto.setName("");

        mvc.perform(post("/items")
                        .header(Constant.HEADER_USER_ID, 1L)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Validation failed for argument [0] in public org.springframework.http.ResponseEntity<java.lang.Object> ")));
    }

    @Test
    void getUserItems_whenPaginationNotValid_thenException() throws Exception {
        var itemDto = generator.nextObject(ItemDto.class);

        mvc.perform(get("/items")
                        .param("from", "-5")
                        .header(Constant.HEADER_USER_ID, 1L)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.error").value(containsString("Произошла непредвиденная ошибка.")));
    }

    @Test
    void createItem_whenAvailableIsNull_thenException() throws Exception {
        var itemDto = generator.nextObject(ItemDto.class);
        itemDto.setAvailable(null);

        mvc.perform(post("/items")
                        .header(Constant.HEADER_USER_ID, 1L)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Validation failed for argument [0] in public org.springframework.http.ResponseEntity<java.lang.Object> ")));
    }

    @Test
    void createItem_whenDescriptionIsBlank_thenException() throws Exception {
        var itemDto = generator.nextObject(ItemDto.class);
        itemDto.setDescription("");

        mvc.perform(post("/items")
                        .header(Constant.HEADER_USER_ID, 1L)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Validation failed for argument [0] in public org.springframework.http.ResponseEntity<java.lang.Object> ")));
    }

    @Test
    void createComment_whenTextIsBlank_thenException() throws Exception {
        var commentDto = generator.nextObject(CommentDto.class);
        commentDto.setText("");

        mvc.perform(post("/items/{itemId}/comment", 1)
                        .header(Constant.HEADER_USER_ID, 1L)
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Validation failed for argument [0] in public org.springframework.http.ResponseEntity<java.lang.Object> ")));
    }
}