package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;
import javafx.animation.AnimationTimer;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.Reflection;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PaneFireworks {
    private final AnimationTimer timer;
    private final Canvas canvas;
    private final List<Particle> particles = new ArrayList<>();
    private final Paint[] colors;
    private final Stage upperStage;
    private final Pane background;
    private final Background backgroundImageDark;
    private Background backgroundImageLight;
    private final StringBuilder winners;
    private final LocalModel localModel;
    private int countDownTillNextFirework = 40;
    private int numLeft = 800;

    public PaneFireworks(Pane back, Stage stage, LocalModel localModel) {
        colors = new Paint[181];
        colors[0] = new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.WHITE),
                new Stop(0.2, Color.hsb(59, 0.38, 1)),
                new Stop(0.6, Color.hsb(59, 0.38, 1, 0.1)),
                new Stop(1, Color.hsb(59, 0.38, 1, 0))
        );
        for (int h = 0; h < 360; h += 2) {
            colors[1 + (h / 2)] = new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.WHITE),
                    new Stop(0.2, Color.hsb(h, 1, 1)),
                    new Stop(0.6, Color.hsb(h, 1, 1, 0.1)),
                    new Stop(1, Color.hsb(h, 1, 1, 0))
            );
        }

        canvas = new Canvas(1024, 768);
        canvas.setBlendMode(BlendMode.ADD);
        canvas.setEffect(new Reflection(0, 0.4, 0.15, 0));

        winners = new StringBuilder();
        background = back;
        upperStage = stage;
        this.localModel = localModel;
        backgroundImageDark = background.getBackground();

        for(Node n : background.getChildren()){
            if(n instanceof BorderPane){
                backgroundImageLight = ((BorderPane) n).getBackground();
                break;
            }
        }

        timer = new AnimationTimer() {
            @Override public void handle(long now) {
                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.setFill(Color.rgb(0, 0, 0, 0.2));
                gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
                drawFireworks(gc);
                if (countDownTillNextFirework == 0 && numLeft >= 0) {
                    countDownTillNextFirework = 10 + (int) (Math.random() * 30);
                    fireParticle();
                }
                countDownTillNextFirework--;

                numLeft--;

                if(numLeft < 0){
                    PaneFireworks.this.stop();
                }

                Text text = new Text(winners.toString());
                text.setFont(gc.getFont());
                gc.setFill(Color.WHITE);

                double textWidth = text.getBoundsInLocal().getWidth();
                double textHeight = text.getBoundsInLocal().getHeight();

                gc.fillText(winners.toString(), (canvas.getWidth() - textWidth) / 2, (canvas.getHeight() + textHeight) / 2);
            }
        };

    }

    public void start() {
        for(Node n : background.getChildrenUnmodifiable()){
            n.setOpacity(0.15);
        }
        background.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, null)));

        for(Node n : background.getChildren()){
            if(n instanceof Pane){
                ((Pane) n).setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, null)));
            }
        }

        background.getChildren().add(canvas);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.setFont(new javafx.scene.text.Font(40));

        winners.append("Congratulations: ");
        for(String winner : localModel.getWinners()){
            winners.append(winner).append(", ");
        }
        if (!winners.isEmpty()) {
            winners.setLength(winners.length() - 2);
        }

        timer.start();
    }

    public void stop() {
        timer.stop();
        background.getChildren().remove(canvas);
        background.setBackground(backgroundImageDark);
        for(Node n : background.getChildren()){
            if(n instanceof Pane){
                ((Pane) n).setBackground(backgroundImageLight);
            }
        }

        System.out.println("Background restored");
        for(Node n : background.getChildrenUnmodifiable()){
            n.setOpacity(1);
        }

    }

    private void drawFireworks(GraphicsContext gc) {
        Iterator<Particle> iter = particles.iterator();
        List<Particle> newParticles = new ArrayList<>();
        while (iter.hasNext()) {
            Particle firework = iter.next();
            if (firework.update()) {
                iter.remove();
                if (firework.shouldExplodeChildren) {
                    if (firework.size == 9) {
                        explodeCircle(firework, newParticles);
                    } else if (firework.size == 8) {
                        explodeSmallCircle(firework, newParticles);
                    }
                }
            }
            firework.draw(gc);
        }
        particles.addAll(newParticles);
    }

    private void fireParticle() {
        particles.add(new Particle(
                canvas.getWidth() * 0.5, canvas.getHeight() + 10,
                Math.random() * 5 - 2.5, 0,
                0, 150 + Math.random() * 100,
                colors[0], 9,
                false, true, true));
    }

    private void explodeCircle(Particle firework, List<Particle> newParticles) {
        final int count = 20 + (int) (60 * Math.random());
        final boolean shouldExplodeChildren = Math.random() > 0.5;
        final double angle = (Math.PI * 2) / count;
        final int color = (int) (Math.random() * colors.length);
        for (int i = count; i > 0; i--) {
            double randomVelocity = 4 + Math.random() * 4;
            double particleAngle = i * angle;
            newParticles.add(
                    new Particle(
                            firework.posX, firework.posY,
                            Math.cos(particleAngle) * randomVelocity, Math.sin(particleAngle) * randomVelocity,
                            0, 0,
                            colors[color],
                            8,
                            true, shouldExplodeChildren, true));
        }
    }

    private void explodeSmallCircle(Particle firework, List<Particle> newParticles) {
        final double angle = (Math.PI * 2) / 12;
        for (int count = 12; count > 0; count--) {
            double randomVelocity = 2 + Math.random() * 2;
            double particleAngle = count * angle;
            newParticles.add(
                    new Particle(
                            firework.posX, firework.posY,
                            Math.cos(particleAngle) * randomVelocity, Math.sin(particleAngle) * randomVelocity,
                            0, 0,
                            firework.color,
                            4,
                            true, false, false));
        }
    }
}

class Particle {
    private static final double GRAVITY = 0.06;
    double alpha;
    final double easing;
    double fade;
    double posX;
    double posY;
    double velX;
    double velY;
    final double targetX;
    final double targetY;
    final Paint color;
    final int size;
    final boolean usePhysics;
    final boolean shouldExplodeChildren;
    final boolean hasTail;
    double lastPosX;
    double lastPosY;

    public Particle(double posX, double posY, double velX, double velY, double targetX, double targetY,
                    Paint color, int size, boolean usePhysics, boolean shouldExplodeChildren, boolean hasTail) {
        this.posX = posX;
        this.posY = posY;
        this.velX = velX;
        this.velY = velY;
        this.targetX = targetX;
        this.targetY = targetY;
        this.color = color;
        this.size = size;
        this.usePhysics = usePhysics;
        this.shouldExplodeChildren = shouldExplodeChildren;
        this.hasTail = hasTail;
        this.alpha = 1;
        this.easing = Math.random() * 0.02;
        this.fade = Math.random() * 0.1;
    }

    public boolean update() {
        lastPosX = posX;
        lastPosY = posY;
        if (this.usePhysics) { // On way down
            velY += GRAVITY;
            posY += velY;
            this.alpha -= this.fade; // Fade out particle
        } else { // On way up
            double distance = (targetY - posY);
            posY += distance * (0.03 + easing);
            alpha = Math.min(distance * distance * 0.00005, 1);
        }
        posX += velX;
        return alpha < 0.005;
    }

    public void draw(GraphicsContext context) {
        final double x = Math.round(posX);
        final double y = Math.round(posY);
        final double xVel = (x - lastPosX) * -5;
        final double yVel = (y - lastPosY) * -5;
        context.setGlobalAlpha(Math.random() * this.alpha);
        context.setFill(color);
        context.fillOval(x - size, y - size, size + size, size + size);
        if (hasTail) {
            context.setFill(Color.rgb(255, 255, 255, 0.3));
            context.fillPolygon(new double[]{posX + 1.5, posX + xVel, posX - 1.5},
                    new double[]{posY, posY + yVel, posY}, 3);
        }
    }
}
