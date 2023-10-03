package ru.practicum.shareit.item.repository;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {

    private final Map<Integer, Item> items;
    private int id = 1;

    private int incrementId() {
        return id++;
    }

    public List<Item> findAllItems() {
        return new ArrayList<>(items.values());
    }

    public Item createItem(Item item) {
        item.setId(incrementId());
        items.put(item.getId(), item);
        return item;
    }

    public Item updateItem(Item item) {
        if (!items.containsKey(item.getId())) {
            throw new NotFoundException("Объект не найден!");
        }
        items.put(item.getId(), item);
        return item;
    }

    public Item getItemById(int id) {
        if (!items.containsKey(id)) {
            throw new NotFoundException("Объект не найден!");
        }
        return items.get(id);
    }
}