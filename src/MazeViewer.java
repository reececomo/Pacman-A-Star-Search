
/**
 * Write a description of class MazeViewer here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */

import java.awt.Shape;
import java.awt.geom.*;
import javax.swing.Timer; //timer
import java.awt.event.*; //actionlistener
import java.awt.Font; //fonts
import java.io.*;

public class MazeViewer
{

    private Timer gameTimer;
    private Timer deathTimer;
    private int deathAngle;
    
    private int frame; //number of frames (time)
    private int panicStopFrame = 10^100; //frame to stop panic
    private int switchFrame = -1; //frame to switch between chase and scatter
    private boolean gameOver;
    
    java.awt.Point previousPacmanPosition;
    
    private Maze maze;
    private Maze.Status[][] statusgrid;
    private PacMan pacman;
    private Ghost.Orientation nextOrientation;
        
    private Ghost[] ghosts;

    private Canvas c;
    private static int[] tilepos;
    public static final int CELL_SIZE = 16; //16 or 32 
    private static final int PACMAN_SIZE = (int) (CELL_SIZE * 1.5); //diameter
    
    private static final java.awt.Color PacmanColour = new java.awt.Color(255,255,0);
    private static final java.awt.Color BackgroundColour = new java.awt.Color(0,10,40);
    private static final java.awt.Color OutlineColour = new java.awt.Color(10,40,200);
    
    public static Font defaultFont = new Font ("Joystix",Font.PLAIN,(int) (CELL_SIZE*1.5));
    public static Font scoreFont = new Font ("Arial",Font.PLAIN,(int) (CELL_SIZE*.8));
    
    private int rows;
    private int cols;
    
    private ScoreText[] scoretext;
    private int scoreCount = 0;
    
    private Sounds myDotSound;

    public MazeViewer(Maze themaze)
    {
       /*
        * builds a MazeViewer for the specified maze,
        * which displays the tile-based game maze with dots, energisers, 
        * Pac-Man and the ghost in their correct positions and orientations. 
        * It also shows the current score and the number of lives left. 
        */

        maze = themaze;
        statusgrid = maze.getMap();
        
        pacman = maze.getPacMan();
        nextOrientation =  pacman.getOrientation();
        
        ghosts = maze.getGhosts();
        
        rows = statusgrid.length;
        cols = statusgrid[0].length;
        
        c = new Canvas(rows*CELL_SIZE, cols*CELL_SIZE, pacman, this);  
        
        //drawEverything();
        drawMaze();
        c.setForegroundColour(new java.awt.Color(255,255,255));
        c.setFont(defaultFont);
        c.drawString("Ready?", (int) (rows*CELL_SIZE/2-CELL_SIZE*3.1), cols*CELL_SIZE/2);
        drawScore();
        
        gameTimer = new Timer(15, nextFrame);
        deathTimer = new Timer(10, drawPacmanDeath);
        
        gameTimer.setInitialDelay(200); //between drawing everything and letting pacman move
        maze.setMV(this);
        gameOver = true;
        
        scoretext = new ScoreText[ghosts.length];
        
        //sounds!
        
        new Sounds(Sounds.Sound.MUSIC_OPENING, true);
        myDotSound = new Sounds(Sounds.Sound.EAT_DOT, false);
        
    }
    
    private void eraseScreen(){
        c.setForegroundColour(new java.awt.Color(0,0,0));
        c.fill(new Rectangle2D.Double(0, 0, CELL_SIZE*rows, CELL_SIZE*cols));
    }

    public void keypress(){
        if(!gameTimer.isRunning() && !(gameOver) && !deathTimer.isRunning()){
            drawEverything(); //redraw maze - remove "ready?"
            gameTimer.start(); //start timer
        }
    }
    
    public void animate()
    {
        //displays the game until the game is over   
        //keyboard press - start timer
        //timer
        //gameTimer.start(); 
        frame = 0;
        gameOver = false;

    }
    
     ActionListener nextFrame = new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
            
            frame++;
            if (frame == panicStopFrame) {
                Ghost.setPanic(maze.getGhosts(), false);
                maze.resetNextScore();
                panicStopFrame = 10^100;
            }
            
            if (switchFrame < 0) switchFrame = 7*1000/gameTimer.getDelay(); //setup
            
            if (frame >= switchFrame){
                if (ghosts[0].getMode() == Ghost.Mode.SCATTER){
                    
                    switchFrame = frame + 7*1000/gameTimer.getDelay();
                    
                    for(int i = 0; i < ghosts.length; i++)
                    ghosts[i].setMode(Ghost.Mode.CHASE, Ghost.Target.PACMAN);
                    
                } else if (ghosts[0].getMode() == Ghost.Mode.CHASE){
                    
                    switchFrame = frame + 20*1000/gameTimer.getDelay();
                    
                    for(int i = 0; i < ghosts.length; i++)
                    ghosts[i].setMode(Ghost.Mode.SCATTER, Ghost.Target.SCATTER);
                    
                }
            }
                
            eraseGhosts(); //redraw where ghosts were
            erasePacman(); //redraw where pacman was
            drawMazeDots(); //redraw dots
            
            maze.gameRun(); //move ghosts & check for winning conditions
            pacman.doMove(maze); //move pacman
            
            drawGhosts(); //draw ghosts
            drawPacman(22.5*Math.cos(frame/5)-22.5); //draw pacman
            
            drawScoreText();
            
            drawScore(); //draws score and lives
            
            if(pacman.isDemo())drawDemoText();
            
        }
    };
    
    public void youWin(){
        gameTimer.stop();
        c.setForegroundColour(new java.awt.Color(255,255,255));
        c.drawString("You Win!", (int) (rows*CELL_SIZE/2-CELL_SIZE*3.5), (int) (cols*CELL_SIZE/2)); //high score font
        gameOver = true;
        myDotSound.loop(false);
        new Sounds(Sounds.Sound.MUSIC_INTERMISSION, true);
        
    }
    
    public void youLose(){
        gameTimer.stop();
        c.setForegroundColour(new java.awt.Color(255,255,255));
        c.drawString("You Lose!", (int) (rows*CELL_SIZE/2-CELL_SIZE*4), (int) (cols*CELL_SIZE/2)); //high score font
        gameOver = true;
        myDotSound.loop(false);
        new Sounds(Sounds.Sound.MUSIC_INTERMISSION, true);
        
    }
    
    public void drawScoreText(int score, java.awt.Point position){//external - create new
        
        //frame + 1000/gameTimer.getDelay();
        scoretext[scoreCount] = new ScoreText(score, position, frame);
        scoreCount++;
        if(scoreCount > scoretext.length-1) scoreCount = 0; //only 4 items ever
    }
    
    private void drawScoreText(){ //draw current
        for (int i = 0; i < scoreCount; i++){
            c.setFont(scoreFont);
            if(scoretext[i].canView(frame)){
                c.setForegroundColour(new java.awt.Color(80,128,185));
                c.drawString(scoretext[i].getScore()+"", scoretext[i].getPosition().x, scoretext[i].getPosition().y);
            } else if (scoretext[i].doErase(frame)) {
                c.setForegroundColour(BackgroundColour);
                c.drawString(scoretext[i].getScore()+"", scoretext[i].getPosition().x, scoretext[i].getPosition().y);
            }

            c.setFont(defaultFont);
        }
    }
    
    private void drawDemoText(){
        
        c.setForegroundColour(new java.awt.Color(255,255,255));

        c.drawString("Demo Mode", (int) (rows*CELL_SIZE/2-CELL_SIZE*4.7), cols*CELL_SIZE-CELL_SIZE*1);
        
    }
    
    private void drawScore(){
        
        c.setForegroundColour(BackgroundColour);
        c.fill(new Rectangle2D.Double(CELL_SIZE, CELL_SIZE, CELL_SIZE*rows, CELL_SIZE*2)); //hide previous score
        c.fill(new Rectangle2D.Double(0, cols*CELL_SIZE-CELL_SIZE*2, CELL_SIZE*rows, CELL_SIZE*2)); //hide previous score & demo text      
        c.setForegroundColour(new java.awt.Color(255,255,255));
        c.drawString(maze.getScore()+"", CELL_SIZE*2, (int) (CELL_SIZE*2.5)); //new score
        c.drawString("HIGH SCORE", (int) (rows*CELL_SIZE/2-CELL_SIZE*5), (int) (CELL_SIZE*1)); //high score font
        c.drawString(getHighscore()+"", (int) (rows*CELL_SIZE/2-CELL_SIZE*2), (int) (CELL_SIZE*2.5)); //new score
        
        if(getHighscore() < maze.getScore()){
            saveHighscore(maze.getScore());
        }
        
        c.setForegroundColour(PacmanColour);
        for(int i = 1; i < maze.getNumLives(); i++){ //draw lives
            c.fill(new Arc2D.Double(CELL_SIZE*2*i-CELL_SIZE, cols*CELL_SIZE-CELL_SIZE*2, PACMAN_SIZE, PACMAN_SIZE, 225, 270, Arc2D.PIE));
        }
    }
    
    private void drawEverything(){ //initial setup
        drawMaze();
        drawPacman(0);
        drawGhosts();
    }
    
    private void erasePacman(){
        c.erase(new Ellipse2D.Double(previousPacmanPosition.x-PACMAN_SIZE/2-2, previousPacmanPosition.y-PACMAN_SIZE/2-2, PACMAN_SIZE+4, PACMAN_SIZE+4));
    }
    
    private void eraseGhosts(){
        
        java.awt.Point position;
        
        for (int i = 0; i < ghosts.length; i++){
            position = ghosts[i].getPosition();
            
            c.erase(new Ellipse2D.Double(position.x-PACMAN_SIZE/2-2, position.y-PACMAN_SIZE/2-2, PACMAN_SIZE+4, PACMAN_SIZE+4)); //top circle
            c.erase(new Rectangle2D.Double(position.x-PACMAN_SIZE/2, position.y, PACMAN_SIZE, PACMAN_SIZE*0.6)); //bottom rectangle

        }
    }
    
    public void doPanicCountdown(int timer){
    
        panicStopFrame = frame+timer*1000/gameTimer.getDelay();
    
    }
    
    private void drawGhosts(){
        java.awt.Point position;
        Ghost.Orientation orientation;
        
        for (int i = 0; i < ghosts.length; i++){
            
            position = ghosts[i].getPosition();
            orientation = ghosts[i].getOrientation();
            
            if(!ghosts[i].isDead()){
                int framediff = 1*1000/gameTimer.getDelay(); //number of frames in last second 
                if(ghosts[i].isPanic()){
                    //draw panicking ghosts - blue foreground
                    if(panicStopFrame-frame < framediff){
                        int diff_r = (int) (255 - (panicStopFrame-frame)*(255-60)/framediff);
                        int diff_g = (int) (255 - (panicStopFrame-frame)*(255-105)/framediff);
                        int diff_b = (int) (255 - (panicStopFrame-frame)*(255-175)/framediff);
                        
                        if(diff_r > 255 || diff_g > 255 || diff_b > 255){
                            diff_r=255; 
                            diff_g=255; 
                            diff_b=255;
                        }
                            
                        c.setForegroundColour(new java.awt.Color(diff_r,diff_g,diff_b));
                    } else {
                        c.setForegroundColour(Ghost.PANIC_COLOUR);
                    }
                } else {
                    c.setForegroundColour(ghosts[i].getColour());
                }
                if(!ghosts[i].isPanic() || panicStopFrame-frame > framediff || ((int) ((panicStopFrame-frame)/5))%5 < 2 ) {
                    c.fill(new Arc2D.Double(position.x-PACMAN_SIZE/2, position.y-PACMAN_SIZE/2, PACMAN_SIZE, PACMAN_SIZE, 0, 180, Arc2D.PIE));                      //draw top - semisphere
                    c.fill(new Rectangle2D.Double(position.x-PACMAN_SIZE/2, position.y, PACMAN_SIZE, PACMAN_SIZE*0.4));                                             //draw body rect
                    
                    for(int j = 0; j < 5; j+=2){
                        c.fill(new Ellipse2D.Double(position.x-PACMAN_SIZE/2+PACMAN_SIZE*0.2*j, position.y+PACMAN_SIZE*0.3, PACMAN_SIZE*0.2, PACMAN_SIZE*0.2));     //draw 3 bumps - 1.3.5
                    }
                    
                    for(int j = 1; j < 4; j+=2){
                        c.erase(new Ellipse2D.Double(position.x-PACMAN_SIZE/2+PACMAN_SIZE*0.2*j, position.y+PACMAN_SIZE*0.3, PACMAN_SIZE*0.2, PACMAN_SIZE*0.2));    //erase 2 bumps - .2.4.
                    }
                }
                
            }
                
            if(ghosts[i].isPanic()){ 
                
                c.setForegroundColour(new java.awt.Color(255,255,255));
                
                //draw wiggly white mouth                
                c.draw(new Arc2D.Double(position.x-PACMAN_SIZE*0.375, position.y+PACMAN_SIZE*0.1, PACMAN_SIZE*0.15, PACMAN_SIZE*0.12, 0, 180, Arc2D.OPEN));
                c.draw(new Arc2D.Double(position.x-PACMAN_SIZE*0.225, position.y+PACMAN_SIZE*0.1, PACMAN_SIZE*0.15, PACMAN_SIZE*0.12, 180, 180, Arc2D.OPEN));
                c.draw(new Arc2D.Double(position.x-PACMAN_SIZE*0.075, position.y+PACMAN_SIZE*0.1, PACMAN_SIZE*0.15, PACMAN_SIZE*0.12, 0, 180, Arc2D.OPEN));
                c.draw(new Arc2D.Double(position.x+PACMAN_SIZE*0.075, position.y+PACMAN_SIZE*0.1, PACMAN_SIZE*0.15, PACMAN_SIZE*0.12, 180, 180, Arc2D.OPEN));
                c.draw(new Arc2D.Double(position.x+PACMAN_SIZE*0.225, position.y+PACMAN_SIZE*0.1, PACMAN_SIZE*0.15, PACMAN_SIZE*0.12, 0, 180, Arc2D.OPEN));
                
                //simple eyes
                c.fill(new Ellipse2D.Double(position.x-PACMAN_SIZE*0.2, position.y-PACMAN_SIZE*0.25, PACMAN_SIZE*0.15, PACMAN_SIZE*0.2)); //left eye
                c.fill(new Ellipse2D.Double(position.x+PACMAN_SIZE*0.1, position.y-PACMAN_SIZE*0.25, PACMAN_SIZE*0.15, PACMAN_SIZE*0.2)); //right eye
                
            } else {
                
                //normal eyes            
                c.setForegroundColour(new java.awt.Color(255,255,255));
                switch(orientation){
                case RIGHT:
                    c.fill(new Ellipse2D.Double(position.x-PACMAN_SIZE*0.25, position.y-PACMAN_SIZE*0.3, PACMAN_SIZE*0.3, PACMAN_SIZE*0.4)); //left eye
                    c.fill(new Ellipse2D.Double(position.x+PACMAN_SIZE*0.15, position.y-PACMAN_SIZE*0.3, PACMAN_SIZE*0.3, PACMAN_SIZE*0.4)); //right eye
                    
                    c.setForegroundColour(new java.awt.Color(0,0,0));
                    c.fill(new Ellipse2D.Double(position.x-PACMAN_SIZE*0.075, position.y-PACMAN_SIZE*0.175, PACMAN_SIZE*0.15, PACMAN_SIZE*0.15)); //left eyeball
                    c.fill(new Ellipse2D.Double(position.x+PACMAN_SIZE*0.30, position.y-PACMAN_SIZE*0.175, PACMAN_SIZE*0.15, PACMAN_SIZE*0.15)); //right eyeball
                    break;
                case UP:
                    c.fill(new Ellipse2D.Double(position.x-PACMAN_SIZE*0.35, position.y-PACMAN_SIZE*0.3, PACMAN_SIZE*0.3, PACMAN_SIZE*0.4)); //left eye
                    c.fill(new Ellipse2D.Double(position.x+PACMAN_SIZE*0.05, position.y-PACMAN_SIZE*0.3, PACMAN_SIZE*0.3, PACMAN_SIZE*0.4)); //right eye
                    
                    c.setForegroundColour(new java.awt.Color(0,0,0));
                    c.fill(new Ellipse2D.Double(position.x-PACMAN_SIZE*0.275, position.y-PACMAN_SIZE*0.3, PACMAN_SIZE*0.15, PACMAN_SIZE*0.15)); //left eyeball
                    c.fill(new Ellipse2D.Double(position.x+PACMAN_SIZE*0.125, position.y-PACMAN_SIZE*0.3, PACMAN_SIZE*0.15, PACMAN_SIZE*0.15)); //right eyeball
                    break;
                case DOWN:
                    c.fill(new Ellipse2D.Double(position.x-PACMAN_SIZE*0.35, position.y-PACMAN_SIZE*0.3, PACMAN_SIZE*0.3, PACMAN_SIZE*0.4)); //left eye
                    c.fill(new Ellipse2D.Double(position.x+PACMAN_SIZE*0.05, position.y-PACMAN_SIZE*0.3, PACMAN_SIZE*0.3, PACMAN_SIZE*0.4)); //right eye
                    
                    c.setForegroundColour(new java.awt.Color(0,0,0));
                    c.fill(new Ellipse2D.Double(position.x-PACMAN_SIZE*0.275, position.y-PACMAN_SIZE*0.05, PACMAN_SIZE*0.15, PACMAN_SIZE*0.15)); //left eyeball
                    c.fill(new Ellipse2D.Double(position.x+PACMAN_SIZE*0.125, position.y-PACMAN_SIZE*0.05, PACMAN_SIZE*0.15, PACMAN_SIZE*0.15)); //right eyeball
                    break;
                case LEFT:
                    c.fill(new Ellipse2D.Double(position.x-PACMAN_SIZE*0.45, position.y-PACMAN_SIZE*0.3, PACMAN_SIZE*0.3, PACMAN_SIZE*0.4)); //left eye
                    c.fill(new Ellipse2D.Double(position.x-PACMAN_SIZE*0.00, position.y-PACMAN_SIZE*0.3, PACMAN_SIZE*0.3, PACMAN_SIZE*0.4)); //right eye
                    
                    c.setForegroundColour(new java.awt.Color(0,0,0));
                    c.fill(new Ellipse2D.Double(position.x-PACMAN_SIZE*0.475, position.y-PACMAN_SIZE*0.175, PACMAN_SIZE*0.15, PACMAN_SIZE*0.15)); //left eyeball
                    c.fill(new Ellipse2D.Double(position.x+PACMAN_SIZE*0.000, position.y-PACMAN_SIZE*0.175, PACMAN_SIZE*0.15, PACMAN_SIZE*0.15)); //right eyeball
                    break;    
                }
            }
        }
    }
    
    private void drawPacman(double x){
        java.awt.Point position = pacman.getPosition();
        Ghost.Orientation orientation = pacman.getOrientation();
        previousPacmanPosition = position;
        
        c.setForegroundColour(PacmanColour);
        switch(orientation){
            case UP:
                c.fill(new Arc2D.Double(position.x-PACMAN_SIZE/2, position.y-PACMAN_SIZE/2, PACMAN_SIZE, PACMAN_SIZE, 135+x, 270-2*x, Arc2D.PIE));
            break;
            case DOWN:
                c.fill(new Arc2D.Double(position.x-PACMAN_SIZE/2, position.y-PACMAN_SIZE/2, PACMAN_SIZE, PACMAN_SIZE, 315+x, 270-2*x, Arc2D.PIE));
            break;
            case LEFT: 
                c.fill(new Arc2D.Double(position.x-PACMAN_SIZE/2, position.y-PACMAN_SIZE/2, PACMAN_SIZE, PACMAN_SIZE, 225+x, 270-2*x, Arc2D.PIE));
            break;
            case RIGHT:
                c.fill(new Arc2D.Double(position.x-PACMAN_SIZE/2, position.y-PACMAN_SIZE/2, PACMAN_SIZE, PACMAN_SIZE, 45+x, 270-2*x, Arc2D.PIE));
            break;
        }
    }

    private void drawMaze(){
        
        fillOutlines();
                
        double dotsize = CELL_SIZE/8;
        double bigdotsize = CELL_SIZE/3;
        
        for (int i = 0; i < rows; i++){
            for (int j = 0; j < cols; j++){
                
                if(statusgrid[i][j] == Maze.Status.DOT){
                    //white dots
                    c.setForegroundColour(new java.awt.Color(255,255,255));
                    c.fill(new Ellipse2D.Double((i+0.5)*CELL_SIZE-dotsize/2, (j+0.5)*CELL_SIZE-dotsize/2, dotsize, dotsize));
                }
                
                if(statusgrid[i][j] == Maze.Status.ENERGISER){
                    //big white dots
                    c.setForegroundColour(new java.awt.Color(255,255,255));
                    c.fill(new Ellipse2D.Double((i+0.5)*CELL_SIZE-bigdotsize/2, (j+0.5)*CELL_SIZE-bigdotsize/2, bigdotsize, bigdotsize));
                }
                
                if(statusgrid[i][j] == Maze.Status.DEAD){
                    drawOutlines(i,j);
                }
                
                //drawgrid(i,j); //temp
                
            }
        }
        
    }
    
    
    private void drawMazeDots(){ //to redraw behind ghosts

        double dotsize = CELL_SIZE/8;
        double bigdotsize = CELL_SIZE/3;
        
        for (int i = 0; i < rows; i++){
            for (int j = 0; j < cols; j++){
                
                if(statusgrid[i][j] == Maze.Status.DOT){
                    //white dots
                    c.setForegroundColour(new java.awt.Color(255,255,255));
                    c.fill(new Ellipse2D.Double((i+0.5)*CELL_SIZE-dotsize/2, (j+0.5)*CELL_SIZE-dotsize/2, dotsize, dotsize));
                }
                
                if(statusgrid[i][j] == Maze.Status.ENERGISER){
                    //big white dots
                    c.setForegroundColour(new java.awt.Color(255,255,255));
                    c.fill(new Ellipse2D.Double((i+0.5)*CELL_SIZE-bigdotsize/2, (j+0.5)*CELL_SIZE-bigdotsize/2, bigdotsize, bigdotsize));
                }
                
            }
        }
        
    }
    
    private void drawgrid(int i, int j){
        
        c.setForegroundColour(new java.awt.Color(50,50,50));
        c.draw(new Rectangle2D.Double(i*CELL_SIZE, j*CELL_SIZE, CELL_SIZE, CELL_SIZE));
                
    }
    
    private void drawOutlines(int i, int j){
        
        //check cells around for Status.DEAD also
        int dead = checkdead(i, j);
        
        c.setForegroundColour(OutlineColour);
        
        if(dead == 4) {
            
            //outer corner
            
            for (int x = i-1; x <= i+1; x+=2){
                for (int y = j-1; y <= j+1; y+=2){
                    if(x>=0 && x < rows && y >= 0 && y < cols){
                        if(statusgrid[x][y] == Maze.Status.DEAD){
                            
                            int dx = x - i; //1 = W, -1 = E
                            int dy = y - j; //1 = N, -1 = S
                            
                            if(dx == 1 && dy == 1){
                                //NW
                                c.setForegroundColour(BackgroundColour);
                                c.fill(new Ellipse2D.Double((i+0.5)*CELL_SIZE+3, (j+0.5)*CELL_SIZE+2, CELL_SIZE-4, CELL_SIZE-4));
                                c.setForegroundColour(OutlineColour);
                                c.draw(new Arc2D.Double((i+0.5)*CELL_SIZE, (j+0.5)*CELL_SIZE, CELL_SIZE, CELL_SIZE, 90, 90, Arc2D.OPEN)); 
                            } else if(dx == 1 && dy == -1){
                                //SW
                                c.setForegroundColour(BackgroundColour);
                                c.fill(new Ellipse2D.Double((i+0.5)*CELL_SIZE+2, (j-0.5)*CELL_SIZE+2, CELL_SIZE-4, CELL_SIZE-4));
                                c.setForegroundColour(OutlineColour);
                                c.draw(new Arc2D.Double((i+0.5)*CELL_SIZE, (j-0.5)*CELL_SIZE, CELL_SIZE, CELL_SIZE, 180, 90, Arc2D.OPEN)); 
                            }  else if (dx == -1 && dy == -1){
                                //SE
                                c.setForegroundColour(BackgroundColour);
                                c.fill(new Ellipse2D.Double((i-0.5)*CELL_SIZE+2, (j-0.5)*CELL_SIZE+2, CELL_SIZE-4, CELL_SIZE-4));                               
                                c.setForegroundColour(OutlineColour);
                                c.draw(new Arc2D.Double((i-0.5)*CELL_SIZE, (j-0.5)*CELL_SIZE, CELL_SIZE, CELL_SIZE, 270, 90, Arc2D.OPEN)); 
                            }  else if (dx == -1 && dy == 1){
                                //NE
                                c.setForegroundColour(BackgroundColour);
                                c.fill(new Ellipse2D.Double((i-0.5)*CELL_SIZE+2, (j+0.5)*CELL_SIZE+2, CELL_SIZE-4, CELL_SIZE-4));                             
                                c.setForegroundColour(OutlineColour);
                                c.draw(new Arc2D.Double((i-0.5)*CELL_SIZE, (j+0.5)*CELL_SIZE, CELL_SIZE, CELL_SIZE, 0, 90, Arc2D.OPEN)); 
                            }
                        }
                    }
                }
            }
        }
        
        if(dead == 6 || dead == 7) {
            //straight    
            
            if(i > 0 && !(statusgrid[i-1][j] == Maze.Status.DEAD)){
                //W
                c.drawLine((int) ((i+0.5)*CELL_SIZE), (j)*CELL_SIZE, (int) ((i+0.5)*CELL_SIZE), (j+1)*CELL_SIZE); 
            }
            
            if(i+1 < rows && !(statusgrid[i+1][j] == Maze.Status.DEAD)){
                //E
                c.drawLine((int) ((i+0.5)*CELL_SIZE), (j)*CELL_SIZE, (int) ((i+0.5)*CELL_SIZE), (j+1)*CELL_SIZE); 
            } 
            
            if(j > 0 && !(statusgrid[i][j-1] == Maze.Status.DEAD)){
                //N
                c.drawLine((i)*CELL_SIZE, (int) ((j+0.5)*CELL_SIZE), (i+1)*CELL_SIZE, (int) ((j+0.5)*CELL_SIZE)); 
            }
            
            if(j+1 < cols && !(statusgrid[i][j+1] == Maze.Status.DEAD)){
                //S
                c.drawLine((i)*CELL_SIZE, (int) ((j+0.5)*CELL_SIZE), (i+1)*CELL_SIZE, (int) ((j+0.5)*CELL_SIZE)); 
            }
        }
        
        if(dead == 8) {
            //inner corner
            for (int x = i-1; x <= i+1; x+=2){
                for (int y = j-1; y <= j+1; y+=2){
                    if(x>=0 && x < rows && y >= 0 && y < cols){
                        if(!(statusgrid[x][y] == Maze.Status.DEAD)){

                            int dx = x - i; //1 = W, -1 = E
                            int dy = y - j; //1 = N, -1 = S

                            c.setForegroundColour(BackgroundColour);
                            c.fill(new Rectangle2D.Double((i)*CELL_SIZE, (j)*CELL_SIZE, CELL_SIZE, CELL_SIZE));
                            c.setForegroundColour(OutlineColour);

                            if(dx == 1 && dy == 1){
                                //NW
                                c.erase(new Arc2D.Double((i+0.5)*CELL_SIZE, (j+0.5)*CELL_SIZE, CELL_SIZE, CELL_SIZE, 90, 90, Arc2D.PIE));
                                c.draw(new Arc2D.Double((i+0.5)*CELL_SIZE, (j+0.5)*CELL_SIZE, CELL_SIZE, CELL_SIZE, 90, 90, Arc2D.OPEN));
                            } else if(dx == 1 && dy == -1){
                                //SW
                                c.erase(new Arc2D.Double((i+0.5)*CELL_SIZE, (j-0.5)*CELL_SIZE, CELL_SIZE, CELL_SIZE, 180, 90, Arc2D.PIE));                                
                                c.draw(new Arc2D.Double((i+0.5)*CELL_SIZE, (j-0.5)*CELL_SIZE, CELL_SIZE, CELL_SIZE, 180, 90, Arc2D.OPEN));
                            }  else if (dx == -1 && dy == -1){
                                //SE
                                c.erase(new Arc2D.Double((i-0.5)*CELL_SIZE, (j-0.5)*CELL_SIZE, CELL_SIZE, CELL_SIZE, 270, 90, Arc2D.PIE));
                                c.draw(new Arc2D.Double((i-0.5)*CELL_SIZE, (j-0.5)*CELL_SIZE, CELL_SIZE, CELL_SIZE, 270, 90, Arc2D.OPEN));
                            }  else if (dx == -1 && dy == 1){
                                //NE
                                c.erase(new Arc2D.Double((i-0.5)*CELL_SIZE, (j+0.5)*CELL_SIZE, CELL_SIZE, CELL_SIZE, 0, 90, Arc2D.PIE));
                                c.draw(new Arc2D.Double((i-0.5)*CELL_SIZE, (j+0.5)*CELL_SIZE, CELL_SIZE, CELL_SIZE, 0, 90, Arc2D.OPEN));
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void fillOutlines(){
        
        c.setForegroundColour(BackgroundColour);
        for (int i = 0; i < rows; i++){
            for (int j = 0; j < cols; j++){
                
                if (statusgrid[i][j] == Maze.Status.DEAD){
                    int dead = checkdead(i,j);
                           
                    if(dead > 5) {
                        
                        if(i > 0 && !(statusgrid[i-1][j] == Maze.Status.DEAD)){
                            //E
                            c.fill(new Rectangle2D.Double((i+0.5)*CELL_SIZE+1, j*CELL_SIZE-2, CELL_SIZE/2, CELL_SIZE+4));
                        }
                        
                        if(i+1 < rows && !(statusgrid[i+1][j] == Maze.Status.DEAD)){
                            //W
                            c.fill(new Rectangle2D.Double((i)*CELL_SIZE-1, (j)*CELL_SIZE-2, CELL_SIZE/2, CELL_SIZE+4));
                        } 
                        
                        if(j > 0 && !(statusgrid[i][j-1] == Maze.Status.DEAD)){
                            //S
                            c.fill(new Rectangle2D.Double(i*CELL_SIZE, (j+0.5)*CELL_SIZE+1, CELL_SIZE, CELL_SIZE/2+1));
                        }
                        
                        if(j+1 < cols && !(statusgrid[i][j+1] == Maze.Status.DEAD)){
                            //N
                            c.fill(new Rectangle2D.Double(i*CELL_SIZE, j*CELL_SIZE-1, CELL_SIZE, CELL_SIZE/2));
                        }
                    }
                    
                    if (dead == 9) {
                        c.fill(new Rectangle2D.Double(i*CELL_SIZE, j*CELL_SIZE, CELL_SIZE, CELL_SIZE));
                    }
                }
            }
        }
    }
    
    private int checkdead(int i, int j){
        int dead = 0;
        for (int x = i-1; x <= i+1; x++){
            for (int y = j-1; y <= j+1; y++){
                if(x>=0 && x < rows && y >= 0 && y < cols){
                    if (statusgrid[x][y] == Maze.Status.DEAD){
                        dead++;
                    }
                } else {
                    dead++; //off screen assumed to be dead
                }
            }
        }
        return dead;
    }
    
    public void saveHighscore(int highscore) {
        try {
            FileWriter myFile = new FileWriter(new File("highscore.txt"));
            myFile.write(Integer.toString(highscore));
            myFile.close();     //Closes the file 
        } catch (IOException e) {
            // Print out the exception that occurred
            System.out.println("Unable to create highscore.txt - "+e.getMessage());
        }
    }
    
    public int getHighscore(){
        try {
            FileReader myFile = new FileReader(new File("highscore.txt"));
            String myString = "";
            int i = 0;
            while((i = myFile.read()) != -1)
            {
                myString += (char) i;
            }
            return Integer.parseInt(myString);
                    
        } catch (IOException e) {
            // Print out the exception that occurred
            saveHighscore(0);
            //System.out.println("Unable to read from highscore.txt - "+e.getMessage());
        }
        return 0;
    }
    
    public void doCollide(){  
        
        gameTimer.stop();
        deathTimer.start();
        new Sounds(Sounds.Sound.PACMAN_DEATH, true);
        myDotSound.loop(false);
        deathAngle = 0;

    }
    
    private void doCollide2(){
        if(maze.getNumLives() > 0){
            pacman.setToOriginal();
            Ghost.setToOriginal(ghosts);
            eraseScreen();
            drawEverything();
        }

        if(pacman.isDemo()){
            if(maze.getNumLives()>0)
            gameTimer.start(); //don't wait for keypress
            
        } else {
            
            if(maze.getNumLives()>0){
                c.setForegroundColour(new java.awt.Color(255,255,255));
                c.setFont(defaultFont);
                c.drawString("Ready?", (int) (rows*CELL_SIZE/2-CELL_SIZE*3.1), cols*CELL_SIZE/2);
            }
            
        }
    }
    
     ActionListener drawPacmanDeath = new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
            
            deathAngle+=3;

            erasePacman(); //redraw where pacman was
            drawGhosts(); //draw ghosts
            drawPacman(deathAngle); //draw pacman
                        
            if(deathAngle >= 135){
                deathTimer.stop();
                doCollide2();
            }
        }
    };
    
    public void playDot(boolean doLoop){
        myDotSound.loop(doLoop);
    }
    
    public void playEnergiser(){
        new Sounds(Sounds.Sound.EAT_CHERRY, true);
    }
    
    public void playGhost(){
        new Sounds(Sounds.Sound.EAT_GHOST, true);
    }
        
}