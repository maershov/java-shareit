package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@Validated
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDto createBooking(@RequestHeader(value = "X-Sharer-User-Id") Long userId,
                                    @Valid @RequestBody BookingRequestDto bookingRequestDto) {
        log.info("Бронирование для пользователя с id " + userId + " создано");
        return bookingService.createBooking(userId, bookingRequestDto);
    }

    @PatchMapping(path = "/{bookingId}")
    public BookingDto updateBooking(@PathVariable Long bookingId,
                                    @RequestHeader(value = "X-Sharer-User-Id") Long userId,
                                    @RequestParam(value = "approved") Boolean approved) {
        log.info("Обновление бронирования для пользователя с id " + userId);
        return bookingService.updateBooking(bookingId, userId, approved);
    }

    @GetMapping(path = "/{bookingId}")
    public BookingDto findBookingByUserId(@PathVariable Long bookingId,
                                          @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        log.info("Бронирование с id " + bookingId + " найдено");
        return bookingService.findBookingByUserId(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> findByBooker(@RequestHeader(value = "X-Sharer-User-Id") Long userId,
                                         @RequestParam(defaultValue = "ALL") BookingState state,
                                         @RequestParam(defaultValue = "0")
                                         @PositiveOrZero(message = "Отсчет страницы должен быть значением >= 0")
                                         int from,
                                         @RequestParam(defaultValue = "20")
                                         @Positive(message = "Размер страницы должен быть значением > 0")
                                         int size) {
        log.info("Получен список всех бронирований пользователя с id " + userId);
        return bookingService.findByBooker(userId, state, from, size);
    }

    @GetMapping(path = "/owner")
    public List<BookingDto> findByOwner(@RequestHeader(value = "X-Sharer-User-Id") Long userId,
                                        @RequestParam(defaultValue = "ALL") BookingState state,
                                        @RequestParam(defaultValue = "0") @PositiveOrZero(message = "Отсчет страницы должен быть значением >= 0") int from,
                                        @RequestParam(defaultValue = "20") @Positive(message = "Размер страницы должен быть значением > 0") int size) {
        log.info("Получен список всех бронирований для всех вещей пользователя с id " + userId);
        return bookingService.findByOwner(userId, state, from, size);
    }
}
