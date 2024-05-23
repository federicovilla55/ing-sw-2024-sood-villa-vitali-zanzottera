package it.polimi.ingsw.gc19.View.GUI;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class FireworksApp extends Application {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Group root = new Group();
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        scene.setFill(Color.BLACK); // Example background color

        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.TRANSPARENT);

        root.getChildren().add(canvas);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Fireworks");
        primaryStage.show();

        List<Particle> particles = new ArrayList<>();
        Random random = new Random();

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Clear the canvas with transparent color
                gc.clearRect(0, 0, WIDTH, HEIGHT);

                // Create new particles randomly
                if (random.nextInt(10) == 0) {
                    double x = random.nextDouble() * WIDTH;
                    double y = random.nextDouble() * HEIGHT;
                    Color color = Color.hsb(random.nextDouble() * 360, 1, 1);
                    for (int i = 0; i < 100; i++) {
                        particles.add(new Particle(x, y, color));
                    }
                }

                // Update and draw particles
                Iterator<Particle> iterator = particles.iterator();
                while (iterator.hasNext()) {
                    Particle particle = iterator.next();
                    particle.update();
                    particle.draw(gc);
                    if (particle.isDead()) {
                        iterator.remove();
                    }
                }
            }
        };
        timer.start();
    }

    private static class Particle {
        private double x, y;
        private double vx, vy;
        private double life;
        private final Color color;

        Particle(double x, double y, Color color) {
            this.x = x;
            this.y = y;
            this.color = color;
            Random random = new Random();
            this.vx = (random.nextDouble() * 2 - 1) * 0.5;  // Slower velocity
            this.vy = (random.nextDouble() * 2 - 1) * 0.5;  // Slower velocity
            this.life = random.nextDouble() * 300 + 60;  // Longer lifespan
        }

        void update() {
            x += vx;
            y += vy;
            life--;
        }

        void draw(GraphicsContext gc) {
            gc.setFill(color);
            gc.fillOval(x, y, 4, 4);  // Increase the size of particles
        }

        boolean isDead() {
            return life <= 0;
        }
    }
}
