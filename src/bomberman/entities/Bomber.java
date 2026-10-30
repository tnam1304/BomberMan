package bomberman.entities;

import bomberman.enums.Direction;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Set;

public class Bomber extends DynamicEntity {

    public Bomber(int x, int y, Image img) {
        super(x, y, img, 2);
    }

    public void handleInput(Set<KeyCode> input) {
        if (input.contains(KeyCode.LEFT) || input.contains(KeyCode.A)) {
            direction = Direction.LEFT;
        } else if (input.contains(KeyCode.RIGHT) || input.contains(KeyCode.D)) {
            direction = Direction.RIGHT;
        } else if (input.contains(KeyCode.UP) || input.contains(KeyCode.W)) {
            direction = Direction.UP;
        } else if (input.contains(KeyCode.DOWN) || input.contains(KeyCode.S)) {
            direction = Direction.DOWN;
        } else {
            direction = Direction.NONE;
        }
    }

    // Nhận danh sách vật thể để kiểm tra va chạm
    public void update(List<Entity> entities) {
        int nextX = x;
        int nextY = y;

        switch (direction) {
            case LEFT -> nextX -= speed;
            case RIGHT -> nextX += speed;
            case UP -> nextY -= speed;
            case DOWN -> nextY += speed;
            case NONE -> {}
        }

        // Nếu vị trí tiếp theo KHÔNG bị vướng tường/gạch thì mới cho đi
        if (!checkCollision(nextX, nextY, entities)) {
            x = nextX;
            y = nextY;
        }
    }

    private boolean checkCollision(int nextX, int nextY, List<Entity> entities) {
        Rectangle2D nextBounds = new Rectangle2D(nextX, nextY, 32, 32);
        for (Entity entity : entities) {
            if (entity instanceof Tile tile && tile.isCollideable()) {
                if (nextBounds.intersects(entity.getBoundary())) {
                    return true; // Có va chạm
                }
            }
        }
        return false;
    }

    @Override
    public void move() {}

    @Override
    public void update() {}

    @Override
    public void render(GraphicsContext gc) {
        if (img != null) {
            gc.drawImage(img, x, y);
        } else {
            gc.setFill(Color.BLACK);
            gc.fillRect(x, y, 32, 32);
        }
    }
}