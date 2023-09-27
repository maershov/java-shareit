package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;
import java.util.List;

public interface ItemRepository {
    List<Item> findAllItems();

    Item createItem(Item item);

    Item updateItem(Item item);

    Item getItemById(int id);

}
