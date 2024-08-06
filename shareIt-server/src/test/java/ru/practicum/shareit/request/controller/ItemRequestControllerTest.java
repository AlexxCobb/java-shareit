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
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.service.interfaces.ItemRequestService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @MockBean
    private ItemRequestService itemRequestService;

    @MockBean
    private ItemRequestMapper itemRequestMapper;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    private final EasyRandom generator = new EasyRandom();

    @Test
    void createRequest_whenArgumentsIsValid_thenReturnCorrectDto() throws Exception {
        var itemRequestDto = generator.nextObject(ItemRequestDto.class);
        itemRequestDto.setItems(null);
        itemRequestDto.setCreated(LocalDateTime.now().plusMinutes(10));
        itemRequestMapper.toItemRequest(itemRequestDto);

        when(itemRequestService.createRequest(any(ItemRequestDto.class), anyLong())).thenReturn(itemRequestDto);

        mvc.perform(post("/requests")
                        .header(Constant.HEADER_USER_ID, 1L)
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId())))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.created", is(itemRequestDto.getCreated().format(DateTimeFormatter.ofPattern(Constant.DATE_PATTERN)))));
    }

    @Test
    void getAllRequestsByUser_whenArgumentsIsValid_thenReturnCorrectDto() throws Exception {
        var itemRequest = generator.nextObject(ItemRequestDto.class);
        itemRequest.setItems(null);
        itemRequest.setCreated(LocalDateTime.now().plusMinutes(10));

        when(itemRequestService.getAllRequestsByUser(anyLong())).thenReturn(List.of(itemRequest));

        mvc.perform(get("/requests")
                        .header(Constant.HEADER_USER_ID, 1L)
                        .content(mapper.writeValueAsString(itemRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemRequest.getId())))
                .andExpect(jsonPath("$[0].description", is(itemRequest.getDescription())))
                .andExpect(jsonPath("$[0].created", is(itemRequest.getCreated().format(DateTimeFormatter.ofPattern(Constant.DATE_PATTERN)))));
    }

    @Test
    void getAllRequests_whenArgumentsIsValid_thenReturnCorrectDto() throws Exception {
        var itemRequest = generator.nextObject(ItemRequestDto.class);
        itemRequest.setItems(null);
        itemRequest.setCreated(LocalDateTime.now().plusMinutes(10));

        when(itemRequestService.getAllRequests(anyLong(), any(), any())).thenReturn(List.of(itemRequest));

        mvc.perform(get("/requests/all")
                        .header(Constant.HEADER_USER_ID, 1L)
                        .content(mapper.writeValueAsString(itemRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemRequest.getId())))
                .andExpect(jsonPath("$[0].description", is(itemRequest.getDescription())))
                .andExpect(jsonPath("$[0].created", is(itemRequest.getCreated().format(DateTimeFormatter.ofPattern(Constant.DATE_PATTERN)))));
    }

    @Test
    void getRequestById_whenArgumentsIsValid_thenReturnCorrectDto() throws Exception {
        var itemRequest = generator.nextObject(ItemRequestDto.class);
        itemRequest.setItems(null);
        itemRequest.setCreated(LocalDateTime.now().plusMinutes(10));

        when(itemRequestService.getRequestById(anyLong(), anyLong())).thenReturn(itemRequest);

        mvc.perform(get("/requests/{requestId}", 1)
                        .header(Constant.HEADER_USER_ID, 1L)
                        .content(mapper.writeValueAsString(itemRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequest.getId())))
                .andExpect(jsonPath("$.description", is(itemRequest.getDescription())))
                .andExpect(jsonPath("$.created", is(itemRequest.getCreated().format(DateTimeFormatter.ofPattern(Constant.DATE_PATTERN)))));
    }
}