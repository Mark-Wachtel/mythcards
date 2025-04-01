package client;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;

public class ClientMain extends GameApplication {

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Myth Cards");
        settings.setWidth(1280);
        settings.setHeight(720);
    }

    @Override
    protected void initGame() {
        // Hier deine FXGL-Spielinitialisierung
    }

    public static void main(String[] args) {
        launch(args);
    }
}
