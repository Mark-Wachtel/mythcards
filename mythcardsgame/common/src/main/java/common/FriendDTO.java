package common;

import java.util.UUID;

import common.FriendDTO.Status;

public record FriendDTO(UUID userId, UUID conversationId, String username, Status status) {
	public enum Status { online, offline, ingame }
}
