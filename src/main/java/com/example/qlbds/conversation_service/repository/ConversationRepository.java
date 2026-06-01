package com.example.qlbds.conversation_service.repository;

import com.example.qlbds.conversation_service.entity.Conversation;
import com.example.qlbds.user_service.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c WHERE c.property.id = :propertyId AND ((c.userOne = :user1 AND c.userTwo = :user2) OR (c.userOne = :user2 AND c.userTwo = :user1))")
    Optional<Conversation> findByPropertyAndParticipants(@Param("propertyId") Long propertyId, @Param("user1") User user1, @Param("user2") User user2);

    @Query("SELECT c FROM Conversation c WHERE c.userOne = :user OR c.userTwo = :user ORDER BY c.lastMessageAt DESC NULLS LAST")
    Page<Conversation> findByUserOrderByLastMessageAtDesc(@Param("user") User user, Pageable pageable);
}
