package bomberman;

import bomberman.entities.*;
import bomberman.map.MapLoader;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import bomberman.GameConfig;

public class Main extends Application {
    public static final int TILE_SIZE = GameConfig.TILE_SIZE;
    public static final int WIDTH = GameConfig.WIDTH;
    public static final int HEIGHT = GameConfig.HEIGHT;

    private GraphicsContext gc;
    private Set<KeyCode> inputKeys = new HashSet<>();
    private List<Entity> entities;
    private Bomber player;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();

        Group root = new Group(canvas);
        Scene scene = new Scene(root);

        // Bắt sự kiện phím
        scene.setOnKeyPressed(e -> inputKeys.add(e.getCode()));
        scene.setOnKeyReleased(e -> inputKeys.remove(e.getCode()));

        stage.setTitle("Bomberman Game - JavaFX");
        stage.setScene(scene);
        stage.show();

        // Khởi tạo nhân vật & nạp bản đồ
        player = new Bomber(32, 32, null);
        entities = MapLoader.loadMap("res/levels/level1.txt");

        // Vòng lặp Game (Game Loop - 60 FPS)
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render();
            }
        };
        timer.start();
    }

    private void update() {
        player.handleInput(inputKeys);

        // Cập nhật vị trí Bomber và kiểm tra va chạm
        player.update(entities);

        for (Entity entity : entities) {
            entity.update();
        }
    }

    private void render() {
        gc.clearRect(0, 0, WIDTH, HEIGHT);

        // Vẽ tường và gạch
        for (Entity entity : entities) {
            entity.render(gc);
        }

        // Vẽ Bomber
        player.render(gc);
    }
}