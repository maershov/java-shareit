package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {

    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> createBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                              @RequestBody @Valid BookItemRequestDto requestDto) {
        log.info("Бронирование для пользователя с id " + userId + " создано");
        return bookingClient.createBooking(userId, requestDto);
    }

    @PatchMapping("/{bookingId}")
    ResponseEntity<Object> updateBooking(@PathVariable Long bookingId, @RequestHeader("X-Sharer-User-Id") Long userId,
                                         @RequestParam Boolean approved) {
        log.info("Обновление бронирования для пользователя с id " + userId);
        return bookingClient.updateBooking(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> findBookingByUserId(@RequestHeader("X-Sharer-User-Id") long userId,
                                          @PathVariable Long bookingId) {
        log.info("Бронирование с id " + bookingId + " найдено");
        return bookingClient.findBookingByUserId(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> findByBooker(@RequestHeader("X-Sharer-User-Id") long userId,
                                                 @RequestParam(name = "state", defaultValue = "all") String stateParam,
                                                 @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                 @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {

        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        log.info("Получен список всех бронирований пользователя с id " + userId);
        return bookingClient.findByBooker(userId, state, from, size);
    }

    @GetMapping("/owner")
    ResponseEntity<Object> findByOwner(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @RequestParam(name = "state", defaultValue = "all") String stateParam,
                                         @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                         @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {

        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        log.info("Получен список всех бронирований для всех вещей пользователя с id " + userId);
        return bookingClient.findByOwner(userId, state, from, size);
    }
}
