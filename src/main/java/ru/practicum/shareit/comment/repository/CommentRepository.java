package ru.practicum.shareit.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.comment.model.Comment;

import java.util.List;
import java.util.Set;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByItemId(Long itemId);

    @Query(value = "SELECT c FROM Comment c " +
            "WHERE c.item.id IN (:itemId)")
    Set<Comment> findCommentsByItemId(Set<Long> itemId);
}
