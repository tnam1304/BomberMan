
package bomberman;

import bomberman.entities.*;
import bomberman.enums.GameState;
import bomberman.map.MapLoader;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import bomberman.EnemyFactory;
import bomberman.enums.EnemyType;

public class Main extends Application {

    public static final int TILE_SIZE = GameConfig.TILE_SIZE;
    public static final int WIDTH = GameConfig.WIDTH;
    public static final int HEIGHT = GameConfig.HEIGHT;

    private GraphicsContext gc;

    private final Set<KeyCode> inputKeys = new HashSet<>();

    private List<Entity> entities = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Bomb> bombs = new ArrayList<>();
    private final List<Item> items = new ArrayList<>();

    private Bomber player;

    private GameState gameState = GameState.MENU;

    private boolean spaceWasDown = false;

    private int score = 0;
    private int highScore = 0;

    private Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        primaryStage = stage;

        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();

        Group root = new Group(canvas);
        root.setFocusTraversable(true);

        Scene scene = new Scene(root);

        scene.setOnKeyPressed(e -> {

            KeyCode key = e.getCode();

            // Chỉ xử lý một lần khi nhấn phím
            if (!inputKeys.contains(key)) {
                handleGameKey(key);
            }

            inputKeys.add(key);
        });

        scene.setOnKeyReleased(e -> {
            inputKeys.remove(e.getCode());
        });

        highScore = HighScoreManager.loadHighScore();

        stage.setTitle("Bomberman Game");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        root.requestFocus();

        AnimationTimer timer = new AnimationTimer() {

            @Override
            public void handle(long now) {
                update();
                render();
            }
        };

        timer.start();
    }

    // Xử lý phím điều khiển trạng thái game
    private void handleGameKey(KeyCode key) {

        switch (gameState) {

            case MENU -> {
                if (key == KeyCode.ENTER) {
                    startGame();
                }
            }

            case PLAYING -> {
                if (key == KeyCode.P || key == KeyCode.ESCAPE) {
                    gameState = GameState.PAUSED;
                    inputKeys.clear();
                }
            }

            case PAUSED -> {
                if (key == KeyCode.P || key == KeyCode.ESCAPE) {
                    gameState = GameState.PLAYING;
                    inputKeys.clear();
                }

                if (key == KeyCode.R) {
                    startGame();
                }
            }

            case GAME_OVER, VICTORY -> {
                if (key == KeyCode.R || key == KeyCode.ENTER) {
                    startGame();
                }
            }
        }
    }

    // Khởi tạo một lượt chơi mới
    private void startGame() {

        entities = MapLoader.loadMap("res/levels/level1.txt");

        if (entities == null) {
            System.err.println("Không thể tải bản đồ!");
            return;
        }

        enemies.clear();
        bombs.clear();
        items.clear();
        inputKeys.clear();

        score = 0;
        spaceWasDown = false;

        player = new Bomber(32, 32, null);

        enemies.add(
                new Enemy(15 * TILE_SIZE, 9 * TILE_SIZE)
        );

        enemies.add(
                new ChasingEnemy(25 * TILE_SIZE, 9 * TILE_SIZE)
        );

        gameState = GameState.PLAYING;
    }

    private void update() {

        if (gameState != GameState.PLAYING) {
            return;
        }

        if (!player.isAlive()) {
            gameState = GameState.GAME_OVER;
            saveHighScore();
            return;
        }

        // Di chuyển Bomber
        player.handleInput(inputKeys);
        player.update(entities);

        // Đặt bom
        boolean spaceDown = inputKeys.contains(KeyCode.SPACE);

        if (spaceDown && !spaceWasDown
                && bombs.size() < player.getMaxBombs()) {

            int bombX = Math.round(
                    (float) player.getX() / TILE_SIZE
            ) * TILE_SIZE;

            int bombY = Math.round(
                    (float) player.getY() / TILE_SIZE
            ) * TILE_SIZE;

            // Không đặt hai bom cùng một vị trí
            boolean occupied = false;

            for (Bomb bomb : bombs) {

                if (!bomb.isExploded()
                        && bomb.getX() == bombX
                        && bomb.getY() == bombY) {

                    occupied = true;
                    break;
                }
            }

            if (!occupied) {
                bombs.add(
                        new Bomb(bombX, bombY, player.getFlameRange())
                );
            }
        }

        spaceWasDown = spaceDown;

        // Cập nhật bom và chain reaction
        updateBombs();

        // Cập nhật Enemy
        updateEnemies();

        // Nhặt vật phẩm
        updateItems();

        // Cập nhật bản đồ
        for (Entity entity : entities) {
            entity.update();
        }

        // Kiểm tra Game Over
        if (!player.isAlive()) {
            gameState = GameState.GAME_OVER;
            saveHighScore();
            return;
        }

        // Kiểm tra chiến thắng màn hiện tại
        boolean allEnemiesDead = true;

        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                allEnemiesDead = false;
                break;
            }
        }

        if (allEnemiesDead) {
            gameState = GameState.VICTORY;
            saveHighScore();
        }
    }

    // Cập nhật bom
    private void updateBombs() {

        for (int i = 0; i < bombs.size(); i++) {

            Bomb bomb = bombs.get(i);

            bomb.update(entities);

            // Chain reaction
            if (bomb.isExploded()) {

                for (Bomb otherBomb : bombs) {

                    if (otherBomb != bomb
                            && !otherBomb.isExploded()
                            && bomb.hits(otherBomb.getBoundary())) {

                        otherBomb.explodeNow(entities);
                    }
                }
            }

            // Tính điểm phá Brick
            int destroyedBricks = bomb.takeDestroyedBrickCount();
            score += destroyedBricks * 10;

            // Nhận vật phẩm rơi ra
            items.addAll(bomb.takeDroppedItems());

            // Bomber trúng lửa
            if (bomb.hits(player.getBoundary())) {
                if (player.hurt()) {
                    // Mỗi lần mất mạng chỉ tính một lần
                }
            }

            // Enemy trúng lửa
            for (Enemy enemy : enemies) {

                if (enemy.isAlive()
                        && bomb.hits(enemy.getBoundary())) {

                    enemy.setAlive(false);
                    score += 100;
                }
            }
        }

        // Xóa bom đã kết thúc
        bombs.removeIf(Bomb::isFinished);
    }

    // Cập nhật Enemy
    private void updateEnemies() {

        for (Enemy enemy : enemies) {

            if (!enemy.isAlive()) {
                continue;
            }

            if (enemy instanceof ChasingEnemy chasingEnemy) {
                chasingEnemy.update(entities, player);
            } else {
                enemy.update(entities);
            }

            // Enemy chạm Bomber
            if (enemy.getBoundary().intersects(player.getBoundary())) {
                player.hurt();
            }
        }
    }

    // Cập nhật vật phẩm
    private void updateItems() {

        List<Item> itemsToRemove = new ArrayList<>();

        for (Item item : items) {

            if (player.getBoundary().intersects(item.getBoundary())) {

                player.collectItem(item.getType());

                score += 25;

                itemsToRemove.add(item);
            }
        }

        items.removeAll(itemsToRemove);
    }

    private void saveHighScore() {

        if (score > highScore) {
            highScore = score;
            HighScoreManager.saveHighScore(highScore);
        }
    }

    // Vẽ toàn bộ game
    private void render() {

        gc.setFill(Color.rgb(35, 35, 35));
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        if (gameState == GameState.MENU) {
            renderMenu();
            return;
        }

        renderGameWorld();
        renderHUD();

        switch (gameState) {

            case PAUSED -> renderOverlay(
                    "PAUSED",
                    "P / ESC: Continue",
                    "R: Restart"
            );

            case GAME_OVER -> renderOverlay(
                    "GAME OVER",
                    "Score: " + score,
                    "ENTER / R: Play Again"
            );

            case VICTORY -> renderOverlay(
                    "VICTORY!",
                    "Score: " + score,
                    "ENTER / R: Play Again"
            );

            default -> {
            }
        }
    }

    private void renderGameWorld() {

        // Bản đồ
        for (Entity entity : entities) {
            entity.render(gc);
        }

        // Vật phẩm
        for (Item item : items) {
            item.render(gc);
        }

        // Enemy
        for (Enemy enemy : enemies) {

            if (enemy.isAlive()) {
                enemy.render(gc);
            }
        }

        // Bomber
        if (player.isAlive()) {
            player.render(gc);
        }

        // Bom
        for (Bomb bomb : bombs) {
            bomb.render(gc);
        }
    }

    // HUD
    private void renderHUD() {

        gc.setFill(Color.rgb(20, 20, 20, 0.9));
        gc.fillRect(0, 0, WIDTH, 30);

        gc.setFont(Font.font("Arial", 14));

        gc.setFill(Color.WHITE);

        gc.fillText("Lives: " + player.getLives(), 10, 20);
        gc.fillText("Score: " + score, 120, 20);
        gc.fillText("High Score: " + highScore, 250, 20);

        gc.fillText("Bombs: " + player.getMaxBombs(), 440, 20);
        gc.fillText("Range: " + player.getFlameRange(), 550, 20);
        gc.fillText("Speed: " + player.getSpeed(), 660, 20);

        gc.fillText("P / ESC: Pause", 790, 20);
    }

    private void renderMenu() {

        gc.setFill(Color.rgb(20, 20, 20));
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);

        gc.setFont(Font.font("Arial", 48));
        gc.setFill(Color.ORANGE);
        gc.fillText(
                "BOMBERMAN",
                WIDTH / 2.0,
                HEIGHT / 2.0 - 50
        );

        gc.setFont(Font.font("Arial", 22));
        gc.setFill(Color.WHITE);
        gc.fillText(
                "Press ENTER to Start",
                WIDTH / 2.0,
                HEIGHT / 2.0 + 20
        );

        gc.setFont(Font.font("Arial", 16));
        gc.setFill(Color.LIGHTGRAY);
        gc.fillText(
                "WASD / Arrow Keys: Move     SPACE: Bomb",
                WIDTH / 2.0,
                HEIGHT / 2.0 + 65
        );

        gc.fillText(
                "High Score: " + highScore,
                WIDTH / 2.0,
                HEIGHT / 2.0 + 105
        );

        gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
    }

    private void renderOverlay(
            String title,
            String line1,
            String line2
    ) {

        gc.setFill(Color.rgb(0, 0, 0, 0.75));
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);

        gc.setFont(Font.font("Arial", 42));
        gc.setFill(Color.ORANGE);
        gc.fillText(
                title,
                WIDTH / 2.0,
                HEIGHT / 2.0 - 40
        );

        gc.setFont(Font.font("Arial", 20));
        gc.setFill(Color.WHITE);
        gc.fillText(
                line1,
                WIDTH / 2.0,
                HEIGHT / 2.0 + 10
        );

        gc.fillText(
                line2,
                WIDTH / 2.0,
                HEIGHT / 2.0 + 50
        );

        gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
    }
}
