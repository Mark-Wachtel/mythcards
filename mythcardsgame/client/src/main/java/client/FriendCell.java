package client;

    import common.FriendDTO;
    import javafx.beans.binding.Bindings;
    import javafx.collections.ObservableMap;
    import javafx.collections.ObservableSet;
    import javafx.collections.SetChangeListener;
    import javafx.geometry.Pos;
    import javafx.scene.control.ContentDisplay;
    import javafx.scene.control.Label;
    import javafx.scene.control.ListCell;
    import javafx.scene.layout.HBox;
    import javafx.scene.layout.Priority;
    import javafx.scene.layout.Region;
    import javafx.scene.paint.Color;
    import javafx.scene.shape.Circle;

    import java.util.Optional;
    import java.util.UUID;

    public class FriendCell extends ListCell<FriendDTO> {

        /* ------------------------------------------------------------
         * Datenquellen, die von außen hereingereicht werden
         * ------------------------------------------------------------ */
        private final ObservableMap<UUID, Integer> unread;   // convId → Anzahl
        private final ObservableSet<UUID> online;            // userIds online

        /* ------------------------------------------------------------
         * UI-Controls
         * ------------------------------------------------------------ */
        private final Label   name  = new Label();
        private final Label   badge = new Label();
        private final Circle  dot   = new Circle(5);
        private final HBox    box   = new HBox(8, dot, name, badge);

        /* ------------------------------------------------------------
         * Listener-Referenz, damit er bei Zell-Recycle entfernt werden kann
         * ------------------------------------------------------------ */
        private SetChangeListener<? super UUID> presenceListener;

        /* ------------------------------------------------------------
         * Konstruktor
         * ------------------------------------------------------------ */
        public FriendCell(ObservableMap<UUID, Integer> unread,
                          ObservableSet<UUID> online) {
            this.unread  = unread;
            this.online  = online;

            // ► Badge-Styling
            badge.getStyleClass().add("badge");
            badge.setMinSize(18, 18);
            badge.setAlignment(Pos.CENTER);
            badge.setVisible(false);

            // ► Name-Label
            name.setTextFill(Color.WHITE);
            name.setMinWidth(Region.USE_PREF_SIZE);
            HBox.setHgrow(name, Priority.ALWAYS);

            // ► Container
            box.setAlignment(Pos.CENTER_LEFT);

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

        /* ------------------------------------------------------------
         * Zelle (neu) befüllen
         * ------------------------------------------------------------ */
        @Override
        protected void updateItem(FriendDTO fr, boolean empty) {
            super.updateItem(fr, empty);

            /* ▸ Alten Presence-Listener abmelden, wenn vorhanden */
            if (presenceListener != null) {
                online.removeListener(presenceListener);
                presenceListener = null;
            }

            if (empty || fr == null) {
                setGraphic(null);
                return;
            }

            /* ▸ Name */
            name.setText(fr.username());

            /* ▸ Presence-Dot ----------------------------------- */
            presenceListener = change ->
                    dot.setFill(online.contains(fr.userId())
                            ? Color.LIMEGREEN
                            : Color.GRAY);

            online.addListener(presenceListener);
            // sofortigen Initial-Refresh auslösen
            presenceListener.onChanged(null);

            /* ▸ Badge-Binding ----------------------------------- */
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
                            () -> unread.getOrDefault(fr.conversationId(), 0) > 0,
                            unread
                    )
            );

            /* ▸ finale Grafik setzen */
            setGraphic(box);
        }
    }
