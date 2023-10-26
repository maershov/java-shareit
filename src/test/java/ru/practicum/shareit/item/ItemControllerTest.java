package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exceptions.InvalidBookingException;
import ru.practicum.shareit.exceptions.ModelNotFoundException;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = ItemController.class)
@AutoConfigureWebMvc
public class ItemControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemService itemService;

    @Autowired
    private MockMvc mvc;
    private ItemDto itemDto;
    private CommentDto commentDto;

    @BeforeEach
    void init() {

        itemDto = ItemDto
                .builder()
                .id(1L)
                .name("Молоток")
                .description("молоток забивной")
                .available(true)
                .build();

        commentDto = CommentDto
                .builder()
                .id(1L)
                .text("новый комментарий")
                .build();
    }

    @Test
    void createItemExpectedStatus200() throws Exception {
        when(itemService.createItem(anyLong(), any()))
                .thenReturn(itemDto);
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(itemDto)));
    }

    @Test
    void createItemWithWrongUserIdExpectedStatus404() throws Exception {
        when(itemService.createItem(anyLong(), any()))
                .thenThrow(new ModelNotFoundException("Пользователь не найден!"));

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 10L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Пользователь не найден!")));
    }

    @Test
    void createItemWithoutNameExpectedStatus400() throws Exception {
        ItemDto noName = ItemDto
                .builder()
                .id(2L)
                .name(null)
                .description("вещь без имени")
                .available(true)
                .build();

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(noName))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createItemWithoutDescriptionExpectedStatus400() throws Exception {
        ItemDto noDesc = ItemDto
                .builder()
                .id(2L)
                .name("Топор")
                .description(null)
                .available(true)
                .build();

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(noDesc))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItemExpectedStatus200() throws Exception {
        itemDto.setDescription("новое описание");
        when(itemService.updateItem(anyLong(), any(), anyLong()))
                .thenReturn(itemDto);
        mvc.perform(patch("/items/{id}", 1)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())));
    }

    @Test
    void updateItemExpectedStatus404() throws Exception {
        when(itemService.updateItem(anyLong(), any(), anyLong()))
                .thenThrow(new ModelNotFoundException("Тестовое исключение"));

        mvc.perform(patch("/items/{id}", 5)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Тестовое исключение")));
    }

    @Test
    void getItemByUserIdExpectedStatus200() throws Exception {
        when(itemService.getItemByUserId(anyLong(), anyLong()))
                .thenReturn(itemDto);
        mvc.perform(get("/items/{id}", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())));
    }

    @Test
    void getItemWithWrongUserIdExpectedStatus404() throws Exception {
        when(itemService.getItemByUserId(anyLong(), anyLong()))
                .thenThrow(new ModelNotFoundException("Вещь не найдена!"));

        mvc.perform(get("/items/{id}", 20)
                        .header("X-Sharer-User-Id", 20L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Вещь не найдена!")));
    }

    @Test
    void getItemListByUserId() throws Exception {
        when(itemService.getItemListByUserId(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(itemDto));
        mvc.perform(get("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDto.getDescription())));
    }

    @Test
    void searchItemByText() throws Exception {
        when(itemService.search(anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(itemDto));
        mvc.perform(get("/items/search?text='name'")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(List.of(itemDto))));
    }

    @Test
    void createCommentExpectedStatus200() throws Exception {
        when(itemService.saveComment(anyLong(), anyLong(), any()))
                .thenReturn(commentDto);
        mvc.perform(post("/items/{id}/comment", 1)
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(commentDto)));
    }

    @Test
    void createCommentExpectedStatus404() throws Exception {
        when(itemService.saveComment(anyLong(), anyLong(), any()))
                .thenThrow(new ModelNotFoundException("Вещь не найдена!"));

        mvc.perform(post("/items/{id}/comment", 5)
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Вещь не найдена!")));
    }

    @Test
    void createCommentExpectedStatus400() throws Exception {
        when(itemService.saveComment(anyLong(), anyLong(), any()))
                .thenThrow(new InvalidBookingException("Пользователь не может оставить отзыв об этой вещи"));

        mvc.perform(post("/items/{id}/comment", 2)
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(UTF_8)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Пользователь не может оставить отзыв об этой вещи")));
    }
}
