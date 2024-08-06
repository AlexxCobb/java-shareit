package ru.practicum.shareit.request.dto;

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
class ItemRequestDtoTest {

    @Autowired
    private JacksonTester<ItemRequestDto> tester;

    @Test
    void test() throws IOException {
        ItemRequestDto requestDto = new ItemRequestDto(1L, "desc", LocalDateTime.now().plusMinutes(30), null);

        JsonContent<ItemRequestDto> result = tester.write(requestDto);

        assertThat(result).hasJsonPath("$.created");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo(requestDto.getCreated().format(DateTimeFormatter.ofPattern(Constant.DATE_PATTERN)));
    }
}