
package bomberman.map;
import bomberman.GameAssets;
import bomberman.entities.*;
import bomberman.GameConfig;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapLoader {

    public static class EnemySpawn {
        public final int x;
        public final int y;
        public final boolean chasing;

        public EnemySpawn(int x, int y, boolean chasing) {
            this.x = x;
            this.y = y;
            this.chasing = chasing;
        }
    }

    public static class LevelData {
        public final int level;
        public final List<Entity> entities;
        public final List<EnemySpawn> enemySpawns;
        public final int playerX;
        public final int playerY;
        public final int exitX;
        public final int exitY;

        public LevelData(
                int level,
                List<Entity> entities,
                List<EnemySpawn> enemySpawns,
                int playerX,
                int playerY,
                int exitX,
                int exitY
        ) {
            this.level = level;
            this.entities = entities;
            this.enemySpawns = enemySpawns;
            this.playerX = playerX;
            this.playerY = playerY;
            this.exitX = exitX;
            this.exitY = exitY;
        }
    }

    public static LevelData loadMap(String filePath) {

        List<Entity> entities = new ArrayList<>();
        List<EnemySpawn> enemySpawns = new ArrayList<>();

        int level = 1;
        int playerX = -1;
        int playerY = -1;
        int exitX = -1;
        int exitY = -1;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String firstLine;

            do {
                firstLine = br.readLine();
            } while (firstLine != null && firstLine.isBlank());

            if (firstLine == null) {
                throw new IllegalArgumentException("File map rỗng!");
            }

            Matcher matcher = Pattern.compile("\\d+").matcher(firstLine);
            List<Integer> numbers = new ArrayList<>();

            while (matcher.find()) {
                numbers.add(Integer.parseInt(matcher.group()));
            }

            if (numbers.size() < 3) {
                throw new IllegalArgumentException(
                        "Dòng đầu phải có dạng: Level 1 11 13"
                );
            }

            level = numbers.get(0);
            int rows = numbers.get(1);
            int cols = numbers.get(2);

            for (int row = 0; row < rows; row++) {

                String line = br.readLine();

                if (line == null || line.length() != cols) {
                    throw new IllegalArgumentException(
                            "Kích thước map không hợp lệ tại hàng " + row
                    );
                }

                for (int col = 0; col < cols; col++) {

                    char symbol = line.charAt(col);

                    int pixelX = col * GameConfig.TILE_SIZE;
                    int pixelY = row * GameConfig.TILE_SIZE;

                    switch (symbol) {

                       case '#' ->
                       entities.add(new Wall(pixelX, pixelY, GameAssets.WALL));
                       
                       case '*' ->
                       entities.add(new Brick(pixelX, pixelY, GameAssets.BRICK));

                        case 'P', 'p' -> {
                            playerX = pixelX;
                            playerY = pixelY;
                        }

                        case 'E' ->
                                enemySpawns.add(
                                        new EnemySpawn(pixelX, pixelY, false)
                                );

                        case 'C' ->
                                enemySpawns.add(
                                        new EnemySpawn(pixelX, pixelY, true)
                                );

                        case 'X' -> {
                            exitX = pixelX;
                            exitY = pixelY;
                        }

                        case ' ' -> {
                            // Ô trống
                        }

                        default -> throw new IllegalArgumentException(
                                "Ký tự không hợp lệ: " + symbol
                        );
                    }
                }
            }

            if (playerX < 0 || playerY < 0) {
                throw new IllegalArgumentException(
                        "Map chưa có vị trí Player (P)!"
                );
            }

            if (exitX < 0 || exitY < 0) {
                throw new IllegalArgumentException(
                        "Map chưa có cửa thoát (X)!"
                );
            }

            System.out.println("Đã tải Level " + level);

            return new LevelData(
                    level,
                    entities,
                    enemySpawns,
                    playerX,
                    playerY,
                    exitX,
                    exitY
            );

        } catch (Exception e) {
            System.err.println("Lỗi đọc map: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
