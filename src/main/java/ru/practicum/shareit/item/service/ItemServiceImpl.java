package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.InvalidBookingException;
import ru.practicum.shareit.exceptions.ModelNotFoundException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.dto.CommentRequestDto;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import static ru.practicum.shareit.item.mapper.ItemMapper.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;


    @Override
    public List<ItemDto> findAllItems() {
        log.info("Получен список вещей.");
        return getListItemDto(itemRepository.findAll());
    }

    @Override
    @Transactional
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        User user = getUserById(userId);
        Item createItem = toItem(itemDto);
        createItem.setOwner(user);
        log.info("Item added.");
        return toItemDto(itemRepository.save(createItem));
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long userId) {
        Item item = getById(itemId);
        if (!Objects.equals(item.getOwner().getId(), userId)) {
            throw new NotFoundException("Неверный ID пользователя.");
        }
        Item updatedItem = itemRepository.save(updateItemFields(item, itemDto));
        log.info("Предмет - {} с id - {} обновлен!", updatedItem.getName(), updatedItem.getId());
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDtoResponse getItemByUserId(Long itemId, Long userId) {
        Item item = getById(itemId);
        BookingShortDto nextBooking = null;
        BookingShortDto lastBooking = null;
        if (Objects.equals(item.getOwner().getId(), userId)) {
            nextBooking = bookingRepository.findTopByItemIdAndStatusAndStartIsAfterOrderByStart(itemId,
                    BookingStatus.APPROVED, LocalDateTime.now());
            lastBooking = bookingRepository.findTopByItemIdAndStatusAndStartIsBeforeOrderByEndDesc(itemId,
                    BookingStatus.APPROVED, LocalDateTime.now());
        }
        List<Comment> comments = commentRepository.findByItemId(itemId);

        log.info("Предмет - {} с id - {} запрошен!", item.getName(), item.getId());

        return ItemMapper.toItemDtoResponse(item, nextBooking, lastBooking, CommentMapper.listToDtoList(comments));
    }


    @Override
    @Transactional(readOnly = true)
    public List<ItemDtoResponse> getItemListByUserId(Long userId) {
        List<Item> items = itemRepository.findAllByOwnerIdOrderById(userId);
        if (items.isEmpty()) {
            log.info("Список предметов пользователя с id {} пуст!", userId);
            return Collections.emptyList();
        }
        log.info("Получен список всех вещей пользователя с ID: " + userId);
        return items.stream().map(i -> getItemByUserId(i.getId(), userId)).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> search(String query) {
        if (query.isBlank()) {
            log.info("Пустой параметр запроса!");
            return Collections.emptyList();
        }
        List<Item> items = itemRepository.search(query);
        if (items.isEmpty()) {
            log.info("По заданному порядку букв - {} предметов не найдено!", query);
            return Collections.emptyList();
        }
        log.info("Получен список предметов соответсвующий поиску по заданному порядку букв - {}!", query);
        return ItemMapper.getListItemDto(items);
    }

    @Override
    @Transactional
    public CommentDto saveComment(Long itemId, Long userId, CommentRequestDto commentRequestDto) {
        Item item = getById(itemId);
        User user = getUserById(userId);
        Booking booking = bookingRepository.findTopByItemIdAndBookerIdAndStatusAndEndIsBefore(
                        itemId, userId, BookingStatus.APPROVED, LocalDateTime.now())
                .orElseThrow(() -> new InvalidBookingException("Пользователь с id " + userId + " ранее не бронировал вещь с id " + itemId));

        Comment comment = CommentMapper.toComment(item, user, commentRequestDto.getText());

        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new ModelNotFoundException("Неверный ID пользователя."));
    }
    private Item getById(Long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() ->
                new ModelNotFoundException("Неверный ID."));
    }
    private Item updateItemFields(Item item, ItemDto itemDto) {
        if (itemDto.getName() != null && !itemDto.getName().equals(item.getName())) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().equals(item.getDescription())) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null && itemDto.getAvailable() != item.getAvailable()) {
            item.setAvailable(itemDto.getAvailable());
        }
        isValid(item);
        return item;
    }

    private void isValid(Item item) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<Item>> violations = validator.validate(item);
        if (!violations.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Переданы некорректные данные для обновления!");
        }
    }
}
