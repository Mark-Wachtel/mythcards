package client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import common.BadgeUpdateDTO;
import common.ChatMessageDTO;
import common.FriendDTO;
import common.PresenceDTO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class ChatSocket {

    private final ObservableMap<UUID, Integer> unreadMap = FXCollections.observableHashMap();
    private final ObservableSet<UUID> onlineSet = FXCollections.observableSet();
    private final ObservableList<FriendDTO> friends = FXCollections.observableArrayList();

    public ObservableMap<UUID, Integer> getUnreadMap() { return unreadMap; }
    public ObservableSet<UUID> getOnlineSet() { return onlineSet; }
    public ObservableList<FriendDTO> getFriendObservableList() { return friends; }

    private final WebSocketStompClient stomp;
    private StompSession session;
    private final ObjectMapper mapper = new ObjectMapper();
    private final URI endpoint;
    private final String jwt;
    private final CompletableFuture<Void> connectionReady = new CompletableFuture<>();

    public ChatSocket(URI endpoint, String jwt) {
        this.endpoint = endpoint;
        this.jwt = jwt;

        mapper.registerModule(new JavaTimeModule());

        this.stomp = new WebSocketStompClient(new StandardWebSocketClient());
        this.stomp.setMessageConverter(jacksonConverter());

        System.out.println("[DEBUG] ChatSocket initialisiert mit Endpoint: " + endpoint);
        System.out.println("[DEBUG] JWT Token (gekürzt): " + (jwt != null ? jwt.substring(0, Math.min(20, jwt.length())) + "..." : "<null>"));
    }

    public void connect() {
        System.out.println("[DEBUG] Versuche WebSocket-Verbindung aufzubauen...");

        WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();
        handshakeHeaders.add("Authorization", "Bearer " + jwt);   // ① HIER!

        StompHeaders connectHeaders = new StompHeaders();         // ② Kann leer bleiben
        // connectHeaders.add("Authorization", "Bearer " + jwt);  // nicht mehr nötig

        stomp.connect(
                endpoint.toString(),
                handshakeHeaders,     // <-- Header mit JWT
                connectHeaders,
                new StompSessionHandlerAdapter() {
                    @Override
                    public void afterConnected(StompSession s, StompHeaders h) {
                        System.out.println("[DEBUG] WebSocket verbunden! Session ID: " + s.getSessionId());
                        session = s;
                        subscribeUserQueues();
                        connectionReady.complete(null);
                    }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                System.err.println("[ERROR] Transportfehler beim WebSocket: " + exception.getMessage());
                exception.printStackTrace();
                connectionReady.completeExceptionally(exception);
            }
        });
    }

    private void subscribeUserQueues() {
        System.out.println("[DEBUG] Subscribing zu /user/queue/badge & /user/queue/presence");
        session.subscribe("/user/queue/badge", new JsonFrameHandler<>(BadgeUpdateDTO.class, this::handleBadge));
        session.subscribe("/user/queue/presence", new JsonFrameHandler<>(PresenceDTO.class, this::handlePresence));
    }

    public void send(String destination, Object payload) {
        if (session != null && session.isConnected()) {
            System.out.println("[DEBUG] Sende Nachricht an " + destination + ": " + payload);
            session.send(destination, payload);
        } else {
            System.err.println("[WARN] Konnte nicht senden, da Session nicht verbunden ist: " + destination);
        }
    }

    private void handleBadge(BadgeUpdateDTO dto) {
        System.out.println("[DEBUG] Badge-Update empfangen: " + dto);
        Platform.runLater(() -> unreadMap.put(dto.conversationId(), dto.unreadCount()));
    }

    private void handlePresence(PresenceDTO dto) {
        System.out.println("[DEBUG] Presence-Update empfangen: " + dto);
        Platform.runLater(() -> {
            if ("ONLINE".equalsIgnoreCase(dto.status())) {
                onlineSet.add(dto.userId());
            } else {
                onlineSet.remove(dto.userId());
            }
        });
    }

    private MessageConverter jacksonConverter() {
        MappingJackson2MessageConverter conv = new MappingJackson2MessageConverter();
        conv.setObjectMapper(mapper);
        return conv;
    }

    private class JsonFrameHandler<T> implements StompFrameHandler {
        private final Class<T> type;
        private final Consumer<T> consumer;

        JsonFrameHandler(Class<T> type, Consumer<T> consumer) {
            this.type = type;
            this.consumer = consumer;
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return type;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void handleFrame(StompHeaders headers, Object payload) {
            System.out.println("[DEBUG] Nachricht empfangen: " + payload);
            consumer.accept((T) payload);
        }
    }

    public void subscribeToConversation(UUID convId, Consumer<ChatMessageDTO> onMessage) {
        System.out.println("[DEBUG] SubscribeToConversation aufgerufen für: " + convId);

        connectionReady.whenComplete((v, ex) -> {
            if (ex != null || session == null || !session.isConnected()) {
                System.err.println("[ERROR] WebSocket nicht verbunden – Subscribe abgebrochen: " + convId);
                if (ex != null) ex.printStackTrace();
                throw new IllegalStateException("WebSocket noch nicht verbunden", ex);
            }

            String topic = "/topic/conversation/" + convId;
            System.out.println("[DEBUG] Subscribing zu: " + topic);
            session.subscribe(topic, new JsonFrameHandler<>(ChatMessageDTO.class, onMessage));
        });
    }
}
