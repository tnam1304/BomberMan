package bomberman;

import bomberman.entities.Enemy;
import bomberman.entities.ChasingEnemy;
import bomberman.enums.EnemyType;

public class EnemyFactory {

    public static Enemy createEnemy(EnemyType type, int x, int y) {
        switch (type) {
            case NORMAL:
                return new Enemy(x, y);

            case CHASING:
                return new ChasingEnemy(x, y);

            default:
                throw new IllegalArgumentException(
                        "Enemy type không hợp lệ: " + type
                );
        }
    }
}