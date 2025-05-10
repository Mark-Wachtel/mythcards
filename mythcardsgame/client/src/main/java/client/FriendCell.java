package client;

import common.FriendDTO;
import javafx.beans.binding.Bindings;
import javafx.collections.*;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.Optional;
import java.util.UUID;

public class FriendCell extends ListCell<FriendDTO> {

    private final ObservableMap<UUID,Integer> unread;
    private final ObservableSet<UUID> online;

    private final Label name  = new Label();
    private final Label badge = new Label();
    private final Circle dot  = new Circle(5);

    public FriendCell(ObservableMap<UUID,Integer> unread,
                      ObservableSet<UUID> online) {
        this.unread = unread;
        this.online = online;

        badge.getStyleClass().add("badge");
        badge.setMinSize(18,18);
        badge.setAlignment(Pos.CENTER);
        badge.setVisible(false);

        HBox box = new HBox(8, dot, name, badge);
        box.setAlignment(Pos.CENTER_LEFT);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        setGraphic(box);
    }

    @Override
    protected void updateItem(FriendDTO fr, boolean empty) {
        super.updateItem(fr, empty);
        if (empty || fr == null) {
            setGraphic(null);
            return;
        }
        name.setText(fr.username());

        /* Präsenz-Binding (userId) */
        dot.setFill(online.contains(fr.userId()) ? Color.LIMEGREEN : Color.GRAY);
        online.addListener((SetChangeListener<? super UUID>) c ->
            dot.setFill(online.contains(fr.userId()) ? Color.LIMEGREEN : Color.GRAY)
        );

        /* Badge-Binding (conversationId) */
        badge.textProperty().bind(
            Bindings.createStringBinding(
                () -> Optional.ofNullable(unread.get(fr.conversationId()))
                              .filter(c -> c > 0)
                              .map(Object::toString)
                              .orElse(""),
                unread
            )
        );
        badge.visibleProperty().bind(
            Bindings.createBooleanBinding(
                () -> unread.getOrDefault(fr.conversationId(),0) > 0,
                unread
            )
        );
        setGraphic(getGraphic());
    }
}