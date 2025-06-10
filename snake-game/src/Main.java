// HEI Snake Game - Java Console Implementation
// Clean Code principles: DRY, KISS, YAGNI, Naming, Factory, Builder, State, Strategy

import java.util.*;

class Point {
    int x, y;
    Point(int x, int y) { this.x = x; this.y = y; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return x == point.x && y == point.y;
    }

    @Override
    public int hashCode() { return Objects.hash(x, y); }
}

enum Direction {
    UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);
    final int dx, dy;
    Direction(int dx, int dy) { this.dx = dx; this.dy = dy; }
}

interface MoveStrategy {
    Point computeNextPosition(LinkedList<Point> body, Direction direction);
}

class DefaultMoveStrategy implements MoveStrategy {
    public Point computeNextPosition(LinkedList<Point> body, Direction direction) {
        Point head = body.getFirst();
        return new Point(head.x + direction.dx, head.y + direction.dy);
    }
}

class Snake {
    private LinkedList<Point> body;
    private Direction direction;
    private MoveStrategy moveStrategy;

    public Snake(LinkedList<Point> body, Direction direction, MoveStrategy strategy) {
        this.body = body;
        this.direction = direction;
        this.moveStrategy = strategy;
    }

    public LinkedList<Point> getBody() { return body; }
    public Direction getDirection() { return direction; }
    public void setDirection(Direction direction) { this.direction = direction; }

    public Point move(boolean grow) {
        Point next = moveStrategy.computeNextPosition(body, direction);
        body.addFirst(next);
        if (!grow) body.removeLast();
        return next;
    }

    public boolean collidesWith(Point point) {
        return body.contains(point);
    }
}

class FoodFactory {
    public static Point generateFood(int gridSize, Set<Point> occupied) {
        Random rand = new Random();
        Point food;
        do {
            food = new Point(rand.nextInt(gridSize), rand.nextInt(gridSize));
        } while (occupied.contains(food));
        return food;
    }
}

class SnakeBuilder {
    public static Snake buildInitialSnake() {
        LinkedList<Point> body = new LinkedList<>();
        body.add(new Point(2, 2));
        body.add(new Point(1, 2));
        body.add(new Point(0, 2));
        return new Snake(body, Direction.RIGHT, new DefaultMoveStrategy());
    }
}

interface GameState {
    void tick(Game game);
}

class MenuState implements GameState {
    public void tick(Game game) {
        System.out.println("\nBienvenue dans HEI Snake!\nAppuyez sur Entrée pour commencer...");
        new Scanner(System.in).nextLine();
        game.setState(new RunningState());
    }
}

class RunningState implements GameState {
    public void tick(Game game) {
        game.render();
        game.readDirection();
        game.update();
    }
}

class GameOverState implements GameState {
    public void tick(Game game) {
        System.out.println("\nGame Over! Score: " + game.getScore());
        System.out.println("Appuyez sur Entrée pour quitter...");
        new Scanner(System.in).nextLine();
        System.exit(0);
    }
}

class Game {
    private static final int GRID_SIZE = 10;
    private GameState state;
    private Snake snake;
    private Point food;
    private int score = 0;

    public Game() {
        this.state = new MenuState();
    }

    public void setState(GameState state) { this.state = state; }
    public int getScore() { return score; }

    public void start() {
        this.snake = SnakeBuilder.buildInitialSnake();
        this.food = FoodFactory.generateFood(GRID_SIZE, new HashSet<>(snake.getBody()));
        while (true) {
            state.tick(this);
        }
    }

    public void render() {
        char[][] grid = new char[GRID_SIZE][GRID_SIZE];
        for (char[] row : grid) Arrays.fill(row, '.');

        for (Point p : snake.getBody()) grid[p.y][p.x] = '*';
        grid[food.y][food.x] = '@';

        for (char[] row : grid) {
            for (char c : row) System.out.print(c + " ");
            System.out.println();
        }
        System.out.println("Score: " + score);
    }

    public void readDirection() {
        System.out.print("Direction (WASD): ");
        char input = new Scanner(System.in).nextLine().toUpperCase().charAt(0);
        Direction newDirection = switch (input) {
            case 'W' -> Direction.UP;
            case 'S' -> Direction.DOWN;
            case 'A' -> Direction.LEFT;
            case 'D' -> Direction.RIGHT;
            default -> snake.getDirection();
        };
        snake.setDirection(newDirection);
    }

    public void update() {
        Point next = snake.move(food != null && snake.getBody().getFirst().equals(food));
        if (food.equals(next)) {
            score++;
            food = FoodFactory.generateFood(GRID_SIZE, new HashSet<>(snake.getBody()));
        }

        // Collision: wall or self
        if (next.x < 0 || next.x >= GRID_SIZE || next.y < 0 || next.y >= GRID_SIZE ||
                snake.getBody().subList(1, snake.getBody().size()).contains(next)) {
            setState(new GameOverState());
        }
    }
}

public class Main {
    public static void main(String[] args) {
        new Game().start();
    }
}
