package common;

public record PendingRequestDTO(
	    String requestId,
	    String senderId,
	    String senderUsername,
	    String expiresAt
	) {}