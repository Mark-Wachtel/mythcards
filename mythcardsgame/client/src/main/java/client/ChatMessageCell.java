package client;

import common.ChatMessageDTO;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Einfache Zellen‐Darstellung:
 *   [HH:mm] <User> : Text
 * (kann später durch Chat‐Blasen ersetzt werden)
 */
public class ChatMessageCell extends ListCell<ChatMessageDTO> {

    /**
     * Formatter für die Uhrzeit.
     *  - "HH:mm" für 24‑h‑Zeit
     *  - {@link DateTimeFormatter#withZone(ZoneId)} macht das
     *    {@code Instant → ZonedDateTime} Mapping automatisch
     */
    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("HH:mm")
                              .withZone(ZoneId.systemDefault());

    private final Label lbl = new Label();
    private final HBox  root = new HBox(lbl);

    public ChatMessageCell() {
        lbl.setWrapText(true);
        lbl.setPadding(new Insets(4));
        lbl.setTextAlignment(TextAlignment.LEFT);
        setGraphic(root);
    }

    @Override
    protected void updateItem(ChatMessageDTO msg, boolean empty) {
        super.updateItem(msg, empty);

        if (empty || msg == null) {
            lbl.setText("");
            setGraphic(null);
            return;
        }

        // Timestamp sicher formatieren (Instant ➜ ZonedDateTime erfolgt im Formatter)
        String time   = TS.format(msg.timestamp());
        String sender = msg.senderId().toString().substring(0, 8);

        lbl.setText(time + "  <" + sender + ">  " + msg.text());
        setGraphic(root);
    }
}