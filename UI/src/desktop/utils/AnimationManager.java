package desktop.utils;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;

public class AnimationManager {

    private boolean animationsEnabled = true; // Default to true, but can be controlled externally

    // Method to toggle the animation state
    public void setAnimationsEnabled(boolean enabled) {
        this.animationsEnabled = enabled;
    }

    public boolean isAnimationsEnabled() {
        return animationsEnabled;
    }

    // Fade-in animation
    public void playFadeInAnimation(Node node) {
        if (!animationsEnabled) return;

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.7), node);  // Reduced to fit 2-second total
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setCycleCount(1);
        fadeIn.play();
    }

    // Scale animation (zoom-in)
    public void playScaleAnimation(Node node) {
        if (!animationsEnabled) return;

        ScaleTransition scale = new ScaleTransition(Duration.seconds(0.7), node);  // Reduced to fit 2-second total
        scale.setFromX(0.5);
        scale.setFromY(0.5);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.setCycleCount(1);
        scale.play();
    }

    // New Slide-in animation from the left
    public void playSlideInAnimation(Node node) {
        if (!animationsEnabled) return;

        TranslateTransition slideIn = new TranslateTransition(Duration.seconds(0.6), node);  // Add slide-in effect
        slideIn.setFromX(-200);  // Starts 200 pixels to the left
        slideIn.setToX(0);       // Ends at the normal position
        slideIn.setCycleCount(1);
        slideIn.play();
    }

    // Play all animations on sheet load
    public void playAnimationsOnLoad(BorderPane mainPane) {
        if (animationsEnabled) {
            // Use a ParallelTransition to play all three animations together
            ParallelTransition animations = new ParallelTransition(
                    createFadeIn(mainPane),
                    createScale(mainPane),
                    createSlideIn(mainPane)
            );
            animations.play();
        }
    }

    // Helper methods to create individual animations (can be used in combination)
    private FadeTransition createFadeIn(Node node) {
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.7), node);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        return fadeIn;
    }

    private ScaleTransition createScale(Node node) {
        ScaleTransition scale = new ScaleTransition(Duration.seconds(0.7), node);
        scale.setFromX(0.5);
        scale.setFromY(0.5);
        scale.setToX(1.0);
        scale.setToY(1.0);
        return scale;
    }

    private TranslateTransition createSlideIn(Node node) {
        TranslateTransition slideIn = new TranslateTransition(Duration.seconds(0.6), node);
        slideIn.setFromX(-200);  // Starts from left outside of the window
        slideIn.setToX(0);       // Slides into the normal position
        return slideIn;
    }
}
