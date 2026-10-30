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

    private Direction previousDirection = Direction.NONE;
    private final int ALIGN_TOLERANCE = 12;
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

        // Hỗ trợ căn chỉnh khi đổi hướng
        if (direction != previousDirection) {

            if ((previousDirection == Direction.UP ||
                    previousDirection == Direction.DOWN) &&
                    (direction == Direction.LEFT ||
                            direction == Direction.RIGHT)) {

                alignY(entities);
            }

            else if ((previousDirection == Direction.LEFT ||
                    previousDirection == Direction.RIGHT) &&
                    (direction == Direction.UP ||
                            direction == Direction.DOWN)) {

                alignX(entities);
            }

            previousDirection = direction;
        }

        int nextX = x;
        int nextY = y;

        switch (direction) {
            case LEFT -> nextX -= speed;
            case RIGHT -> nextX += speed;
            case UP -> nextY -= speed;
            case DOWN -> nextY += speed;
            case NONE -> {}
        }

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

    private void alignX(List<Entity> entities) {

        int targetX = Math.round((float) x / 32) * 32;

        if (Math.abs(targetX - x) <= ALIGN_TOLERANCE &&
                !checkCollision(targetX, y, entities)) {

            x = targetX;
        }
    }

    private void alignY(List<Entity> entities) {

        int targetY = Math.round((float) y / 32) * 32;

        if (Math.abs(targetY - y) <= ALIGN_TOLERANCE &&
                !checkCollision(x, targetY, entities)) {

            y = targetY;
        }
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