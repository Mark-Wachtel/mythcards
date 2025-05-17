package server.chat;

import common.ChatMessageDTO;

public class ChatMapper {
    public static ChatMessageDTO toDTO(ChatMessage msg) {
        return new ChatMessageDTO(
            msg.getConversation().getId(),
            msg.getId(),
            msg.getSenderId(),
            msg.getCreatedAt(),
            msg.getText(),
            msg.isImportant()
        );
    }
}