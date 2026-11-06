
package bomberman;

public class GameConfig {

    // Kích thước logic của mỗi ô
    public static final int TILE_SIZE = 32;

    // Kích thước bản đồ thực tế
    public static final int MAP_COLS = 13;
    public static final int MAP_ROWS = 11;

    // Kích thước cửa sổ
    public static final int WIDTH = 1440;
    public static final int HEIGHT = 900;

    // Tỷ lệ phóng to đồ họa
    public static final int RENDER_SCALE = 2;

    // Kích thước vùng bản đồ sau khi phóng to
    public static final int BOARD_WIDTH =
            MAP_COLS * TILE_SIZE * RENDER_SCALE;

    public static final int BOARD_HEIGHT =
            MAP_ROWS * TILE_SIZE * RENDER_SCALE;

    // Vị trí vùng bản đồ
    public static final int BOARD_X = 40;
    public static final int BOARD_Y = 150;

    // Các thông số gameplay giữ nguyên
    public static final int PLAYER_SPEED = 3;
    public static final int BOMB_DELAY = 2000;
    public static final int FLAME_DURATION = 500;
}
