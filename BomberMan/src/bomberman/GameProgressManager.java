package bomberman;

import java.io.*;

public class GameProgressManager {
    private static final String FILE_NAME = "save_level.txt";

    // Ghi số level hiện tại vào file
    public static void saveProgress(int level) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            writer.write(String.valueOf(level));
        } catch (IOException e) {
            System.err.println("Lỗi lưu tiến trình game: " + e.getMessage());
        }
    }

    // Đọc level đã lưu từ file (trả về 1 nếu chưa có file lưu)
    public static int loadProgress() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return 1;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line != null) {
                return Integer.parseInt(line.trim());
            }
        } catch (Exception e) {
            System.err.println("Lỗi đọc tiến trình game: " + e.getMessage());
        }
        return 1;
    }
}