package client;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.BadgeUpdateDTO;
import common.FriendDTO;
import common.PresenceDTO;
import javafx.application.Platform;
import javafx.collections.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession.Subscription;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Single reusable WebSocket/STOMP client for chat, presence & badge updates.
 */
public class ChatSocket {

    /* ----------------- Public observable state ------------------ */
    private final ObservableMap<UUID, Integer> unreadMap = FXCollections.observableHashMap();
    private final ObservableSet<UUID> onlineSet        = FXCollections.observableSet();
    private final ObservableList<FriendDTO> friends    = FXCollections.observableArrayList();

    public ObservableMap<UUID,Integer> getUnreadMap() { return unreadMap; }
    public ObservableSet<UUID>  getOnlineSet()        { return onlineSet; }
    public ObservableList<FriendDTO> getFriendObservableList() { return friends; }

    /* ----------------- Internal ------------------ */
    private final WebSocketStompClient stomp;
    private StompSession session;
    private final ObjectMapper mapper = new ObjectMapper();
    private final URI endpoint;
    private final String jwt;

    public ChatSocket(URI endpoint, String jwt) {
        this.endpoint = endpoint;
        this.jwt      = jwt;
        this.stomp    = new WebSocketStompClient(new StandardWebSocketClient());
        stomp.setMessageConverter(jacksonConverter());
    }

    /**
     * Establishes the connection and subscribes to user queues.
     */
    public void connect() throws ExecutionException, InterruptedException {
        WebSocketHttpHeaders httpHeaders = new WebSocketHttpHeaders();
        httpHeaders.set("Authorization", "Bearer " + jwt);

        ListenableFuture<StompSession> fut = stomp
                .connect(endpoint.toString(), httpHeaders, new StompSessionHandlerAdapter(){});
        session = fut.get();
        subscribeUserQueues();
    }

    private void subscribeUserQueues() {
        session.subscribe("/user/queue/badge", new JsonFrameHandler<>(BadgeUpdateDTO.class, this::handleBadge));
        session.subscribe("/user/queue/presence", new JsonFrameHandler<>(PresenceDTO.class, this::handlePresence));
        // conversation topics werden dynamisch abonniert, wenn Fenster öffnet
    }

    /* ============ Send helper  ============ */
    public void send(String destination, Object payload) {
        if (session == null || !session.isConnected()) return;
        session.send(destination, payload);
    }

    /* ============ Frame Handlers ============ */
    private void handleBadge(BadgeUpdateDTO dto) {
        Platform.runLater(() -> unreadMap.put(dto.conversationId(), dto.unreadCount()));
    }

    private void handlePresence(PresenceDTO dto) {
        Platform.runLater(() -> {
            if ("ONLINE".equals(dto.status())) onlineSet.add(dto.userId());
            else                               onlineSet.remove(dto.userId());
        });
    }

    /* ============ Helper classes ============ */
    private <T> MessageConverter jacksonConverter() {
        MappingJackson2MessageConverter c = new MappingJackson2MessageConverter();
        c.setObjectMapper(mapper);
        return c;
    }

    private class JsonFrameHandler<T> implements StompFrameHandler {
        private final Class<T> type;
        private final java.util.function.Consumer<T> consumer;
        JsonFrameHandler(Class<T> type, java.util.function.Consumer<T> consumer) {
            this.type = type; this.consumer = consumer;
        }
        @Override public Type getPayloadType(StompHeaders headers) { return type; }
        @Override public void handleFrame(StompHeaders h, Object o) {
            consumer.accept(type.cast(o));
        }
    }
}