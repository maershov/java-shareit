package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

public class BookingMapperTest {
    private Booking booking;
    private BookingDto bookingDto;
    private BookingShortDto bookingShortDto;

    @BeforeEach
    void before() {
        User user = User
                .builder()
                .id(1L)
                .name("Ivan")
                .email("Ivan@mail.ru")
                .build();

        Item item = Item
                .builder()
                .id(1L)
                .name("Дрель")
                .description("дрель аккамуляторная")
                .available(true)
                .build();

        booking = Booking
                .builder()
                .id(1L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .item(item)
                .booker(user)
                .status(BookingStatus.APPROVED)
                .build();

        bookingDto = BookingMapper.toBookingDto(booking);
        bookingShortDto = BookingMapper.toBookingShortDto(booking);
    }

    @Test
    void toBookingDto() {
        Assertions.assertNotNull(booking);
        Assertions.assertEquals(booking.getStart(), bookingDto.getStart());
        Assertions.assertEquals(booking.getEnd(), bookingDto.getEnd());
        Assertions.assertEquals(booking.getItem().getId(), bookingDto.getItem().getId());
        Assertions.assertEquals(booking.getBooker().getId(), bookingDto.getBooker().getId());
        Assertions.assertEquals(booking.getStatus(), bookingDto.getStatus());
    }

    @Test
    void toBookingShortDto() {
        Assertions.assertNotNull(booking);
        Assertions.assertEquals(booking.getStart(), bookingShortDto.getStart());
        Assertions.assertEquals(booking.getEnd(), bookingShortDto.getEnd());
        Assertions.assertEquals(booking.getBooker().getId(), bookingShortDto.getBookerId());
        Assertions.assertEquals(booking.getStatus(), bookingShortDto.getStatus());
    }
}
