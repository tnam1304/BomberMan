import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Portal extends Tile {
    private boolean unlocked = false;

    public Portal(int x, int y) {
        super(x, y, true); // Đi qua được
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void enter() {
        if (unlocked) {
            System.out.println("Chuyển sang Level tiếp theo!");
        }
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.setFill(unlocked ? Color.PURPLE : Color.DARKPURPLE);
        gc.fillRect(x * 32 + 4, y * 32 + 4, 24, 24);
    }
}