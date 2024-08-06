package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.constants.Constant;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @MockBean
    private ItemRequestClient itemRequestClient;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    private final EasyRandom generator = new EasyRandom();

    @Test
    void createRequest_whenDescriptionIsBlank_thenException() throws Exception {
        var itemRequestDto = generator.nextObject(ItemRequestDto.class);
        itemRequestDto.setItems(null);
        itemRequestDto.setDescription("");
        itemRequestDto.setCreated(LocalDateTime.now().plusMinutes(10));

        mvc.perform(post("/requests")
                        .header(Constant.HEADER_USER_ID, 1L)
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Validation failed for argument [0] in public org.springframework.http.ResponseEntity<java.lang.Object>")));

    }

    @Test
    void getAllRequests_whenPaginationNotValid_thenException() throws Exception {
        var itemRequest = generator.nextObject(ItemRequestDto.class);

        mvc.perform(get("/requests/all")
                        .param("from", "-5")
                        .header(Constant.HEADER_USER_ID, 1L)
                        .content(mapper.writeValueAsString(itemRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.error").value(containsString("Произошла непредвиденная ошибка.")));
    }

    @Test
    void createRequest_whenTimeNotValid_thenException() throws Exception {
        var itemRequest = generator.nextObject(ItemRequestDto.class);
        itemRequest.setItems(null);
        itemRequest.setCreated(LocalDateTime.now().minusHours(10));

        mvc.perform(post("/requests")
                        .header(Constant.HEADER_USER_ID, 1L)
                        .content(mapper.writeValueAsString(itemRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Validation failed for argument [0] in public org.springframework.http.ResponseEntity<java.lang.Object>")));
    }
}