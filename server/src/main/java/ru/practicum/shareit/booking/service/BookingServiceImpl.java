package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.InvalidBookingException;
import ru.practicum.shareit.error.ModelNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.shareit.booking.mapper.BookingMapper.*;
import static ru.practicum.shareit.booking.model.BookingStatus.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final Sort sort = Sort.by(Sort.Direction.DESC, "end");

    @Override
    @Transactional
    public BookingDto createBooking(Long userId, BookingRequestDto bookingRequestDto) {
        User booker = getUserById(userId);
        Item item = getItemById(bookingRequestDto.getItemId());
        if (item.getOwner().getId().equals(userId)) {
            throw new ModelNotFoundException("Невозможно забронировать вещь");
        }
        if (!item.getAvailable()) {
            throw new InvalidBookingException("Вещь c id " + item.getId() + " недоступна для бронирования");
        }

        Booking booking = toBooking(bookingRequestDto, booker, item);
        booking.setStatus(WAITING);
        log.info("Бронирование для пользователя с id " + userId + " создано");
        return toBookingDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto updateBooking(Long id, Long userId, Boolean approved) {
        Booking booking = getBookingById(id);

        if (!booking.getItem().getOwner().getId().equals(userId) || booking.getStart().isBefore(LocalDateTime.now())) {
            throw new ModelNotFoundException("Невозможно забронировать вещь");
        }
        if (!booking.getStatus().equals(WAITING)) {
            throw new InvalidBookingException("Невозможно изменить статус бронирования.");
        }
        if (approved) {
            booking.setStatus(APPROVED);
        } else {
            booking.setStatus(REJECTED);
        }
        log.info("Обновление бронирования для пользователя с id " + userId);
        return toBookingDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto findBookingByUserId(Long id, Long userId) {
        Booking booking = getBookingById(id);
        if (booking.getItem().getOwner().getId().equals(userId) || booking.getBooker().getId().equals(userId)) {
            log.info("Бронирование с id " + id + " найдено");
            return toBookingDto(booking);
        } else {
            throw new ModelNotFoundException("Невозможно получить информацию о вещи");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> findByBooker(Long userId, BookingState state, int from, int size) {
        getUserById(userId);

        List<Booking> bookings;
        Pageable page = PageRequest.of(from / size, size, sort);
        switch (state) {
            case ALL:
                bookings = bookingRepository.findAllByBookerIdOrderByStartDesc(userId, page);
                break;
            case PAST:
                bookings = bookingRepository.findAllByBookerIdAndEndIsBeforeOrderByStartDesc(userId, LocalDateTime.now(), page);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByBookerIdAndStartIsAfterOrderByStartDesc(userId, LocalDateTime.now(), page);
                break;
            case CURRENT:
                bookings = bookingRepository.findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(userId,
                        LocalDateTime.now(), LocalDateTime.now(), page);
                break;
            case WAITING:
                bookings = bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, WAITING, page);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, REJECTED, page);
                break;
            default:
                throw new InvalidBookingException("Unknown state: " + state);
        }
        log.info("Получен список всех бронирований пользователя с id " + userId);
        return getListOfBookingDto(bookings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> findByOwner(Long userId, BookingState state, int from, int size) {
        getUserById(userId);

        List<Booking> bookings;
        Pageable page = PageRequest.of(from / size, size, sort);
        switch (state) {
            case ALL:
                bookings = bookingRepository.findAllByItemOwnerIdOrderByStartDesc(userId, page);
                break;
            case PAST:
                bookings = bookingRepository.findAllByItemOwnerIdAndEndIsBeforeOrderByStartDesc(userId, LocalDateTime.now(), page);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByItemOwnerIdAndStartIsAfterOrderByStartDesc(userId, LocalDateTime.now(), page);
                break;
            case CURRENT:
                bookings = bookingRepository.findAllByItemOwnerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(userId,
                        LocalDateTime.now(), LocalDateTime.now(), page);
                break;
            case WAITING:
                bookings = bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, WAITING, page);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, REJECTED, page);
                break;
            default:
                throw new InvalidBookingException("Unknown state: " + state);
        }
        log.info("Получен список всех бронирований для всех вещей пользователя с id " + userId);
        return getListOfBookingDto(bookings);
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new ModelNotFoundException("Неверный ID пользователя"));
    }

    private Item getItemById(Long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() ->
                new ModelNotFoundException("Неверный ID вещи"));
    }

    private Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(() -> new ModelNotFoundException("Бронирование на найдено!."));
    }

}

