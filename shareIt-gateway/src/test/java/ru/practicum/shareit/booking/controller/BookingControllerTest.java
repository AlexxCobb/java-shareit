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
import ru.practicum.shareit.constants.Constant;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {

    @MockBean
    private BookingClient bookingClient;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    private final EasyRandom generator = new EasyRandom();

    @Test
    void createBooking_whenStartDateNotValid_thenException() throws Exception {
        var bookingRequestDto = generator.nextObject(BookingRequestDto.class);
        bookingRequestDto.setStart(LocalDateTime.now().minusHours(10));

        mvc.perform(post("/bookings")
                        .header(Constant.HEADER_USER_ID, 1L)
                        .content(mapper.writeValueAsString(bookingRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Validation failed for argument [0]" +
                        " in public org.springframework.http.ResponseEntity<java.lang.Object> ru.practicum.shareit.booking.controller")));
    }

    @Test
    void createBooking_whenEndDateNotValid_thenException() throws Exception {
        var bookingRequestDto = generator.nextObject(BookingRequestDto.class);
        bookingRequestDto.setStart(LocalDateTime.now().plusMinutes(10));
        bookingRequestDto.setEnd(LocalDateTime.now().minusHours(10));

        mvc.perform(post("/bookings")
                        .header(Constant.HEADER_USER_ID, 1L)
                        .content(mapper.writeValueAsString(bookingRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Validation failed for argument [0] in public org.springframework.http.ResponseEntity<java.lang.Object> ru.practicum.shareit.booking.controller")));

    }

    @Test
    void createBooking_whenItemIsNull_thenException() throws Exception {
        var bookingRequestDto = generator.nextObject(BookingRequestDto.class);
        bookingRequestDto.setStart(LocalDateTime.now().plusMinutes(10));
        bookingRequestDto.setEnd(LocalDateTime.now().plusHours(10));
        bookingRequestDto.setItemId(null);

        mvc.perform(post("/bookings")
                        .header(Constant.HEADER_USER_ID, 1L)
                        .content(mapper.writeValueAsString(bookingRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Validation failed for argument [0] in public org.springframework.http.ResponseEntity<java.lang.Object> ru.practicum.shareit.booking.controller")));
    }
}
