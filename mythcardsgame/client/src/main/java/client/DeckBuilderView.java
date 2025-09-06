package client;

import com.almasb.fxgl.dsl.FXGL;
import common.CardData;
import common.DeckDetailDto;
import common.DeckDto;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DeckBuilderView extends BorderPane {
    
    // UI-Komponenten
    private final TextField deckNameField;
    private final Label deckCardCountLabel;
    private final FlowPane deckCardsPane;
    private final FlowPane availableCardsPane;
    private final ListView<DeckDto> deckListView;
    private final Button saveButton;
    private final Button deleteButton;
    private final Button newDeckButton;
    
    // Daten
    private final ObservableList<CardData> availableCards = FXCollections.observableArrayList();
    private final ObservableList<CardData> deckCards = FXCollections.observableArrayList();
    private final ObservableList<DeckDto> userDecks = FXCollections.observableArrayList();
    
    // Services
    private final DeckServiceClient deckService;
    private final UUID userId;
    
    // Aktuelles Deck
    private UUID currentDeckId;
    
    public DeckBuilderView(UUID userId, String accessToken) {
        this.userId = userId;
        this.deckService = new DeckServiceClient(accessToken);
        
        // Initialisiere UI-Komponenten
        this.deckNameField = new TextField();
        this.deckCardCountLabel = new Label("0 / 30 Karten");
        this.deckCardsPane = new FlowPane();
        this.availableCardsPane = new FlowPane();
        this.deckListView = new ListView<>(userDecks);
        this.saveButton = new Button("Deck speichern");
        this.deleteButton = new Button("Deck löschen");
        this.newDeckButton = new Button("Neues Deck");
        
        initializeUI();
        loadUserDecks();
        loadAvailableCards();
    }
    
    private void initializeUI() {
        // Hauptlayout
        setPadding(new Insets(10));
        setStyle("-fx-background-color: #1a1a1a;");
        
        // Linke Seite - Deck-Liste
        VBox leftPanel = createLeftPanel();
        
        // Mitte - Aktuelles Deck
        VBox centerPanel = createCenterPanel();
        
        // Rechte Seite - Verfügbare Karten
        ScrollPane rightPanel = createRightPanel();
        
        // Layout zusammenfügen
        setLeft(leftPanel);
        setCenter(centerPanel);
        setRight(rightPanel);
    }
    
    private VBox createLeftPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #2a2a2a; -fx-border-color: #444; -fx-border-radius: 5;");
        panel.setPrefWidth(250);
        
        Label title = new Label("Meine Decks");
        title.setFont(Font.font(18));
        title.setTextFill(Color.WHITE);
        
        // Deck-Liste konfigurieren
        deckListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(DeckDto deck, boolean empty) {
                super.updateItem(deck, empty);
                if (empty || deck == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(deck.getName());
                    setTextFill(Color.WHITE);
                    setStyle("-fx-background-color: #333;");
                }
            }
        });
        
        deckListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldDeck, newDeck) -> {
                if (newDeck != null) {
                    loadDeck(newDeck.getId());
                }
            }
        );
        
        // Buttons
        newDeckButton.setOnAction(e -> createNewDeck());
        newDeckButton.setMaxWidth(Double.MAX_VALUE);
        
        panel.getChildren().addAll(title, deckListView, newDeckButton);
        VBox.setVgrow(deckListView, Priority.ALWAYS);
        
        return panel;
    }
    
    private VBox createCenterPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #2a2a2a; -fx-border-color: #444; -fx-border-radius: 5;");
        panel.setPrefWidth(400);
        
        Label title = new Label("Deck Editor");
        title.setFont(Font.font(20));
        title.setTextFill(Color.WHITE);
        
        // Deck-Name
        HBox nameBox = new HBox(10);
        nameBox.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label("Deck-Name:");
        nameLabel.setTextFill(Color.WHITE);
        deckNameField.setPrefWidth(200);
        nameBox.getChildren().addAll(nameLabel, deckNameField);
        
        // Karten-Zähler
        deckCardCountLabel.setTextFill(Color.WHITE);
        deckCardCountLabel.setFont(Font.font(14));
        
        // Deck-Karten-Bereich
        ScrollPane deckScroll = new ScrollPane(deckCardsPane);
        deckScroll.setFitToWidth(true);
        deckScroll.setPrefHeight(400);
        deckScroll.setStyle("-fx-background: #1a1a1a; -fx-background-color: #1a1a1a;");
        
        deckCardsPane.setHgap(10);
        deckCardsPane.setVgap(10);
        deckCardsPane.setPadding(new Insets(10));
        deckCardsPane.setStyle("-fx-background-color: #1a1a1a;");
        
        // Drag-and-Drop für Deck-Bereich
        deckCardsPane.setOnDragOver(event -> {
            if (event.getGestureSource() != deckCardsPane && 
                event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });
        
        deckCardsPane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                String cardId = db.getString();
                addCardToDeck(cardId);
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        saveButton.setOnAction(e -> saveDeck());
        deleteButton.setOnAction(e -> deleteDeck());
        
        buttonBox.getChildren().addAll(saveButton, deleteButton);
        
        panel.getChildren().addAll(
            title, nameBox, deckCardCountLabel, deckScroll, buttonBox
        );
        
        VBox.setVgrow(deckScroll, Priority.ALWAYS);
        
        return panel;
    }
    
    private ScrollPane createRightPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #2a2a2a; -fx-border-color: #444; -fx-border-radius: 5;");
        panel.setPrefWidth(400);
        
        Label title = new Label("Verfügbare Karten");
        title.setFont(Font.font(18));
        title.setTextFill(Color.WHITE);
        
        // Such-Feld
        TextField searchField = new TextField();
        searchField.setPromptText("Karte suchen...");
        searchField.textProperty().addListener((obs, old, text) -> filterCards(text));
        
        // Karten-Bereich
        availableCardsPane.setHgap(10);
        availableCardsPane.setVgap(10);
        availableCardsPane.setPadding(new Insets(10));
        availableCardsPane.setStyle("-fx-background-color: #1a1a1a;");
        
        panel.getChildren().addAll(title, searchField, availableCardsPane);
        
        ScrollPane scrollPane = new ScrollPane(panel);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #2a2a2a; -fx-background-color: #2a2a2a;");
        
        return scrollPane;
    }
    
    private void loadUserDecks() {
        deckService.getUserDecks(userId)
            .thenAccept(decks -> Platform.runLater(() -> {
                userDecks.setAll(decks);
                if (!decks.isEmpty()) {
                    deckListView.getSelectionModel().selectFirst();
                }
            }))
            .exceptionally(ex -> {
                Platform.runLater(() -> showError("Fehler beim Laden der Decks", ex.getMessage()));
                return null;
            });
    }
    
    private void loadAvailableCards() {
        // Hier würde normalerweise ein API-Call erfolgen
        // Für diese Demo verwenden wir Beispieldaten
        CardServiceClient cardService = new CardServiceClient(deckService.getAccessToken());
        cardService.getAllCards()
            .thenAccept(cards -> Platform.runLater(() -> {
                availableCards.setAll(cards);
                updateAvailableCardsDisplay();
            }))
            .exceptionally(ex -> {
                Platform.runLater(() -> showError("Fehler beim Laden der Karten", ex.getMessage()));
                return null;
            });
    }
    
    private void updateAvailableCardsDisplay() {
        availableCardsPane.getChildren().clear();
        
        for (CardData card : availableCards) {
            CardView cardView = new CardView(card);
            cardView.setScaleX(0.5);
            cardView.setScaleY(0.5);
            
            // Drag-and-Drop
            cardView.setOnDragDetected(event -> {
                Dragboard db = cardView.startDragAndDrop(TransferMode.COPY);
                ClipboardContent content = new ClipboardContent();
                content.putString(card.id.toString());
                db.setContent(content);
                event.consume();
            });
            
            // Doppelklick zum Hinzufügen
            cardView.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    addCardToDeck(card.id.toString());
                }
            });
            
            availableCardsPane.getChildren().add(cardView);
        }
    }
    
    private void updateDeckDisplay() {
        deckCardsPane.getChildren().clear();
        
        for (CardData card : deckCards) {
            CardView cardView = new CardView(card);
            cardView.setScaleX(0.5);
            cardView.setScaleY(0.5);
            
            // Doppelklick zum Entfernen
            cardView.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    removeCardFromDeck(card);
                }
            });
            
            deckCardsPane.getChildren().add(cardView);
        }
        
        deckCardCountLabel.setText(deckCards.size() + " / 30 Karten");
    }
    
    private void addCardToDeck(String cardIdStr) {
        if (deckCards.size() >= 30) {
            showError("Deck voll", "Ein Deck kann maximal 30 Karten enthalten.");
            return;
        }
        
        UUID cardId = UUID.fromString(cardIdStr);
        availableCards.stream()
            .filter(c -> c.id.equals(cardId))
            .findFirst()
            .ifPresent(card -> {
                deckCards.add(card);
                updateDeckDisplay();
            });
    }
    
    private void removeCardFromDeck(CardData card) {
        deckCards.remove(card);
        updateDeckDisplay();
    }
    
    private void loadDeck(UUID deckId) {
        deckService.getDeckDetails(userId, deckId)
            .thenAccept(deck -> Platform.runLater(() -> {
                currentDeckId = deck.getId();
                deckNameField.setText(deck.getName());
                deckCards.setAll(deck.getCards());
                updateDeckDisplay();
            }))
            .exceptionally(ex -> {
                Platform.runLater(() -> showError("Fehler beim Laden des Decks", ex.getMessage()));
                return null;
            });
    }
    
    private void saveDeck() {
        String name = deckNameField.getText().trim();
        if (name.isEmpty()) {
            showError("Fehler", "Bitte geben Sie einen Deck-Namen ein.");
            return;
        }
        
        if (deckCards.isEmpty()) {
            showError("Fehler", "Das Deck ist leer.");
            return;
        }
        
        List<UUID> cardIds = deckCards.stream()
            .map(card -> card.id)
            .collect(Collectors.toList());
        
        if (currentDeckId == null) {
            // Neues Deck erstellen
            deckService.createDeck(userId, name, cardIds)
                .thenAccept(deck -> Platform.runLater(() -> {
                    currentDeckId = deck.getId();
                    loadUserDecks();
                    showInfo("Erfolg", "Deck wurde erstellt.");
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> showError("Fehler beim Erstellen", ex.getMessage()));
                    return null;
                });
        } else {
            // Bestehendes Deck aktualisieren
            deckService.updateDeck(userId, currentDeckId, name, cardIds)
                .thenAccept(deck -> Platform.runLater(() -> {
                    loadUserDecks();
                    showInfo("Erfolg", "Deck wurde aktualisiert.");
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> showError("Fehler beim Aktualisieren", ex.getMessage()));
                    return null;
                });
        }
    }
    
    private void deleteDeck() {
        if (currentDeckId == null) {
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Deck löschen");
        confirm.setHeaderText("Sind Sie sicher?");
        confirm.setContentText("Möchten Sie dieses Deck wirklich löschen?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deckService.deleteDeck(userId, currentDeckId)
                    .thenAccept(success -> Platform.runLater(() -> {
                        currentDeckId = null;
                        deckNameField.clear();
                        deckCards.clear();
                        updateDeckDisplay();
                        loadUserDecks();
                        showInfo("Erfolg", "Deck wurde gelöscht.");
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> showError("Fehler beim Löschen", ex.getMessage()));
                        return null;
                    });
            }
        });
    }
    
    private void createNewDeck() {
        currentDeckId = null;
        deckNameField.clear();
        deckCards.clear();
        updateDeckDisplay();
        deckListView.getSelectionModel().clearSelection();
    }
    
    private void filterCards(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            updateAvailableCardsDisplay();
            return;
        }
        
        String lower = searchText.toLowerCase();
        availableCardsPane.getChildren().clear();
        
        availableCards.stream()
            .filter(card -> card.title.toLowerCase().contains(lower))
            .forEach(card -> {
                CardView cardView = new CardView(card);
                cardView.setScaleX(0.5);
                cardView.setScaleY(0.5);
                
                cardView.setOnDragDetected(event -> {
                    Dragboard db = cardView.startDragAndDrop(TransferMode.COPY);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(card.id.toString());
                    db.setContent(content);
                    event.consume();
                });
                
                cardView.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        addCardToDeck(card.id.toString());
                    }
                });
                
                availableCardsPane.getChildren().add(cardView);
            });
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}