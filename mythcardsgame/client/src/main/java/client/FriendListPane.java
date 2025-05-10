package client;

import javafx.collections.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;

import java.util.UUID;

import common.FriendDTO;        

public class FriendListPane extends BorderPane {

    private final ListView<FriendDTO> view = new ListView<>();
    private final ObservableMap<UUID, Integer> unreadMap;      // convId -> count
    private final ObservableSet<UUID> onlineSet;               // userId online

    public FriendListPane(ChatSocket socket) {
        this.unreadMap = socket.getUnreadMap();
        this.onlineSet = socket.getOnlineSet();

        view.setCellFactory(v -> new FriendCell(unreadMap, onlineSet));
        view.setItems(socket.getFriendObservableList());

        setCenter(view);
        setPadding(new Insets(4));
    }
}