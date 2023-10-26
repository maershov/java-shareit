package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.InvalidBookingException;
import ru.practicum.shareit.exceptions.ModelNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingDto create(Long userId, BookingRequestDto bookingRequestDto) {
        User booker = getUserById(userId);
        Item item = getItemById(bookingRequestDto.getItemId());
        if (item.getOwner().getId().equals(userId)) {
            throw new ModelNotFoundException("Невозможно забронировать вещь");
        }
        if (!item.getAvailable()) {
            throw new InvalidBookingException("Вещь c id " + item.getId() + " недоступна для бронирования");
        }

        Booking booking = BookingMapper.toBooking(bookingRequestDto, booker, item);
        log.info("Бронирование для пользователя с id " + userId + " создано");
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto update(Long id, Long userId, boolean approved) {
        Booking booking = getBookingById(id);

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ModelNotFoundException("Невозможно забронировать вещь");
        }
        if (booking.getStatus() == BookingStatus.APPROVED) {
            throw new InvalidBookingException("Бронирование с id " + id + "уже подтверждена");
        }
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Обновление бронирования для пользователя с id " + userId);
        return BookingMapper.toBookingDto(updatedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto findById(Long id, Long userId) {
        Booking booking = getBookingById(id);
        if (booking.getItem().getOwner().getId().equals(userId) || booking.getBooker().getId().equals(userId)) {
            log.info("Бронирование с id " + id + " найдено");
            return BookingMapper.toBookingDto(booking);
        } else {
            throw new ModelNotFoundException("Невозможно получить информацию о вещи");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> findByBooker(Long userId, BookingState state) {
        getUserById(userId);

        List<Booking> bookings;
        switch (state) {
            case ALL:
                bookings = bookingRepository.findAllByBookerIdOrderByStartDesc(userId);
                break;
            case CURRENT:
                bookings = bookingRepository.findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(userId,
                        LocalDateTime.now(),
                        LocalDateTime.now());
                break;
            case PAST:
                bookings = bookingRepository.findAllByBookerIdAndEndIsBeforeOrderByStartDesc(userId,
                        LocalDateTime.now());
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByBookerIdAndStartIsAfterOrderByStartDesc(userId,
                        LocalDateTime.now());
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId,
                        BookingStatus.REJECTED);
                break;
            case WAITING:
                bookings = bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId,
                        BookingStatus.WAITING);
                break;
            default:
                throw new InvalidBookingException("Unknown state: " + state);
        }
        log.info("Получен список всех бронирований пользователя с id " + userId);
        return BookingMapper.getListOfBookingDto(bookings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> findByOwner(Long userId, BookingState state) {
        getUserById(userId);

        List<Booking> bookings;
        switch (state) {
            case ALL:
                bookings = bookingRepository.findAllByItemOwnerIdOrderByStartDesc(userId);
                break;
            case CURRENT:
                bookings = bookingRepository.findAllByItemOwnerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(userId,
                        LocalDateTime.now(),
                        LocalDateTime.now());
                break;
            case PAST:
                bookings = bookingRepository.findAllByItemOwnerIdAndEndIsBeforeOrderByStartDesc(userId,
                        LocalDateTime.now());
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByItemOwnerIdAndStartIsAfterOrderByStartDesc(userId,
                        LocalDateTime.now());
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId,
                        BookingStatus.REJECTED);
                break;
            case WAITING:
                bookings = bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId,
                        BookingStatus.WAITING);
                break;
            default:
                throw new InvalidBookingException("Unknown state: " + state);

        }
        log.info("Получен список всех бронирований для всех вещей пользователя с id " + userId);
        return BookingMapper.getListOfBookingDto(bookings);
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
        return bookingRepository.findById(bookingId).orElseThrow(() -> new ModelNotFoundException("Бронирование с id " + bookingId + " не найдено"));
    }

}

