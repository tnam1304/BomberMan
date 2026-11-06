
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
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import bomberman.EnemyFactory;
import bomberman.enums.EnemyType;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

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

    private int currentLevel = 1;

    private int exitX = -1;
    private int exitY = -1;
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

            // SỬA ĐOẠN NÀY TRONG handleGameKey:
            case MENU -> {
                if (key == KeyCode.N) {
                    startGame(1, true); // Chơi mới từ Level 1, reset điểm
                } else if (key == KeyCode.C) {
                    int savedLevel = GameProgressManager.loadProgress();
                    startGame(savedLevel, false); // Chơi tiếp level đã lưu
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
                    startGame(1, true);
                }
            }

            case GAME_OVER, VICTORY -> {
                 if (key == KeyCode.R || key == KeyCode.ENTER) {
                    startGame(1, true);
                }
            }
        }
    }

    // Khởi tạo một lượt chơi mới

private void startGame(int level, boolean resetScore) {

    String[] mapPaths = {
            "res/levels/level1.txt",
            "res/levels/level2.txt",
            "res/levels/level3.txt"
    };

    if (level < 1 || level > mapPaths.length) {
        return;
    }

    MapLoader.LevelData data =
            MapLoader.loadMap(mapPaths[level - 1]);

    if (data == null) {
        System.err.println("Không thể tải bản đồ!");
        return;
    }

    currentLevel = level;

    entities = data.entities;

    enemies.clear();
    bombs.clear();
    items.clear();
    inputKeys.clear();

    if (resetScore) {
        score = 0;
    }

    spaceWasDown = false;

    player = new Bomber(
            data.playerX,
            data.playerY,
            GameAssets.PLAYER
    );

    exitX = data.exitX;
    exitY = data.exitY;

    for (MapLoader.EnemySpawn spawn : data.enemySpawns) {

        if (spawn.chasing) {
            enemies.add(new ChasingEnemy(spawn.x, spawn.y));
        } else {
            enemies.add(new Enemy(spawn.x, spawn.y));
        }
    }

    gameState = GameState.PLAYING;
}

private void update() {

    if (gameState != GameState.PLAYING) {
        return;
    }

    // Kiểm tra Game Over
    if (!player.isAlive()) {
        gameState = GameState.GAME_OVER;
        saveHighScore();
        return;
    }

    // Di chuyển Bomber
    player.handleInput(inputKeys);
    player.update(entities);

    // Đặt bom khi nhấn SPACE
    boolean spaceDown = inputKeys.contains(KeyCode.SPACE);

    if (spaceDown && !spaceWasDown
            && bombs.size() < player.getMaxBombs()) {

        int bombX = Math.round(
                (float) player.getX() / TILE_SIZE
        ) * TILE_SIZE;

        int bombY = Math.round(
                (float) player.getY() / TILE_SIZE
        ) * TILE_SIZE;

        // Kiểm tra vị trí đã có bom chưa
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

    // Cập nhật bom và phản ứng dây chuyền
    updateBombs();

    // Cập nhật Enemy
    updateEnemies();

    // Nhặt Item
    updateItems();

    // Cập nhật các đối tượng trên bản đồ
    for (Entity entity : entities) {
        entity.update();
    }

    // Kiểm tra Game Over sau khi cập nhật
    if (!player.isAlive()) {
        gameState = GameState.GAME_OVER;
        saveHighScore();
        return;
    }

    // Kiểm tra điều kiện mở cửa và chuyển màn
    if (gameState == GameState.PLAYING) {
    checkLevelCompletion();
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

    // Nền tổng thể
    gc.setFill(Color.rgb(8, 19, 48));
    gc.fillRect(0, 0, WIDTH, HEIGHT);

    // Họa tiết nền
    gc.setFill(Color.rgb(30, 55, 105, 0.35));

    for (int i = 0; i < 12; i++) {
        gc.fillOval(40 + i * 125, 120 + (i % 3) * 210, 8, 8);
    }

    if (gameState == GameState.MENU) {
        renderMenu();
        return;
    }

    // Vẽ khu vực bản đồ
    renderBoardFrame();

    gc.save();

    // Dịch chuyển và phóng to bản đồ
    gc.translate(GameConfig.BOARD_X, GameConfig.BOARD_Y);
    gc.scale(GameConfig.RENDER_SCALE, GameConfig.RENDER_SCALE);

    renderGameWorld();

    gc.restore();

    // Vẽ HUD và bảng thông tin
    renderHUD();
    renderSidePanel();

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
                "YOU WIN!",
                "You completed all 3 levels!",
                "ENTER / R: Play Again"
        );

        default -> {
        }
    }
}


private void renderBoardFrame() {

    double x = GameConfig.BOARD_X;
    double y = GameConfig.BOARD_Y;
    double w = GameConfig.BOARD_WIDTH;
    double h = GameConfig.BOARD_HEIGHT;

    // Bóng phía dưới
    gc.setFill(Color.rgb(0, 0, 0, 0.5));
    gc.fillRoundRect(x + 8, y + 10, w, h, 18, 18);

    // Nền khung
    gc.setFill(Color.rgb(17, 39, 83));
    gc.fillRoundRect(x, y, w, h, 18, 18);

    // Viền xanh
    gc.setStroke(Color.rgb(65, 120, 220));
    gc.setLineWidth(4);
    gc.strokeRoundRect(x, y, w, h, 18, 18);

    // Viền sáng bên trong
    gc.setStroke(Color.rgb(130, 180, 255, 0.5));
    gc.setLineWidth(2);
    gc.strokeRoundRect(
            x + 6, y + 6,
            w - 12, h - 12,
            14, 14
    );
}


private void checkLevelCompletion() {

    boolean allEnemiesDead = true;

    for (Enemy enemy : enemies) {
        if (enemy.isAlive()) {
            allEnemiesDead = false;
            break;
        }
    }

    // Chưa tiêu diệt hết Enemy thì cửa chưa mở
    if (!allEnemiesDead) {
        return;
    }

    // Kiểm tra Player có chạm cửa thoát không
    javafx.geometry.Rectangle2D exitBounds =
            new javafx.geometry.Rectangle2D(
                    exitX,
                    exitY,
                    TILE_SIZE,
                    TILE_SIZE
            );

    // SỬA ĐOẠN NÀY TRONG checkLevelCompletion:
    if (player.getBoundary().intersects(exitBounds)) {
        saveHighScore();

        if (currentLevel < 3) {
            int nextLevel = currentLevel + 1;
            GameProgressManager.saveProgress(nextLevel); // <--- THÊM DÒNG NÀY ĐỂ LƯU PROGRESS
            startGame(nextLevel, false);
        } else {
            gameState = GameState.VICTORY;
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
        // Vẽ cửa thoát
        if (exitX >= 0 && exitY >= 0) {
            gc.setFill(Color.DARKGREEN);
            renderExit();

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 24));
            gc.fillText("X",
                    exitX + TILE_SIZE / 2 - 7,
                    exitY + TILE_SIZE / 2 + 8);
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


private void renderExit() {

    boolean allEnemiesDead = true;

    for (Enemy enemy : enemies) {
        if (enemy.isAlive()) {
            allEnemiesDead = false;
            break;
        }
    }

    if (allEnemiesDead) {
        gc.setFill(Color.LIMEGREEN);
    } else {
        gc.setFill(Color.DARKRED);
    }

    gc.fillRect(exitX + 3, exitY + 3, 26, 26);

    gc.setStroke(Color.WHITE);
    gc.strokeRect(exitX + 3, exitY + 3, 26, 26);

    gc.setFill(Color.WHITE);
    gc.setFont(Font.font("Arial", 16));

    gc.fillText(
            "X",
            exitX + 11,
            exitY + 22
    );
}


    // HUD
    private void renderHUD() {
        double y = 25;
    double h = 85;

    double[] x = {35, 345, 655, 965, 1275};
    double[] w = {290, 290, 290, 290, 130};

    String[] labels = {
        "LIVES",
        "SCORE",
        "LEVEL",
        "BOMBS",
        "RANGE"
    };

    String[] values = {
        String.valueOf(player.getLives()),
        String.valueOf(score),
        currentLevel + " - 1",
        String.valueOf(player.getMaxBombs()),
        String.valueOf(player.getFlameRange())
    };

    for (int i = 0; i < x.length; i++) {

        gc.setFill(Color.rgb(15, 38, 91));
        gc.fillRoundRect(x[i], y, w[i], h, 20, 20);

        gc.setStroke(Color.rgb(75, 135, 245));
        gc.setLineWidth(3);
        gc.strokeRoundRect(x[i], y, w[i], h, 20, 20);

        gc.setFill(Color.rgb(125, 180, 255, 0.3));
        gc.fillRoundRect(
                x[i] + 8, y + 7,
                w[i] - 16, 5, 5, 5
        );

        gc.setTextAlign(
                javafx.scene.text.TextAlignment.CENTER
        );

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        gc.setFill(Color.LIGHTBLUE);
        gc.fillText(labels[i], x[i] + w[i] / 2, y + 29);

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        gc.setFill(Color.WHITE);
        gc.fillText(values[i], x[i] + w[i] / 2, y + 67);
    }

    gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
    }


private void renderSidePanel() {

    double x = 925;
    double y = 150;
    double w = 470;
    double h = 704;

    // Khung chính
    gc.setFill(Color.rgb(15, 36, 78));
    gc.fillRoundRect(x, y, w, h, 24, 24);

    gc.setStroke(Color.rgb(60, 115, 215));
    gc.setLineWidth(3);
    gc.strokeRoundRect(x, y, w, h, 24, 24);

    gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);

    // Tiêu đề
    gc.setFont(Font.font("Arial", FontWeight.BOLD, 30));
    gc.setFill(Color.GOLD);
    gc.fillText("HOW TO PLAY", x + w / 2, y + 55);

    // Đường phân cách
    gc.setStroke(Color.rgb(90, 145, 230));
    gc.strokeLine(x + 35, y + 75, x + w - 35, y + 75);

    // Hướng dẫn
    gc.setFont(Font.font("Arial", FontWeight.BOLD, 22));
    gc.setFill(Color.WHITE);

    gc.fillText("MOVEMENT", x + w / 2, y + 125);

    gc.setFont(Font.font("Arial", 19));
    gc.setFill(Color.LIGHTBLUE);

    gc.fillText("W A S D / ARROW KEYS", x + w / 2, y + 165);
    gc.fillText("SPACE : PLACE BOMB", x + w / 2, y + 205);
    gc.fillText("P : PAUSE GAME", x + w / 2, y + 245);

    // Khung trạng thái
    gc.setFill(Color.rgb(25, 60, 110));
    gc.fillRoundRect(x + 30, y + 290, w - 60, 120, 18, 18);

    gc.setFont(Font.font("Arial", FontWeight.BOLD, 22));
    gc.setFill(Color.WHITE);
    gc.fillText("MISSION", x + w / 2, y + 325);

    gc.setFont(Font.font("Arial", 17));
    gc.setFill(Color.LIGHTGREEN);
    gc.fillText("Defeat all enemies", x + w / 2, y + 360);
    gc.fillText("Find the exit door", x + w / 2, y + 390);

    // Trạng thái cửa
    boolean allDead = true;

    for (Enemy enemy : enemies) {
        if (enemy.isAlive()) {
            allDead = false;
            break;
        }
    }

    gc.setFont(Font.font("Arial", FontWeight.BOLD, 24));

    if (allDead) {
        gc.setFill(Color.LIMEGREEN);
        gc.fillText("EXIT UNLOCKED", x + w / 2, y + 485);
    } else {
        gc.setFill(Color.ORANGE);
        gc.fillText("EXIT LOCKED", x + w / 2, y + 485);
    }

    gc.setFont(Font.font("Arial", FontWeight.BOLD, 64));
    gc.setFill(Color.LIMEGREEN);
    gc.fillText("X", x + w / 2, y + 580);

    gc.setFont(Font.font("Arial", 16));
    gc.setFill(Color.LIGHTGRAY);
    gc.fillText("BOMBERMAN ADVENTURE", x + w / 2, y + 655);

    gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
}


    private void renderMenu() {
        // Lấy chiều rộng và chiều cao thực tế của Canvas để căn giữa chính xác
        double width = gc.getCanvas().getWidth();
        double height = gc.getCanvas().getHeight();
        double centerX = width / 2.0;
        double centerY = height / 2.0;
        double time = System.currentTimeMillis() * 0.003;

        // 1. NỀN TỐI SÂU TƯƠNG PHẢN CAO
        gc.setFill(Color.rgb(10, 10, 18));
        gc.fillRect(0, 0, width, height);

        // 2. LƯỚI SYNTHWAVE TRÔI Ở NỀN
        gc.setStroke(Color.rgb(0, 240, 255, 0.12));
        gc.setLineWidth(1);

        for (int x = 0; x < width; x += 40) {
            gc.strokeLine(x, 0, x, height);
        }
        double gridOffset = (time * 30) % 40;
        for (double y = gridOffset; y < height; y += 40) {
            gc.strokeLine(0, y, width, y);
        }

        // ÉP CĂN GIỮA TOÀN BỘ CHỮ TRONG MENU
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);

        // 3. TIÊU ĐỀ "BOMBERMAN"
        double titleY = centerY - 140;

        // Bóng khối 3D
        gc.setFont(Font.font("Arial", FontWeight.BLACK, 62));
        gc.setFill(Color.rgb(180, 20, 60));
        gc.fillText("BOMBERMAN", centerX + 4, titleY + 4);

        // Chữ chính màu Vàng
        gc.setFill(Color.rgb(255, 230, 0));
        gc.fillText("BOMBERMAN", centerX, titleY);

        // Viền Đỏ Cam
        gc.setStroke(Color.rgb(255, 60, 0));
        gc.setLineWidth(2);
        gc.strokeText("BOMBERMAN", centerX, titleY);

        // Subtitle nhấp nháy
        double alpha = 0.6 + 0.4 * Math.sin(time * 4);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.setFill(Color.rgb(0, 240, 255, alpha));
        gc.fillText("PRESS [KEY] TO SELECT", centerX, titleY + 38);

        // 4. KHUNG MENU CHÍNH (Nới rộng panelW lên 500px)
        double panelW = 500;
        double panelH = 180;
        double panelX = centerX - panelW / 2.0;
        double panelY = centerY - 60;

        // Nền khung đen trong suốt
        gc.setFill(Color.rgb(16, 20, 32, 0.9));
        gc.fillRoundRect(panelX, panelY, panelW, panelH, 14, 14);

        // Viền khung Neon Cyan & Pink
        gc.setStroke(Color.rgb(0, 240, 255, 0.8));
        gc.setLineWidth(2);
        gc.strokeRoundRect(panelX, panelY, panelW, panelH, 14, 14);

        gc.setStroke(Color.rgb(255, 0, 128, 0.5));
        gc.setLineWidth(1);
        gc.strokeRoundRect(panelX + 4, panelY + 4, panelW - 8, panelH - 8, 10, 10);

        // 5. CÁC NÚT LỰA CHỌN (Đã được căn giữa theo centerX)
        int savedLevel = GameProgressManager.loadProgress();

        // Option 1: NEW GAME
        double opt1Y = panelY + 62;
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        gc.setFill(Color.rgb(50, 255, 126));
        gc.fillText("[ N ]   NEW GAME", centerX, opt1Y);

        // Option 2: CONTINUE
        double opt2Y = panelY + 128;
        if (savedLevel > 1) {
            gc.setFill(Color.rgb(0, 210, 255));
            gc.fillText("[ C ]   CONTINUE  (LV " + savedLevel + ")", centerX, opt2Y);
        } else {
            gc.setFill(Color.rgb(100, 110, 130));
            gc.fillText("[ C ]   CONTINUE  (NO SAVE)", centerX, opt2Y);
        }

        // 6. THÔNG TIN PHÍA DƯỚI
        double footerY = centerY + 160;

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
        gc.setFill(Color.rgb(160, 175, 200));
        gc.fillText("WASD / ARROW KEYS : Move    •    SPACE : Bomb", centerX, footerY);

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        gc.setFill(Color.rgb(255, 205, 0));
        gc.fillText("★ HIGH SCORE: " + highScore + " ★", centerX, footerY + 30);

        // Trả lại căn lề mặc định
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
