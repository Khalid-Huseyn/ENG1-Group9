package com.eng1.goozers;

public class GameEngine {
    private boolean isPaused = true;

    public boolean isPaused(){
        return isPaused;
    }

    public void pauseGame() {
        isPaused = true;
    }

    public void resumeGame(){
        isPaused = false;
    }
}