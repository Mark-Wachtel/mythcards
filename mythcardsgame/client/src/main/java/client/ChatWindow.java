package client;

import client.ChatSocket;
import common.ChatMessageDTO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Einzelnes Chat-Fenster (Popup) mit History-Pagination.
 */
public class ChatWindow extends BorderPane {

    /* --- Konstanten ------------------------------------------------------ */
    private static final int PAGE_SIZE = 25;
    private static final Duration SCROLL_TOLERANCE = Duration.millis(50);

    /* --- Abhängigkeiten --------------------------------------------------- */
    private final UUID conversationId;
    private final ChatSocket socket;
    private final String accessToken = ClientMain.getAccessToken();

    /* --- UI & State ------------------------------------------------------- */
    private final ListView<ChatMessageDTO> list = new ListView<>();
    private final ObservableList<ChatMessageDTO> items =
            FXCollections.observableArrayList();

    private Instant oldest = Instant.now();
    private final AtomicBoolean loading = new AtomicBoolean(false);

    /* Wird per lookup gefunden – erst nach dem ersten layout pass existent */
    private ScrollBar vBar;

    /* --- Konstruktor ------------------------------------------------------ */
    public ChatWindow(UUID conversationId, ChatSocket socket) {
        this.conversationId = conversationId;
        this.socket = socket;

        list.setItems(items);
        list.setCellFactory(v -> new ChatMessageCell());
        setCenter(list);

        /* REST-History + Scroll-Handling */
        loadHistory();
        Platform.runLater(this::initScrollHandling);

        /* WebSocket-Callback  */
        socket.subscribeToConversation(conversationId, this::addMessage);
    }

    /* ===================================================================== *
     *  Scroll-Handling                                                      *
     * ===================================================================== */
    private void initScrollHandling() {
        vBar = findVerticalBar(list);
        if (vBar == null) return;        // sollte nie passieren

        /* Listener für Hoch/Runter-Scrollen */
        vBar.valueProperty().addListener((obs, oldVal, newVal) -> {
            double val = newVal.doubleValue();

            /* ganz unten?  -> readAck */
            if (almostEquals(val, 1)) {
                ackRead();
            }
            /* ganz oben?   -> ältere Seite nachladen */
            else if (almostEquals(val, 0) && !loading.get()) {
                loadHistory();
            }
        });

        /* Listener für neue Items: falls unten, autoscrollen */
        items.addListener((ListChangeListener<ChatMessageDTO>) c -> {
            if (isAtBottom()) {
                list.scrollTo(items.size() - 1);
            }
        });
        
        Platform.runLater(() -> vBar.setValue(1));
    }

    private boolean isAtBottom() {
        return vBar == null || almostEquals(vBar.getValue(), 1);
    }

    private static boolean almostEquals(double a, double b) {
        return Math.abs(a - b) < 1e-3;
    }

    private ScrollBar findVerticalBar(Control control) {
        for (Node n : control.lookupAll(".scroll-bar")) {
            if (n instanceof ScrollBar sb && sb.getOrientation() == Orientation.VERTICAL) {
                return sb;
            }
        }
        return null;
    }

    /* ===================================================================== *
     *  REST-Calls (History + ReadAck)                                       *
     * ===================================================================== */
    private void loadHistory() {
        if (loading.getAndSet(true)) return;           // bereits im Gang

        String url = String.format(
                "http://localhost:8080/api/chat/history?convId=%s&before=%s&size=%d",
                conversationId, oldest, PAGE_SIZE);

        new HistoryService(url).start();
    }

    private class HistoryService extends Service<ChatMessageDTO[]> {
        private final String url;
        HistoryService(String url) { this.url = url; }

        @Override
        protected Task<ChatMessageDTO[]> createTask() {
            return new Task<>() {
                @Override protected ChatMessageDTO[] call() throws Exception {
                    RestTemplate rest = new RestTemplate();
                    HttpHeaders headers = new HttpHeaders();
                    headers.setBearerAuth(accessToken);
                    HttpEntity<Void> entity = new HttpEntity<>(headers);

                    ResponseEntity<ChatMessageDTO[]> resp =
                            rest.exchange(url, HttpMethod.GET, entity,
                                          ChatMessageDTO[].class);
                    return resp.getBody();
                }
            };
        }

        @Override
        protected void succeeded() {
            ChatMessageDTO[] arr = getValue();
            if (arr != null && arr.length > 0) {

                // chronologisch sortieren (älteste zuerst)
                Arrays.sort(arr, Comparator.comparing(ChatMessageDTO::timestamp));

                // neues „ältestes“ Zeit-Mark behalten
                oldest = arr[0].timestamp();

                /* Scroll-Position merken */
                ScrollBar vBar = findVerticalBar(list);
                double pos = vBar != null ? vBar.getValue() : 0.0;

                /* Elemente einfügen */
                items.addAll(0, Arrays.asList(arr));

                /* Scroll-Position wiederherstellen */
                if (vBar != null) {
                    vBar.setValue(pos);
                }
            }
            loading.set(false);
        }

        @Override
        protected void failed() {
            getException().printStackTrace();
            loading.set(false);
        }
    }

    /* --------------------------------------------------------------------- */
    private void ackRead() {
        String url = "http://localhost:8080/api/chat/readAck?convId=" + conversationId;
        new Thread(() -> {
            try {
                RestTemplate rest = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(accessToken);
                rest.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Void.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "readAck-" + conversationId).start();
    }

    /* ===================================================================== *
     *  Öffentliche API                                                      *
     * ===================================================================== */
    public void addMessage(ChatMessageDTO msg) {
        Platform.runLater(() -> items.add(msg));
    }
}