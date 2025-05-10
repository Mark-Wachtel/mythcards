package client;

import client.ChatSocket;
import common.ChatMessageDTO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.springframework.web.client.RestTemplate;
import java.util.concurrent.atomic.AtomicBoolean; 

import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Einzelnes Chat-Fenster (Popup) mit History-Pagination.
 */
public class ChatWindow extends BorderPane {

    private final UUID conversationId;
    private final RestTemplate rest = new RestTemplate();
    private final ChatSocket socket;

    private final ListView<ChatMessageDTO> list = new ListView<>();
    private final ObservableList<ChatMessageDTO> items = FXCollections.observableArrayList();

    private Instant oldest = Instant.now();
    private final AtomicBoolean loading = new AtomicBoolean(false);

    public ChatWindow(UUID conversationId, ChatSocket socket) {
        this.conversationId = conversationId;
        this.socket = socket;

        list.setItems(items);
        list.setCellFactory(v -> new ChatMessageCell()); // TODO
        setCenter(list);

        loadHistory();
        Platform.runLater(this::initScrollHandling); // erst nach Skin ready
    }

    /* ---------------------------------------------------- */
    private void initScrollHandling() {
        ScrollBar vBar = findVerticalBar(list);
        if (vBar == null) return;      // fallback – kein Pagination

        // Wenn ganz unten → ReadAck, wenn ganz oben → weitere History
        vBar.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() == vBar.getMax()) {
                ackRead();
            } else if (newVal.doubleValue() == vBar.getMin() && !loading.get()) {
                loadHistory();
            }
        });

        // Startposition unten
        vBar.setValue(vBar.getMax());
    }

    private ScrollBar findVerticalBar(Control control) {
        Set<Node> nodes = control.lookupAll(".scroll-bar");
        for (Node n : nodes) {
            if (n instanceof ScrollBar sb && sb.getOrientation() == Orientation.VERTICAL) {
                return sb;
            }
        }
        return null;
    }

    /* ---------------- REST-Calls ------------------------ */
    private void loadHistory() {
        loading.set(true);
        String url = String.format("http://localhost:8080/api/chat/history?convId=%s&before=%s&size=25",
                conversationId, oldest);

        new Thread(() -> {
            ChatMessageDTO[] arr = rest.getForObject(url, ChatMessageDTO[].class);
            if (arr != null && arr.length > 0) {
                Arrays.sort(arr, Comparator.comparing(ChatMessageDTO::timestamp));
                oldest = arr[0].timestamp();
                Platform.runLater(() -> items.addAll(0, Arrays.asList(arr)));
            }
            loading.set(false);
        }).start();
    }

    private void ackRead() {
        String url = "http://localhost:8080/api/chat/readAck?convId=" + conversationId;
        new Thread(() -> rest.getForEntity(url, Void.class)).start();
    }
}
