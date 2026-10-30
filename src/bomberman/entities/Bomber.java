
package bomberman.entities;

import bomberman.GameConfig;
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

    private static final int ALIGN_TOLERANCE = 12;

    private static final int MAX_LIVES = 3;
    private static final long INVINCIBLE_TIME = 2_000_000_000L;

    private int lives = MAX_LIVES;
    private long invincibleUntil = 0;

    private int maxBombs = 1;
    private int flameRange = 1;

    public Bomber(int x, int y, Image img) {
        super(x, y, img, GameConfig.PLAYER_SPEED);
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

    public void update(List<Entity> entities) {

        if (direction == Direction.NONE) {
            return;
        }

        Direction moveDirection = direction;

        if (isVertical(previousDirection) && isHorizontal(direction)) {

            if (alignY(entities)) {
                previousDirection = direction;
            } else {
                moveDirection = previousDirection;
            }

        } else if (isHorizontal(previousDirection) && isVertical(direction)) {

            if (alignX(entities)) {
                previousDirection = direction;
            } else {
                moveDirection = previousDirection;
            }

        } else {
            previousDirection = direction;
        }

        int nextX = x;
        int nextY = y;

        switch (moveDirection) {
            case LEFT -> nextX -= speed;
            case RIGHT -> nextX += speed;
            case UP -> nextY -= speed;
            case DOWN -> nextY += speed;
            case NONE -> {
            }
        }

        if (!checkCollision(nextX, nextY, entities)) {
            x = nextX;
            y = nextY;
        }
    }

    private boolean isHorizontal(Direction direction) {
        return direction == Direction.LEFT
                || direction == Direction.RIGHT;
    }

    private boolean isVertical(Direction direction) {
        return direction == Direction.UP
                || direction == Direction.DOWN;
    }

    private boolean checkCollision(
            int nextX,
            int nextY,
            List<Entity> entities
    ) {

        int size = GameConfig.TILE_SIZE;

        Rectangle2D nextBounds = new Rectangle2D(
                nextX, nextY, size, size
        );

        for (Entity entity : entities) {

            if (entity instanceof Tile tile && tile.isCollideable()) {

                if (nextBounds.intersects(entity.getBoundary())) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean alignX(List<Entity> entities) {

        int size = GameConfig.TILE_SIZE;
        int targetX = Math.round((float) x / size) * size;

        int tolerance = Math.max(ALIGN_TOLERANCE, speed * 2);

        if (Math.abs(targetX - x) <= tolerance
                && !checkCollision(targetX, y, entities)) {

            x = targetX;
            return true;
        }

        return false;
    }

    private boolean alignY(List<Entity> entities) {

        int size = GameConfig.TILE_SIZE;
        int targetY = Math.round((float) y / size) * size;

        int tolerance = Math.max(ALIGN_TOLERANCE, speed * 2);

        if (Math.abs(targetY - y) <= tolerance
                && !checkCollision(x, targetY, entities)) {

            y = targetY;
            return true;
        }

        return false;
    }

    // Nhận sát thương
    public boolean hurt() {

        long currentTime = System.nanoTime();

        // Đang bất tử thì không nhận thêm sát thương
        if (isInvulnerable() || !isAlive()) {
            return false;
        }

        lives--;

        if (lives <= 0) {
            lives = 0;
            setAlive(false);
        } else {
            invincibleUntil = currentTime + INVINCIBLE_TIME;
        }

        return true;
    }

    public boolean isInvulnerable() {
        return System.nanoTime() < invincibleUntil;
    }

    public int getLives() {
        return lives;
    }

    // Nhặt vật phẩm
    public void collectItem(Item.ItemType type) {

        switch (type) {
            case BOMB_UP -> maxBombs++;
            case FLAME_UP -> flameRange++;
            case SPEED_UP -> speed++;
        }
    }

    public int getMaxBombs() {
        return maxBombs;
    }

    public int getFlameRange() {
        return flameRange;
    }

    public int getSpeed() {
        return speed;
    }

    @Override
    public void move() {
    }

    @Override
    public void update() {
    }

    @Override
    public void render(GraphicsContext gc) {

        // Hiệu ứng nhấp nháy khi bất tử
        if (isInvulnerable()) {

            long elapsed = System.nanoTime();

            if ((elapsed / 150_000_000L) % 2 == 0) {
                return;
            }
        }

        if (img != null) {
            gc.drawImage(img, x, y);
        } else {
            gc.setFill(Color.BLACK);
            gc.fillRect(
                    x, y,
                    GameConfig.TILE_SIZE,
                    GameConfig.TILE_SIZE
            );
        }
    }
}
