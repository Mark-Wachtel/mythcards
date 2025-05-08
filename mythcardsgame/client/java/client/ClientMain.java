package client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.List;
import java.util.UUID;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;

import javafx.scene.image.ImageView;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import common.CardData;
import common.AuthResponse;
import common.LoginRequest;
import common.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;

	public class ClientMain extends GameApplication {

	    private List<CardData> cardList;
	    private final HttpClient httpClient = HttpClient.newHttpClient();
	    private final ObjectMapper objectMapper = new ObjectMapper();
	    private String accessToken;
	    private String refreshToken;
	    private long expiresIn;
	    private boolean loggedIn = false;

	    @Override
	    protected void initSettings(GameSettings settings) {
	        settings.setTitle("Myth Cards");
	        settings.setWidth(1280);
	        settings.setHeight(720);
	    }

	    @Override
	    protected void initGame() {
	        showLoginScene();
	    }

	    private void showLoginScene() {
	        TextField usernameField = new TextField();
	        usernameField.setPromptText("Username");

	        PasswordField passwordField = new PasswordField();
	        passwordField.setPromptText("Password");

	        Button loginButton = new Button("Login");
	        Button registerButton = new Button("Register");
	        Button offlineButton = new Button("Offline (coming soon)");
	        offlineButton.setDisable(true);

	        Text infoText = new Text();

	        loginButton.setOnAction(e -> tryLogin(usernameField.getText(), passwordField.getText(), infoText));
	        registerButton.setOnAction(e -> tryRegister(usernameField.getText(), passwordField.getText(), infoText));

	        VBox vbox = new VBox(10, usernameField, passwordField, loginButton, registerButton, offlineButton, infoText);
	        vbox.setTranslateX(400);
	        vbox.setTranslateY(200);

	        FXGL.getGameScene().clearUINodes();
	        FXGL.addUINode(vbox);
	    }

	    private void tryLogin(String username, String password, Text infoText) {
	        try {
	            LoginRequest loginRequest = new LoginRequest(username, password);
	            String requestBody = objectMapper.writeValueAsString(loginRequest);

	            HttpRequest request = HttpRequest.newBuilder()
	                    .uri(URI.create("http://localhost:8080/auth/login"))
	                    .header("Content-Type", "application/json")
	                    .POST(BodyPublishers.ofString(requestBody))
	                    .build();

	            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
	                .thenAccept(response -> {
	                    FXGL.runOnce(() -> {
	                        System.out.println("STATUS: " + response.statusCode());
	                        System.out.println("BODY: " + response.body());
	                    }, Duration.ZERO);

	                    if (response.statusCode() == 200) {
	                        try {
	                            AuthResponse auth = objectMapper.readValue(response.body(), AuthResponse.class);
	                            accessToken = auth.accessToken();
	                            refreshToken = auth.refreshToken();
	                            expiresIn = auth.expiresIn();

	                            FXGL.runOnce(() -> {
	                                infoText.setText("Login erfolgreich!");
	                                loggedIn = true;
	                                startGame();
	                            }, Duration.ZERO);
	                        } catch (Exception ex) {
	                            ex.printStackTrace();
	                            FXGL.runOnce(() -> infoText.setText("Fehler beim Verarbeiten!"), Duration.ZERO);
	                        }
	                    } else {
	                        FXGL.runOnce(() -> infoText.setText("Login fehlgeschlagen!"), Duration.ZERO);
	                    }
	                })
	                .exceptionally(ex -> {
	                    FXGL.runOnce(() -> {
	                        System.out.println("EXCEPTION: " + ex.getMessage());
	                        ex.printStackTrace();
	                    }, Duration.ZERO);
	                    return null;
	                });
	        } catch (Exception ex) {
	            ex.printStackTrace();
	            infoText.setText("Fehler beim Senden!");
	        }
	    }

	    private void tryRegister(String username, String password, Text infoText) {
	        try {
	        	
	        	FXGL.runOnce(() -> FXGL.addUINode(new Text(">>> tryRegister wurde aufgerufen!")), Duration.seconds(1));
	            RegisterRequest registerRequest = new RegisterRequest(username, password);
	            String requestBody = objectMapper.writeValueAsString(registerRequest);

	            HttpRequest request = HttpRequest.newBuilder()
	                    .uri(URI.create("http://localhost:8080/auth/register"))
	                    .header("Content-Type", "application/json")
	                    .POST(BodyPublishers.ofString(requestBody))
	                    .build();
	            
	            System.out.println("SENDE an /auth/register: " + requestBody);

	            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
	                .thenAccept(response -> {
	                    FXGL.runOnce(() -> {
	                        System.out.println("STATUS: " + response.statusCode());
	                        System.out.println("BODY: " + response.body());
	                    }, Duration.ZERO);

	                    if (response.statusCode() == 200) {
	                        try {
	                            AuthResponse auth = objectMapper.readValue(response.body(), AuthResponse.class);
	                            accessToken = auth.accessToken();
	                            refreshToken = auth.refreshToken();
	                            expiresIn = auth.expiresIn();

	                            FXGL.runOnce(() -> {
	                                infoText.setText("Registrierung erfolgreich!");
	                                loggedIn = true;
	                                startGame();
	                            }, Duration.ZERO);
	                        } catch (Exception ex) {
	                            ex.printStackTrace();
	                            FXGL.runOnce(() -> infoText.setText("Fehler beim Verarbeiten!"), Duration.ZERO);
	                        }
	                    } else if (response.statusCode() == 409) {
	                        FXGL.runOnce(() -> infoText.setText("Username schon vergeben!"), Duration.ZERO);
	                    } else {
	                       // FXGL.runOnce(() -> infoText.setText("Registrierung fehlgeschlagen!"), Duration.ZERO);
	                        FXGL.runOnce(() -> {
	                        	 infoText.setText("Unbekannte Antwort:\nStatus: " + response.statusCode() + "\nBody: [" + response.body() + "]");
	                        }, Duration.ZERO);
	                    }
	                })
	                .exceptionally(ex -> {
	                    FXGL.runOnce(() -> {
	                        System.out.println("EXCEPTION: " + ex.getMessage());
	                        ex.printStackTrace();
	                    }, Duration.ZERO);
	                    return null;
	                });
	        } catch (Exception ex) {
	            ex.printStackTrace();
	            infoText.setText("Fehler beim Senden!");
	        }
	    }

	    private void startGame() {
	        if (!loggedIn) {
	            System.out.println("Spielstart blockiert: Noch nicht eingeloggt!");
	            return;
	        }

	        FXGL.getGameScene().clearUINodes();
	        initRealGame();
	    }

	    private void initRealGame() {
	        cardList = List.of(); // Leere Liste als Platzhalter

	        int y = 50;
	        for (CardData card : cardList) {
	            String info = "[%s] %s (%s)".formatted(card.type(), card.title(), card.element());
	            FXGL.addUINode(new Text(info), 50, y);
	            y += 30;
	        }

	        ImageView cardView = FXGL.texture("generated/card_1_filled.png");
	        FXGL.addUINode(cardView, 100, 100);
	    }

	    public static void main(String[] args) {
	        launch(args);
	    }
	}

