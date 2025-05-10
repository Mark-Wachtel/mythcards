package client;

import common.ChatMessageDTO;
import javafx.geometry.Insets;
import javafx.scene.control.ListCell;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;

import java.time.format.DateTimeFormatter;

/**
 * Sehr einfache Zellen­darstellung:
 * [HH:mm] <User> : Text
 * (später durch Chat-Blase ersetzen)
 */
public class ChatMessageCell extends ListCell<ChatMessageDTO> {

    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("HH:mm");

    private final Label lbl = new Label();

    public ChatMessageCell() {
        lbl.setWrapText(true);
        lbl.setPadding(new Insets(4));
        lbl.setTextAlignment(TextAlignment.LEFT);
        setGraphic(new HBox(lbl));
    }

    @Override
    protected void updateItem(ChatMessageDTO msg, boolean empty) {
        super.updateItem(msg, empty);

        if (empty || msg == null) {
            lbl.setText(null);
            setGraphic(null);
        } else {
            String t = TS.format(msg.timestamp()) +
                       "  <" + msg.senderId().toString().substring(0, 8) + ">  "
                       + msg.text();
            lbl.setText(t);
            setGraphic(lbl);
        }
    }
}