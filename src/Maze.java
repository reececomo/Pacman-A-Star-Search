/**
 * Write a description of class Maze here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */

import javax.swing.Timer; //timer
import java.awt.event.*; //actionlistener

public class Maze
{   

    private Status[][] grid;
    private Ghost[] currentGhosts;
    private PacMan pacman;
    private MazeViewer mazeviewer;
    
    private int nextScore = 200;
    private int score = 0;
    private int numLives = 3;
    private int panicCounter = 7;
    
    private int numDots = 0; //counted when maze created
    private int numEnergisers = 0; //counted when maze created
    
    public static enum Status{LEGAL, DEAD, DOT, ENERGISER, INVALID};

    public Maze(boolean[][] gameGrid, int[][] noDots, int[][] energiserPos, Ghost[] ghosts, PacMan pm)
    {
       /* a specified boolean 2D array gameGrid. A true value inidicates legal space, while a false means dead space.
        * 
        * an integer 2D array specify the legal tiles that have no dots. 
        * code should stop the construction if the noDots position is a dead space.
        * 
        * an integer 2D array specify the legal tiles that have energisers. 
        * code should stop the construction if the energiserPos position is a dead space or is a noDots position.
        * 
        * an array of Ghosts object, and
        * a PacMan object
        */
       
        int rows = gameGrid.length;
        int cols = gameGrid[0].length;
    
        grid = new Status[rows][cols];
       
        for (int i = 0; i<rows; i++){
            for (int j = 0; j < cols; j++){
                if (gameGrid[i][j]){
                    //dot by default
                    numDots++;
                    grid[i][j] = Status.DOT;
                } else {
                    //dead
                    grid[i][j] = Status.DEAD;
                }
            }
        }
        
        int x; int y;
        
        for (int i = 0; i < noDots.length; i++) {
            //this is a stupid way of doing this imo
            
            x = noDots[i][0];
            y = noDots[i][1];
                
            if(grid[x][y] == Status.DEAD){
                //error - close program
                throw new IllegalArgumentException("Error - No dot in dead position");
            } else {
                if(grid[x][y] == Status.DOT){
                    numDots --;
                }
                
                grid[x][y] = Status.LEGAL; 
            }
        }
        
        for (int i = 0; i < energiserPos.length; i++) {
            
            x = energiserPos[i][0];
            y = energiserPos[i][1];
                
            if(grid[x][y] == Status.DEAD){
                //error - close program
                throw new IllegalArgumentException("Error - Energiser in dead position");
            } else {
                if(grid[x][y] == Status.DOT){
                    numDots --;
                }
                grid[x][y] = Status.ENERGISER; 
                numEnergisers++;
            }
        }   
       
        currentGhosts = ghosts;
        pacman = pm;
    }
    
    public Status[][] getMap(){
        //returns the game map.
        return grid;
    }
    
    public void doStopDot() {
        mazeviewer.playDot(false); 
    }
        
    public void doDot() {
        //adds 10 to the player's current score.
        score+=10;
        numDots--;
        mazeviewer.playDot(true);
    }   
    
    public void doEnergiser() {
        score+=50;
        numEnergisers--;
        mazeviewer.playEnergiser();
    }    
    
    public void setMV(MazeViewer mv){
        mazeviewer = mv;
    }
    
    public void doPanicCountdown(){
        mazeviewer.doPanicCountdown(panicCounter);
    }
    
    public int getScore() {
        //returns the player's current score.
        return score;
    }
    
    public void setStatus(int i, int j, Status status){
        grid[i][j] = status;
    }
    
    public int getNumLives(){
        //returns the number of lives left.
        return numLives;
    }
    
    public Ghost[] getGhosts(){ 
        //returns a list of ghosts.
        return currentGhosts;
    }
    
    public PacMan getPacMan(){ 
        //returns the Pac-Man.
        return pacman;
    }
    
    public void setPanicCounter(int counter){ 
        //changes the value of the panic counter. 
        panicCounter = counter;
    }

    public boolean gameRun(){
        /*maintains the status of the game, moves the ghosts, and checks to see if the game is over. 
         * 
         * As we are only working on a single level game, the game is considered to be over:
         * If there is no Pac-Man life left (the player lose),
         * If the user has eaten all dots and all energisers (the player wins), or
         * If the user finished off all the ghosts (the player wins) 
         */
        
        for (int i = 0; i < currentGhosts.length; i++){
            currentGhosts[i].move(this);
        }
               
        // check if game is over
        if (getNumLives() == 0) {
            //game over
            mazeviewer.youLose();
            return false;
        }
        
        if (numDots == 0 && numEnergisers == 0){
            mazeviewer.youWin();
            return false;
        }
        
        return true; //what does this do??
    }

    public String toString(){
        /* that returns a String detailing the game status, 
         * including the number of dots/energisers left, 
         * the score of the player and the positions and orientations of all ghosts and Pac-Man. 
         * Note that in Java, for any class that implements a toString()
         * you can "print out" an object of this class using System.out.print(). 
         */
        String temp = "";
        temp += "Dots left: " + numDots;
        temp += "\n Energisers left: " + numEnergisers;
        temp += "\n Score: " + score; 
        
        for(int i = 0; i < currentGhosts.length ; i++){
            temp += "\n Ghost number " + i + " Position:" + currentGhosts[i].getPosition().toString();
        }
        
        temp += "\n Pacman Position:" + pacman.getPosition().toString();
        
        return temp;
        
    }
    
    public Status locationStatus(java.awt.Point location){
        if(location.x >= 0 && location.x < grid.length*MazeViewer.CELL_SIZE && location.y >= 0 && location.y < grid[0].length*MazeViewer.CELL_SIZE){
            return grid[location.x/MazeViewer.CELL_SIZE][location.y/MazeViewer.CELL_SIZE];
        } else {
            return Status.INVALID;
        }
    }
    
    public void doCollide(boolean panic, java.awt.Point position){
        
        if(!panic){
            numLives--;
            mazeviewer.doCollide();
            
        } else {
            score +=nextScore;
            mazeviewer.drawScoreText(nextScore, position);
            nextScore *=2;
            mazeviewer.playGhost();
        }
    }
    
    public void resetNextScore(){
        nextScore = 200;
    }
    
    /* 
     * Pac-Man must eat all dots in the maze in order to win. 
     * The small dots are worth 10 points each, and the large dots are worth 50 points each. 
     * 
     * Eating one of energisers will set all the ghosts into Ghost.PANIC mode, and set them to aimless wander. 
     * During this time, the ghosts become vunerable and can be eaten by the Pac-Man. 
     * A count-down counter will record the time remaining for the ghosts to be in frightened panic mode. 
     */
    
    
}
