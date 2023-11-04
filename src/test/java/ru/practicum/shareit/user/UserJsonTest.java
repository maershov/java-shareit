package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class UserJsonTest {

    @Autowired
    JacksonTester<UserDto> json;

    @Test
    void serializeUserDtoToJson() throws Exception {
        UserDto userDto = UserDto
                .builder()
                .id(1L)
                .name("Petr")
                .email("petr@yandex.ru")
                .build();

        JsonContent<UserDto> result = json.write(userDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Petr");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("petr@yandex.ru");
    }
}
