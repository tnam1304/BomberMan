
package bomberman;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class HighScoreManager {

    private static final Path FILE_PATH = Path.of("highscore.txt");

    public static int loadHighScore() {
        try {
            if (Files.exists(FILE_PATH)) {
                String content = Files.readString(FILE_PATH).trim();
                return Integer.parseInt(content);
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Không thể đọc High Score: " + e.getMessage());
        }

        return 0;
    }

    public static void saveHighScore(int score) {
        try {
            Files.writeString(FILE_PATH, String.valueOf(score));
        } catch (IOException e) {
            System.err.println("Không thể lưu High Score: " + e.getMessage());
        }
    }
}
