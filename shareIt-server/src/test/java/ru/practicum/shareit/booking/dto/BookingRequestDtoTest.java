package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.constants.Constant;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingRequestDtoTest {

    @Autowired
    private JacksonTester<BookingRequestDto> tester;

    @Test
    void test() throws IOException {
        BookingRequestDto requestDto = new BookingRequestDto(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(1));

        JsonContent<BookingRequestDto> result = tester.write(requestDto);
        assertThat(result).hasJsonPath("$.start");
        assertThat(result).hasJsonPath("$.end");
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo(requestDto.getStart().format(DateTimeFormatter.ofPattern(Constant.DATE_PATTERN)));
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo(requestDto.getEnd().format(DateTimeFormatter.ofPattern(Constant.DATE_PATTERN)));
    }
}