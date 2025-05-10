package client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.prefs.Preferences;
import java.util.concurrent.CompletableFuture;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import common.PendingRequestDTO;
import common.AuthResponse;
import common.LoginRequest;
import common.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import common.CardData;

public class ClientMain extends GameApplication {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Preferences prefs = Preferences.userNodeForPackage(ClientMain.class);
    private final FriendServiceClient friendClient = new FriendServiceClient();

    private String accessToken;
    private UUID currentUserId;
    private boolean loggedIn;
    private String currentUsername;

    private ListView<String> combinedListView;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Myth Cards Client");
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.getCSSList().add("chat.css"); 
    }

    @Override
    protected void initGame() {
    	  
        String savedUser = prefs.get("username", null);
        String savedPass = prefs.get("password", null);
        if (savedUser != null && savedPass != null) {
            tryLogin(savedUser, savedPass, null, true, true);
        } else {
            showLoginScene();
        }
    }

    private void showLoginScene() {
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        CheckBox rememberCb = new CheckBox("Remember me");
        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");
        Text statusText = new Text();

        loginButton.setOnAction(e -> tryLogin(usernameField.getText(), passwordField.getText(), statusText, false, rememberCb.isSelected()));
        registerButton.setOnAction(e -> tryRegister(usernameField.getText(), passwordField.getText(), statusText, rememberCb.isSelected()));

        VBox vbox = new VBox(10, usernameField, passwordField, rememberCb, loginButton, registerButton, statusText);
        vbox.setTranslateX(400);
        vbox.setTranslateY(200);

        FXGL.getGameScene().clearUINodes();
        FXGL.addUINode(vbox);
    }

    private void tryLogin(String username, String password, Text statusText, boolean isAuto, boolean remember) {
        try {
            String body = new ObjectMapper().writeValueAsString(new LoginRequest(username, password));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> FXGL.runOnce(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                AuthResponse auth = new ObjectMapper().readValue(response.body(), AuthResponse.class);
                                accessToken = auth.accessToken();
                                AuthTokenStore.getInstance().setAccessToken(accessToken);
                                currentUserId = UUID.fromString(auth.userId());
                                currentUsername = username;
                                friendClient.setAccessToken(accessToken);
                                loggedIn = true;
                                if (!isAuto && remember) {
                                    prefs.put("username", username);
                                    prefs.put("password", password);
                                }
                                showMainMenuScene();
                            } catch (Exception ex) {
                                statusText.setText("Error processing response");
                            }
                        } else {
                            statusText.setText("Login failed: " + response.statusCode());
                        }
                    }, javafx.util.Duration.ZERO));
        } catch (Exception ex) {
            statusText.setText("Error sending request");
        }
    }

    private void tryRegister(String username, String password, Text statusText, boolean remember) {
        try {
            String body = new ObjectMapper().writeValueAsString(new RegisterRequest(username, password));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/auth/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> FXGL.runOnce(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                AuthResponse auth = new ObjectMapper().readValue(response.body(), AuthResponse.class);
                                accessToken = auth.accessToken();
                                AuthTokenStore.getInstance().setAccessToken(accessToken);
                                currentUserId = UUID.fromString(auth.userId());
                                currentUsername = username;
                                friendClient.setAccessToken(accessToken);
                                loggedIn = true;
                                if (remember) {
                                    prefs.put("username", username);
                                    prefs.put("password", password);
                                }
                                showMainMenuScene();
                            } catch (Exception ex) {
                                statusText.setText("Error processing response");
                            }
                        } else {
                            statusText.setText("Register failed: " + response.statusCode());
                        }
                    }, javafx.util.Duration.ZERO));
        } catch (Exception ex) {
            statusText.setText("Error sending request");
        }
    }

    private void showMainMenuScene() {
        BorderPane root = new BorderPane();
        HBox topBar = new HBox(20);
        Button logoutBtn = new Button("Logout");
        Button settingsBtn = new Button("Settings");
        settingsBtn.setOnAction(e -> showSettingsScene());
        Button cardTestBtn = new Button("Card Test");
        cardTestBtn.setOnAction(e -> showCardTest());

        logoutBtn.setOnAction(e -> {
            loggedIn = false;
            accessToken = null;
            currentUserId = null;
            friendClient.setAccessToken(null);
            prefs.remove("username");
            prefs.remove("password");
            showLoginScene();
        });
        topBar.getChildren().addAll(new Button("Profile"), new Button("Match History"), cardTestBtn, settingsBtn, logoutBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER_LEFT);
        root.setTop(topBar);

        combinedListView = new ListView<>();
        combinedListView.setPrefHeight(500);

        VBox centerBox = new VBox(10, combinedListView);
        centerBox.setPadding(new Insets(10));
        root.setCenter(centerBox);

        FXGL.getGameScene().clearUINodes();
        FXGL.addUINode(root);

        fetchAndDisplay();
    }

    private void showCardTest() {
        // 1) Testkarte holen
        CardData card = CardDataFetcher.fetchCardData(1);
        if (card == null) {
            FXGL.getDialogService().showMessageBox("Fehler beim Laden der Testkarte!");
            return;
        }

        // 2) Texte registrieren
        CardLocalization.register(card);

        // 3) Ansicht erzeugen
        Node cardNode = new CardView(card);

        // 4) Szene aufräumen und Karte anzeigen
        FXGL.getGameScene().clearUINodes();
        FXGL.addUINode(cardNode);
    }

    private void showSettingsScene() {
        BorderPane root = new BorderPane(); 
        Text title = new Text("Settings");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button audioSettingsBtn = new Button("Audio Settings");
        audioSettingsBtn.setOnAction(e -> showAudioSettings());

        Button graphicsSettingsBtn = new Button("Graphics Settings (coming soon)");
        graphicsSettingsBtn.setDisable(true);
        Button controlsSettingsBtn = new Button("Controls Settings (coming soon)");
        controlsSettingsBtn.setDisable(true);

        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> showMainMenuScene());

        VBox menu = new VBox(15, title, audioSettingsBtn, graphicsSettingsBtn, controlsSettingsBtn, backBtn);
        menu.setPadding(new Insets(20));
        menu.setAlignment(Pos.TOP_CENTER);

        root.setCenter(menu);

        FXGL.getGameScene().clearUINodes();
        FXGL.addUINode(root);
    }

    private void showAudioSettings() {
        BorderPane root = new BorderPane();
        Text title = new Text("Audio Settings");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Slider gameVolume = new Slider(0, 100, 70);
        Slider musicVolume = new Slider(0, 100, 50);
        Slider discordVolume = new Slider(0, 100, 80);
        gameVolume.setShowTickLabels(true);
        musicVolume.setShowTickLabels(true);
        discordVolume.setShowTickLabels(true);
        gameVolume.setMajorTickUnit(25);
        musicVolume.setMajorTickUnit(25);
        discordVolume.setMajorTickUnit(25);

        VBox sliders = new VBox(15,
            new VBox(new Text("Game Volume"), gameVolume),
            new VBox(new Text("Custom Music Volume"), musicVolume),
            new VBox(new Text("Discord Voice Volume"), discordVolume)
        );

        Button loadMusicBtn = new Button("Load Your Music");
        loadMusicBtn.setOnAction(e -> {
            FXGL.getDialogService().showMessageBox("TODO: FilePicker für MP3s öffnen und Playlist verwalten");
        });

        Button backBtn = new Button("Back to Settings");
        backBtn.setOnAction(e -> showSettingsScene());

        VBox content = new VBox(20, title, sliders, loadMusicBtn, backBtn);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_CENTER);

        root.setCenter(content);

        FXGL.getGameScene().clearUINodes();
        FXGL.addUINode(root);
    }

    private void fetchAndDisplay() {
        if (!loggedIn || currentUserId == null || accessToken == null) return;
        CompletableFuture<List<PendingRequestDTO>> reqFut = friendClient.fetchPendingRequests(currentUserId);
        CompletableFuture<List<String>> friFut = friendClient.fetchFriends(currentUserId);
        reqFut.thenCombine(friFut, (reqs, friends) -> {
            FXGL.runOnce(() -> {
                List<String> combined = new ArrayList<>();
                DateTimeFormatter dbFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());
                for (PendingRequestDTO r : reqs) {
                    String exp = "";
                    if (r.expiresAt() != null && !r.expiresAt().isBlank()) {
                        try {
                            LocalDateTime ldt = LocalDateTime.parse(r.expiresAt(), dbFmt);
                            exp = " (exp: " + fmt.format(ldt.atZone(ZoneId.systemDefault()).toInstant()) + ")";
                        } catch (DateTimeParseException ex) {
                            exp = " (exp: " + r.expiresAt() + ")";
                        }
                    }
                    combined.add("[Request] " + r.senderUsername() + exp);
                }
                friends.sort(String::compareToIgnoreCase);
                combined.addAll(friends);
                combinedListView.getItems().setAll(combined);
            }, javafx.util.Duration.ZERO);
            return null;
        }).exceptionally(ex -> { ex.printStackTrace(); return null; });
    }

    private void promptAddFriend() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Add Friend");
        dlg.setHeaderText("Enter friend's username:");
        dlg.showAndWait().ifPresent(username -> {
            friendClient.fetchUserIdByUsername(username)
                .thenCompose(targetId -> {
                    if (targetId == null || targetId.equals(currentUserId))
                        return CompletableFuture.completedFuture(false);
                    return friendClient.fetchFriends(currentUserId)
                        .thenCompose(f -> f.contains(username)
                            ? CompletableFuture.completedFuture(false)
                            : friendClient.sendFriendRequest(currentUserId, targetId)
                        );
                })
                .whenComplete((ok, err) -> FXGL.runOnce(() -> {
                    if (err != null) FXGL.getDialogService().showErrorBox(err.getMessage(), null);
                    else if (!ok) FXGL.getDialogService().showMessageBox("Could not send request to " + username);
                    else FXGL.getDialogService().showMessageBox("Friend request sent to " + username);
                    fetchAndDisplay();
                }, javafx.util.Duration.ZERO));
        });
    }

    private void openChat(String user) {
        // TODO: chat logic
    }

    public static void main(String[] args) {
        launch(args);
    }
}
