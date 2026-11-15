package com.eng1.goozers;

/**
 * keeps track of whether the game is paused
 * Main checks this before moving the player and ducks
 * the isPaused varaible is true so the game starts paused until the player presses Esc
 */
public class GameEngine {
    private boolean isPaused = true;

    /** tells Main whether the game is paused */
    public boolean isPaused() {
        return isPaused;
    }

    /** marks the game as paused so Main stops the game physics and movement updates */
    public void pauseGame() {
        isPaused = true;
    }

    /** marks the game as running so Main continues the game physics and movement updates */
    public void resumeGame() {
        isPaused = false;
    }
}