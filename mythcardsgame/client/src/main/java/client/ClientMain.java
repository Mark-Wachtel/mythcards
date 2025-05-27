package client;


import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.FXGLScene;
import com.almasb.fxgl.app.scene.IntroScene;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.app.scene.StartupScene;
import com.almasb.fxgl.dsl.FXGL;
import common.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.prefs.Preferences;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class ClientMain extends GameApplication {

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Preferences prefs = Preferences.userNodeForPackage(ClientMain.class);
    private final FriendServiceClient friendClient = new FriendServiceClient();

    private static String accessToken;
    private UUID currentUserId;
    private String currentUsername;
    private boolean loggedIn;

    private ChatSocket chatSocket;
    private ListView<FriendDTO> combinedListView;

    private BorderPane root;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Myth Cards Client 0.0");
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.getCSSList().add("styles/chat.css");
        settings.setMainMenuEnabled(false);
        settings.setIntroEnabled(true);

        settings.setSceneFactory(new SceneFactory() {
            @Override
            public StartupScene newStartup(int width, int height) {
                return new CustomStartupScene(width, height);
            }
            
            @Override
            public IntroScene newIntro() {
                return new MyIntroScene();
            }
        });  
    }
    
    public class MyIntroScene extends IntroScene {
        public MyIntroScene() {
            super();

            // Hintergrundbild über FXGL laden
            var bg = FXGL.texture("bg/loading_screen.png");
            bg.setFitWidth(FXGL.getSettings().getWidth());
            bg.setFitHeight(FXGL.getSettings().getHeight());

            getContentRoot().getChildren().add(bg);

            // Optional: Loading-Text, Animation, etc.
            // Text loading = new Text("Loading ...");
            // loading.setFill(Color.WHITE);
            // loading.setFont(Font.font(30));
            // getContentRoot().getChildren().add(loading);
        }

		@Override
		public void startIntro() {
			// TODO Auto-generated method stub
			finishIntro();
		}
    }
    
    public static class CustomStartupScene extends StartupScene {

        public CustomStartupScene(int appWidth, int appHeight) {
            super(appWidth, appHeight);

            // Nutze hier kein FXGL.texture(...) – FXGL ist noch nicht initialisiert!
            javafx.scene.image.Image bgImage = new javafx.scene.image.Image(
                getClass().getResource("/assets/textures/bg/loading_screen.png").toExternalForm()
            );

            javafx.scene.image.ImageView bgView = new javafx.scene.image.ImageView(bgImage);
            bgView.setFitWidth(appWidth);
            bgView.setFitHeight(appHeight);

            getContentRoot().getChildren().add(bgView);
        }
    }

    @Override
    protected void initGame() {
        String savedUser = prefs.get("username", null);
        String savedPass = prefs.get("password", null);
        if (savedUser != null && savedPass != null) {
            tryLogin(savedUser, savedPass, new Text(), true);
        } else {
            showLoginScene();
        }
    }

    private void showLoginScene() {
        TextField userField = new TextField();
        userField.setPromptText("Username");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");

        CheckBox rememberCb = new CheckBox("Remember me");

        Button loginBtn = new Button("Login");
        Button registerBtn = new Button("Register");

        Text statusText = new Text();

        loginBtn.setOnAction(e -> tryLogin(userField.getText(), passField.getText(), statusText, rememberCb.isSelected()));
        registerBtn.setOnAction(e -> tryRegister(userField.getText(), passField.getText(), statusText, rememberCb.isSelected()));

        VBox box = new VBox(10, userField, passField, rememberCb, loginBtn, registerBtn, statusText);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));

        // ⚡ FXGL-Texture verwenden (bereits initialisiert)
        var bgView = FXGL.texture("bg/loading_screen.png");
        bgView.setFitWidth(1280);
        bgView.setFitHeight(720);

        StackPane root = new StackPane(bgView, box);
        root.setPrefSize(1280, 720);

        FXGL.getGameScene().clearUINodes();
        FXGL.addUINode(root);
    }

    private void tryLogin(String username, String password, Text statusText, boolean remember) {
        try {
            String body = MAPPER.writeValueAsString(new LoginRequest(username, password));
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(resp -> Platform.runLater(() -> {
                        if (resp.statusCode() == 200) {
                            handleLoginSuccess(resp.body(), remember);
                        } else {
                            statusText.setText("Login fehlgeschlagen: " + resp.statusCode());
                        }
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> statusText.setText("Fehler beim Senden der Anfrage"));
                        return null;
                    });
        } catch (Exception ex) {
            Platform.runLater(() -> statusText.setText("Fehler beim Erstellen der Anfrage"));
        }
    }

    private void handleLoginSuccess(String jsonBody, boolean remember) {
        try {
            AuthResponse auth = MAPPER.readValue(jsonBody, AuthResponse.class);
            accessToken = auth.accessToken();
            currentUserId = UUID.fromString(auth.userId());
            loggedIn = true;

            friendClient.setAccessToken(accessToken);
            chatSocket = new ChatSocket(new URI("ws://localhost:8080/ws"), accessToken);

            showMainMenuScene();

            CompletableFuture.runAsync(() -> {
                try {
                    chatSocket.connect();
                    System.out.println("Connected with chatSocket: " + chatSocket.toString());
                } catch (Exception e) {
                    Platform.runLater(() -> showAlert("WebSocket-Fehler", e.getMessage()));
                }
            });

            if (remember) {
                prefs.put("username", currentUsername);
                prefs.put("password", ""); // Achtung: im echten Projekt verschlüsseln
            }
        } catch (Exception e) {
            showAlert("Verarbeitungsfehler", "Antwort konnte nicht gelesen werden");
        }
    }

    private void tryRegister(String username, String password, Text statusText, boolean remember) {
        try {
            String body = MAPPER.writeValueAsString(new RegisterRequest(username, password));
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/auth/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(resp -> Platform.runLater(() -> {
                        if (resp.statusCode() == 200) {
                            tryLogin(username, password, statusText, remember);
                        } else {
                            statusText.setText("Registrierung fehlgeschlagen: " + resp.statusCode());
                        }
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> statusText.setText("Fehler beim Senden der Anfrage"));
                        return null;
                    });
        } catch (Exception ex) {
            Platform.runLater(() -> statusText.setText("Fehler beim Erstellen der Anfrage"));
        }
    }

    private void showMainMenuScene() {
        root = new BorderPane();

        // Top Navigation
        Button btnHome = new Button("Startseite");
        Button btnProfile = new Button("Profil");
        Button btnDecks = new Button("Decks");
        Button btnPlay = new Button("Spielen");
        Button btnSettings = new Button("Einstellungen");
        Button logoutBtn = new Button("Logout");

        logoutBtn.setOnAction(e -> logout());
        btnHome.setOnAction(e -> root.setCenter(new StackPane(new Label("Willkommen im Hauptmenü!"))));
        btnProfile.setOnAction(e -> root.setCenter(createProfileView()));

        HBox topBar = new HBox(10, btnHome, btnProfile, btnDecks, btnPlay, btnSettings, logoutBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER);
        root.setTop(topBar);

        // Standardansicht
        root.setCenter(new StackPane(new Label("Willkommen im Hauptmenü!")));

        // Freundesliste rechts immer sichtbar
        root.setRight(createFriendsView());

        FXGL.getGameScene().clearUINodes();
        FXGL.addUINode(root);

        friendClient.fetchFriends(currentUserId)
                .thenAccept(list -> Platform.runLater(() -> chatSocket.getFriendObservableList().setAll(list)));
    }


    private Node createFriendsView() {
        VBox wrapper = new VBox(10);

        Button addFriendBtn = new Button("+ Freund hinzufügen");
        addFriendBtn.setMaxWidth(Double.MAX_VALUE);
        addFriendBtn.setOnAction(e -> showAddFriendDialog());

        wrapper.getChildren().add(addFriendBtn);

        // PENDING REQUESTS anzeigen
        friendClient.fetchPendingRequests(currentUserId, accessToken)
                .thenAccept(requests -> Platform.runLater(() -> {
                	System.out.println("Empfangene Freundschaftsanfragen: " + requests.size());
                    for (PendingRequestDTO req : requests) {
                        Label nameLabel = new Label(req.senderUsername());
                        Button acceptBtn = new Button("Annehmen");
                        Button declineBtn = new Button("Ablehnen");

                        acceptBtn.setOnAction(a -> {
                            friendClient.acceptFriendRequest(req.requestId())
                                    .thenAccept(success -> {
                                        if (success) {
                                        	reloadFriendUI();
                                            Platform.runLater(() -> showAlert("Angenommen", req.senderUsername() + " wurde hinzugefügt."));
                                        }
                                    });
                        });

                        declineBtn.setOnAction(d -> {
                            friendClient.declineFriendRequest(req.requestId())
                                    .thenAccept(success -> {
                                        if (success) {
                                        	reloadFriendUI();
                                            Platform.runLater(() -> showAlert("Abgelehnt", req.senderUsername() + " wurde abgelehnt."));
                                        }
                                    });
                        });

                        HBox requestBox = new HBox(5, nameLabel, acceptBtn, declineBtn);
                        requestBox.setAlignment(Pos.CENTER_LEFT);
                        wrapper.getChildren().add(requestBox);
                    }
                }));

        combinedListView = new ListView<>(chatSocket.getFriendObservableList());
        combinedListView.setCellFactory(list -> new FriendCell(chatSocket.getUnreadMap(), chatSocket.getOnlineSet()));
        combinedListView.setOnMouseClicked(evt -> {
            if (evt.getButton() == MouseButton.PRIMARY && evt.getClickCount() == 1) {
                FriendDTO sel = combinedListView.getSelectionModel().getSelectedItem();
                if (sel != null) openChat(sel);
            }
        });

        VBox.setVgrow(combinedListView, Priority.ALWAYS);
        wrapper.getChildren().add(combinedListView);
        wrapper.setPadding(new Insets(10));
        wrapper.setPrefWidth(250);
        System.out.println("check");
        chatSocket.loadUnreadSummary();
        System.out.println(chatSocket.getUnreadMap().values().toString());
        return wrapper;
    }

    private void reloadFriendUI() {
        // Liste neuladen + UI neu setzen
        friendClient.fetchFriends(currentUserId)
            .thenAccept(list -> Platform.runLater(() -> {
                chatSocket.getFriendObservableList().setAll(list);
                root.setRight(createFriendsView());
            }));
        chatSocket.loadUnreadSummary();
        System.out.println("reloaded friend ui...");
    }

    private void showAddFriendDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Freund hinzufügen");
        dialog.setHeaderText("UUID eingeben:");
        dialog.setContentText("UUID:");
        dialog.showAndWait().ifPresent(uuidString -> {
            try {
                UUID receiverId = UUID.fromString(uuidString.trim());
                friendClient.sendFriendRequest(currentUserId, receiverId)
                    .thenAccept(success -> Platform.runLater(() -> {
                        if (success) showAlert("Erfolg", "Anfrage gesendet!");
                        else showAlert("Fehler", "Anfrage fehlgeschlagen.");
                    }));
            } catch (IllegalArgumentException ex) {
                showAlert("Fehler", "Ungültige UUID eingegeben!");
            }
        });
    }


    private void openChat(FriendDTO friend) {
        ensureConversation(friend.userId(), friend.conversationId())
                .thenAccept(convId -> Platform.runLater(() -> {
                    ChatWindow pane = new ChatWindow(convId, chatSocket, currentUserId);
                    Stage stage = new Stage();
                    stage.setTitle("Chat mit " + friend.username());
                    stage.setScene(new Scene(pane, 400, 600));

                  

                    HBox inputBar = createInputBar(convId, pane);
                    pane.setBottom(inputBar);
                    stage.show();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> showAlert("Fehler", ex.getMessage()));
                    return null;
                });
    }

    private CompletableFuture<UUID> ensureConversation(UUID friendId, UUID convId) {
        if (convId != null) return CompletableFuture.completedFuture(convId);
        CreateConversationDTO dto = new CreateConversationDTO(false, null, List.of(currentUserId, friendId));
        try {
            String payload = MAPPER.writeValueAsString(dto);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/conversations"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();
            return httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                    .thenApply(resp -> {
                        if (resp.statusCode() != 200)
                            throw new RuntimeException("Erstellung fehlgeschlagen: " + resp.statusCode());
                        try {
                            GroupCreatedDTO created = MAPPER.readValue(resp.body(), GroupCreatedDTO.class);
                            return created.conversationId();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (Exception e) {
            CompletableFuture<UUID> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }
    
    private Node createProfileView() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));

        Label headline = new Label("Dein Profil");
        Label nameLabel = new Label("Benutzername: " + currentUsername);
        Label idLabel = new Label("Deine UUID:");
        TextField uuidField = new TextField(currentUserId.toString());
        uuidField.setEditable(false);
        uuidField.setPrefWidth(300);

        Button copyBtn = new Button("Kopieren");
        copyBtn.setOnAction(e -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(uuidField.getText());
            Clipboard.getSystemClipboard().setContent(content);
            showAlert("Kopiert", "UUID wurde in die Zwischenablage kopiert.");
        });

        HBox uuidBox = new HBox(5, uuidField, copyBtn);
        box.getChildren().addAll(headline, nameLabel, idLabel, uuidBox);
        return box;
    }


    private HBox createInputBar(UUID convId, ChatWindow pane) {
        TextField input = new TextField();
        input.setPromptText("Nachricht eingeben...");
        Button sendBtn = new Button("Send");
        sendBtn.setOnAction(e -> {
            String text = input.getText();
            if (!text.isBlank()) {
                ChatMessageDTO dto = new ChatMessageDTO(convId, UUID.randomUUID(), currentUserId, Instant.now(), text, false);
                chatSocket.send("/app/chat.send", dto);
                input.clear();
            }
        });
        HBox box = new HBox(5, input, sendBtn);
        box.setPadding(new Insets(5));
        return box;
    }

    private void logout() {
        loggedIn = false;
        accessToken = null;
        currentUserId = null;
        friendClient.setAccessToken(null);
        prefs.remove("username");
        prefs.remove("password");
        chatSocket.disconnect();
        showLoginScene();
    }

    private void showAlert(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        a.setHeaderText(title);
        a.showAndWait();
    }
    
    public static String getAccessToken()
    {
			return accessToken;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
