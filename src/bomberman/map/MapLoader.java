package bomberman.map;

import bomberman.entities.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import bomberman.GameConfig;

public class MapLoader {
    public static List<Entity> loadMap(String filePath) {
        List<Entity> entities = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // Đọc dòng thông số đầu: Level Row Col
            int row = 0;
            while ((line = br.readLine()) != null) {
                for (int col = 0; col < line.length(); col++) {
                    char symbol = line.charAt(col);
                    int pixelX = col * GameConfig.TILE_SIZE;
                    int pixelY = row * GameConfig.TILE_SIZE;

                    if (symbol == '#') {
                        entities.add(new Wall(pixelX, pixelY, null));
                    } else if (symbol == '*') {
                        entities.add(new Brick(pixelX, pixelY, null));
                    }
                }
                row++;
            }
        } catch (Exception e) {
            System.err.println("Không tìm thấy file map: " + filePath);
        }
        return entities;
    }
}