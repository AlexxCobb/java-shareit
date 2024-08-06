package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.ItemBookingValidationService;
import ru.practicum.shareit.booking.service.interfaces.BookingService;
import ru.practicum.shareit.constants.Constant;
import ru.practicum.shareit.item.model.Item;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {

    @MockBean
    private BookingService bookingService;

    @MockBean
    private ItemBookingValidationService validationService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    private final EasyRandom generator = new EasyRandom();


    @Test
    void createBooking_whenArgumentsIsValid_thenReturnCorrectDto() throws Exception {
        var bookingRequestDto = generator.nextObject(BookingRequestDto.class);
        bookingRequestDto.setStart(LocalDateTime.now().plusMinutes(10));
        bookingRequestDto.setEnd(LocalDateTime.now().plusMinutes(50));
        var bookingResponseDto = generator.nextObject(BookingResponseDto.class);

        when(validationService.isItemAvailable(anyLong(),anyLong())).thenReturn(new Item());
        when(bookingService.createBookingByUser(any(BookingRequestDto.class), anyLong(), any(Item.class))).thenReturn(bookingResponseDto);


        mvc.perform(post("/bookings")
                        .header(Constant.HEADER_USER_ID, 1L)
                        .content(mapper.writeValueAsString(bookingRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingResponseDto.getId())))
                .andExpect(jsonPath("$.booker.id", is(bookingResponseDto.getBooker().getId())))
                .andExpect(jsonPath("$.item.id", is(bookingResponseDto.getItem().getId())))
                .andExpect(jsonPath("$.item.name", is(bookingResponseDto.getItem().getName())))
                .andExpect(jsonPath("$.status", is(bookingResponseDto.getStatus().toString())))
                .andExpect(jsonPath("$.start", is(bookingResponseDto.getStart().format(DateTimeFormatter.ofPattern(Constant.DATE_PATTERN)))))
                .andExpect(jsonPath("$.end", is(bookingResponseDto.getEnd().format(DateTimeFormatter.ofPattern(Constant.DATE_PATTERN)))));
    }


    @Test
    void createBooking_whenHeaderNotExist_thenException() throws Exception {
        var bookingRequestDto = generator.nextObject(BookingRequestDto.class);
        bookingRequestDto.setStart(LocalDateTime.now().plusMinutes(10));
        bookingRequestDto.setEnd(LocalDateTime.now().plusHours(10));

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.error").value(containsString("Произошла непредвиденная ошибка.")));
    }

    @Test
    void bookingStatusManagement_whenRequestParamNotExist_thenException() throws Exception {
        var bookingResponseDto = generator.nextObject(BookingResponseDto.class);

        mvc.perform(patch("/bookings/{bookingId}", 1)
                        .header(Constant.HEADER_USER_ID, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.error").value(containsString("Произошла непредвиденная ошибка.")));
    }

    @Test
    void bookingStatusManagement_whenArgumentsIsValid_thenReturnCorrectDto() throws Exception {
        var bookingResponseDto = generator.nextObject(BookingResponseDto.class);

        when(bookingService.managingBookingStatus(anyLong(), anyLong(), any(Boolean.class))).thenReturn(bookingResponseDto);

        mvc.perform(patch("/bookings/{bookingId}", 1)
                        .param("approved", "true")
                        .header(Constant.HEADER_USER_ID, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingResponseDto.getId())))
                .andExpect(jsonPath("$.booker.id", is(bookingResponseDto.getBooker().getId())))
                .andExpect(jsonPath("$.item.id", is(bookingResponseDto.getItem().getId())))
                .andExpect(jsonPath("$.item.name", is(bookingResponseDto.getItem().getName())))
                .andExpect(jsonPath("$.status", is(bookingResponseDto.getStatus().toString())))
                .andExpect(jsonPath("$.start", is(bookingResponseDto.getStart().format(DateTimeFormatter.ofPattern(Constant.DATE_PATTERN)))))
                .andExpect(jsonPath("$.end", is(bookingResponseDto.getEnd().format(DateTimeFormatter.ofPattern(Constant.DATE_PATTERN)))));
    }

    @Test
    void getBookingInfo_whenArgumentsIsValid_thenReturnCorrectDto() throws Exception {
        var bookingResponseDto = generator.nextObject(BookingResponseDto.class);

        when(bookingService.getBookingById(anyLong(), anyLong())).thenReturn(bookingResponseDto);

        mvc.perform(get("/bookings/{bookingId}", 1)
                        .header(Constant.HEADER_USER_ID, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingResponseDto.getId())))
                .andExpect(jsonPath("$.booker.id", is(bookingResponseDto.getBooker().getId())))
                .andExpect(jsonPath("$.item.id", is(bookingResponseDto.getItem().getId())))
                .andExpect(jsonPath("$.item.name", is(bookingResponseDto.getItem().getName())))
                .andExpect(jsonPath("$.status", is(bookingResponseDto.getStatus().toString())))
                .andExpect(jsonPath("$.start", is(bookingResponseDto.getStart().format(DateTimeFormatter.ofPattern(Constant.DATE_PATTERN)))))
                .andExpect(jsonPath("$.end", is(bookingResponseDto.getEnd().format(DateTimeFormatter.ofPattern(Constant.DATE_PATTERN)))));
    }

    @Test
    void getAllBookingsOfUser_whenPaginationNotValid_thenException() throws Exception {
        mvc.perform(patch("/bookings")
                        .param("from", "-5")
                        .header(Constant.HEADER_USER_ID, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.error").value(containsString("Произошла непредвиденная ошибка.")));
    }

    @Test
    void getBookingsOfAllUserItems_whenArgumentsIsValid_thenReturnCorrectDto() throws Exception {
        var bookingResponseDto = generator.nextObject(BookingResponseDto.class);

        when(bookingService.getAllBookingsOfAllUserItems(anyLong(), any(String.class), any(), any())).thenReturn(List.of(bookingResponseDto));

        mvc.perform(get("/bookings/owner", 1)
                        .header(Constant.HEADER_USER_ID, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(bookingResponseDto.getId())))
                .andExpect(jsonPath("$[0].booker.id", is(bookingResponseDto.getBooker().getId())))
                .andExpect(jsonPath("$[0].item.id", is(bookingResponseDto.getItem().getId())))
                .andExpect(jsonPath("$[0].item.name", is(bookingResponseDto.getItem().getName())))
                .andExpect(jsonPath("$[0].status", is(bookingResponseDto.getStatus().toString())))
                .andExpect(jsonPath("$[0].start", is(bookingResponseDto.getStart().format(DateTimeFormatter.ofPattern(Constant.DATE_PATTERN)))))
                .andExpect(jsonPath("$[0].end", is(bookingResponseDto.getEnd().format(DateTimeFormatter.ofPattern(Constant.DATE_PATTERN)))));
    }
}
