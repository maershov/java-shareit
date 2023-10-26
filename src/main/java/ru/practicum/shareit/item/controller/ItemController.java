package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDto createItem(@RequestHeader(name = "X-Sharer-User-Id") Long userId, @Valid @RequestBody ItemDto itemDto) {
        log.info("Товар создан с владельцем id: " + userId);
        return itemService.createItem(userId, itemDto);
    }


    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long userId,
                              @RequestBody ItemDto itemDto) {
        log.info("Данные вещи обновлены.");
        return itemService.updateItem(itemId, itemDto, userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemByUserId(@PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получена вещь с id: " + itemId);
        return itemService.getItemByUserId(itemId, userId);
    }

    @GetMapping
    public List<ItemDto> getItemListByUserId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @RequestParam(defaultValue = "0") @Min(0) int from,
                                             @RequestParam(defaultValue = "20") @Min(1) int size) {
        log.info("Получен список всех вещей пользователя с ID: " + userId);
        return itemService.getItemListByUserId(userId, from, size);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam(value = "text") String text,
                                @RequestParam(defaultValue = "0") @Min(0) int from,
                                @RequestParam(defaultValue = "20") @Min(1) int size) {
        log.info("Найдена вещь по ключевому слову: " + text);
        return itemService.search(text, from, size);
    }

    @PostMapping(path = "/{itemId}/comment")
    public CommentDto saveComment(@PathVariable Long itemId,
                                  @RequestHeader(value = "X-Sharer-User-Id") Long userId,
                                  @RequestBody @Valid CommentDto commentDto) {
        return itemService.saveComment(itemId, userId, commentDto);
    }
}
