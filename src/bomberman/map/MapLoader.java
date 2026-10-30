
package bomberman.map;

import bomberman.entities.*;
import bomberman.GameConfig;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class MapLoader {

    public static List<Entity> loadMap(String filePath) {

        List<Entity> entities = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            // Đọc thông tin Level, Rows, Cols
            String firstLine = br.readLine();

            if (firstLine == null) {
                throw new IllegalArgumentException("File map rỗng!");
            }

            String[] info = firstLine.trim().split("\\s+");

            int level = Integer.parseInt(info[0]);
            int rows = Integer.parseInt(info[1]);
            int cols = Integer.parseInt(info[2]);

            System.out.println("Loading Level: " + level);

            // Đọc nội dung bản đồ
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

                        case '#':
                            entities.add(new Wall(pixelX, pixelY, null));
                            break;

                        case '*':
                            entities.add(new Brick(pixelX, pixelY, null));
                            break;

                        case 'p':
                            // Vị trí xuất hiện Bomber
                            break;

                        case ' ':
                            // Ô trống
                            break;

                        default:
                            System.err.println(
                                    "Ký tự không hợp lệ: " + symbol
                            );
                    }
                }
            }

            System.out.println("Đã tải map thành công!");

        } catch (Exception e) {
            System.err.println("Lỗi đọc map: " + e.getMessage());
            e.printStackTrace();
        }

        return entities;
    }
}
