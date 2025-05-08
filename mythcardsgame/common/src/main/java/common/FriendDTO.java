package common;

import java.util.UUID;

import common.FriendDTO.Status;

public record FriendDTO(UUID userId, String username, Status status) {
    public enum Status { online, offline, ingame }
}
