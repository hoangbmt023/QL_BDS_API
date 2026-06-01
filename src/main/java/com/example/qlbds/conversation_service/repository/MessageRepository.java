package com.example.qlbds.conversation_service.repository;

import com.example.qlbds.conversation_service.entity.Conversation;
import com.example.qlbds.conversation_service.entity.Message;
import com.example.qlbds.user_service.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByConversationOrderByCreatedAtDesc(Conversation conversation, Pageable pageable);

    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.conversation = :conversation AND m.sender != :currentUser AND m.isRead = false")
    void markMessagesAsRead(@Param("conversation") Conversation conversation, @Param("currentUser") User currentUser);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation = :conversation AND m.sender != :currentUser AND m.isRead = false")
    Long countUnreadMessagesInConversation(@Param("conversation") Conversation conversation, @Param("currentUser") User currentUser);

    @Query("SELECT COUNT(m) FROM Message m WHERE (m.conversation.userOne = :currentUser OR m.conversation.userTwo = :currentUser) AND m.sender != :currentUser AND m.isRead = false")
    Long countTotalUnreadMessagesForUser(@Param("currentUser") User currentUser);
}
