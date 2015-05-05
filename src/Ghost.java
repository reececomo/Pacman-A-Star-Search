
/**
 * Write a description of class Ghost here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */

import java.awt.Point;

public class Ghost
{
    //my variables
    private Mode            GhostMode;
    private Mode            PreviousMode;
    private Orientation     GhostOrientation;
    private Orientation     PreviousOrientation;
    private int[]           GhostScatterTarget;
    private Target          GhostTarget;
    private boolean         dead;
    
    private int             GhostSpeed;

    private java.awt.Color  GhostColour;
    private Point           GhostPosition;
    private Point           OriginalPosition; //to go back home when dead
    
    // Default Ghost Variables
    public static enum Mode{CHASE,SCATTER,PANIC};
    public static enum Orientation{UP,DOWN,LEFT,RIGHT};
    public static enum Target {PACMAN,OFFSET,AIMLESS,SCATTER};
    public static final int[] SPEED={0,1,2,4,8,16,32};
    public static final int OFFSET = 4;
    
    public static final java.awt.Color PANIC_COLOUR = new java.awt.Color(60,100,175);
    
    /*
     * The SPEED indicates the legal speed, representing how many pixels the center of the Pac-Man or the ghost moves per frame. 
     * Note that these speed are proper divisors of the MazeViewer's CELL_SIZE.
     */

    public Ghost(java.awt.Point pos, java.awt.Color colour, int[] scatterTarget){
        /* creates a ghost with a specified position
         * colour
         * a two element integer array specifying the tile position of the scatter target
         * 
         * and sets its initial orientation to Orientation.UP
         * mode to Mode.SCATTER
         * initial travelling speed to CELL_SIZE. 
         */
        
        GhostPosition = pos;
        GhostColour = colour;
        GhostTarget = Target.SCATTER;
        GhostScatterTarget = scatterTarget;
        GhostOrientation = Orientation.UP;
        PreviousOrientation = GhostOrientation;
        setSpeed(MazeViewer.CELL_SIZE);
        GhostMode = Mode.SCATTER;
        
        dead = false;
        OriginalPosition = GhostPosition;
        PreviousMode = GhostMode;
    }
    
    public Ghost(java.awt.Point pos, java.awt.Color colour, Orientation ori, int speed, Mode m, Target t, int[] scatterTarget) {
       
        /* 
         * creates a ghost with client specified position, colour, orientation, speed, mode, 
         * target scheme and a two element integer array specifying the tile position of the scatter target. 
         * 
         * Your code should ensure the consistency between the specified mode and target scheme. 
         * Note, only Target.SCATTER can be used in Mode.SCATTER, only Target.AIMLESS can be used in Mode.PANIC.
         */
        
        GhostPosition = pos;
        GhostColour = colour;
        GhostOrientation = ori;
        setSpeed(speed);
        setMode(m, t);
        GhostScatterTarget = scatterTarget;
        dead = false;
        OriginalPosition = GhostPosition;
        PreviousOrientation = GhostOrientation;
        PreviousMode = GhostMode;
    }
    
    // Return GhostMode
    public Mode getMode() { return GhostMode; }

    // Return GhostSpeed
    public int getSpeed() { return GhostSpeed; }

    // Changes the speed 
    public void setSpeed(int newSpeed) {
        boolean found = false;
        // If the new speed is in the array of speeds
        for (int i = 0; i < SPEED.length; i++)
            if (SPEED[i] == newSpeed) { found = true; break; }
        // Set Ghost speed to new speed, or the default speed
        GhostSpeed = found ? newSpeed : SPEED[0];
    }
    
    // Get GhostColour
    public java.awt.Color getColour() { return GhostColour; }
    
    // Set GhostColour
    public void setColour(java.awt.Color colour) { GhostColour = colour; }
    
    public void setMode(Mode m, Target t) {
        //changes the mode to m and target scheme to t. Again your code should ensure consistencies between the input.
        
       if (
            (t == Target.SCATTER && m == Mode.SCATTER) ||
            (t == Target.AIMLESS && m == Mode.PANIC)||
            ((t == Target.PACMAN || t == Target.OFFSET) && m == Mode.CHASE)
        ){
            GhostMode = m;  
            GhostTarget = t;
        } else {
            throw new IllegalArgumentException("Inconsitency between mode & target scheme");
        }        
    }
    
    public Orientation getOrientation(){
        //returns the current orientation of the ghost.
        return GhostOrientation;
    }
    
    public Point getPosition(){
        //returns the current position of the ghost.
        return GhostPosition;
    }
    
    public boolean isDead(){
        return dead;
    }
    
    public boolean isPanic(){
        //returns true if the ghost is frightened and in PANIC mode.
        return GhostMode == Mode.PANIC;
    }
    
    public static void setPanic(Ghost[] ghosts, boolean panic){
        //sets a set of ghosts in panic mode or release them from panic mode. Remember setting the ghost in PANIC means the target scheme will be AIMLESS. 
        for (int i = 0; i < ghosts.length; i++){
            if(panic) {
                if(ghosts[i].GhostMode != Mode.PANIC)
                    ghosts[i].PreviousMode = ghosts[i].GhostMode;
                ghosts[i].GhostMode = Mode.PANIC;
                ghosts[i].setSpeed(MazeViewer.CELL_SIZE/2);
            } else {
                //return to previous !!!
                ghosts[i].GhostMode = ghosts[i].PreviousMode;
                ghosts[i].setSpeed(MazeViewer.CELL_SIZE);
                
                ghosts[i].GhostPosition.x = ((int) (ghosts[i].GhostPosition.x/2))*2; //round to nearest 2 pixels
                ghosts[i].GhostPosition.y = ((int) (ghosts[i].GhostPosition.y/2))*2; //round to nearest 2 pixels
                
            }
        }
    }
    
    public boolean atGrid(){
        boolean canx = false;
        if ((GhostPosition.x-MazeViewer.CELL_SIZE/2)%MazeViewer.CELL_SIZE == 0) canx = true;
        boolean cany = false;
        if ((GhostPosition.y-MazeViewer.CELL_SIZE/2)%MazeViewer.CELL_SIZE == 0) cany = true;

        return canx && cany;
    }
    
    public void move(Maze maze){
        //automatically moves the ghost in the user specified maze to the next position with proper orientation.
        boolean doMove = false;
        
        PacMan pacman = maze.getPacMan();
        
        //conditions to collide
        
        if((GhostPosition.x == pacman.getPosition().x && GhostPosition.y == pacman.getPosition().y || 
        
        Math.abs(GhostPosition.x - pacman.getPosition().x)== 1 && GhostPosition.y == pacman.getPosition().y ||
        GhostPosition.x == pacman.getPosition().x && Math.abs(GhostPosition.y - pacman.getPosition().y) == 1 || 
        
        Math.abs(GhostPosition.x - pacman.getPosition().x)== 2 && GhostPosition.y == pacman.getPosition().y ||
        GhostPosition.x == pacman.getPosition().x && Math.abs(GhostPosition.y - pacman.getPosition().y) == 2 ) && !dead){
            
            maze.doCollide(isPanic(), GhostPosition);
            if(isPanic()){
                dead = true;
            }
        }
        
        if(atGrid()){
            
            Orientation[] Ori = getOrientations(maze);
            
           if(dead){
                GhostOrientation = targetDirection(OriginalPosition,maze);
                
                if(Math.abs(GhostPosition.x - OriginalPosition.x) <= 4 && Math.abs(GhostPosition.y - OriginalPosition.y) <= 4){
                    dead = false;
                }
            } else {
                switch(GhostMode){
                    case PANIC:
                    //random from possible directions 
                    GhostOrientation = Ori[(int) (Math.random()*Ori.length)];
                    break;
                    
                    case SCATTER: 
                    //follow scatter target
                    Point target = new Point (GhostScatterTarget[0], GhostScatterTarget[1]);
                    GhostOrientation = targetDirection(target, maze, true);
                    break;
                    
                    case CHASE:
                    if(GhostTarget == Target.PACMAN){
                        //follow pacman
                        GhostOrientation = targetDirection(maze.getPacMan().getPosition(), maze);
                    } else {
                        //offset
                        GhostOrientation = targetDirection(offset(maze.getPacMan().getPosition(),maze.getPacMan().getOrientation()), maze);
                    }
                }
            }

            switch (maze.locationStatus(nextPos(GhostPosition,GhostOrientation))){
            case INVALID:
            //wrap around to other side of screen
                if (GhostPosition.x == MazeViewer.CELL_SIZE/2){ //left side
                    GhostPosition.x = maze.getMap().length*MazeViewer.CELL_SIZE-MazeViewer.CELL_SIZE/2;
                    
                } else if (GhostPosition.x == maze.getMap().length*MazeViewer.CELL_SIZE-MazeViewer.CELL_SIZE/2){ //right side
                    GhostPosition.x = MazeViewer.CELL_SIZE/2;
                }
            case DEAD:
                //won't happen
                break;
            case ENERGISER:
            case LEGAL:
            case DOT:
            doMove = true;
            }
        } else {
            doMove = true;
        }
        
        if(doMove){
             PreviousOrientation = GhostOrientation;
             GhostPosition = nextPos2(GhostPosition, GhostOrientation);
        }
    }

    private Orientation targetDirection(Point target, Maze maze){
        return targetDirection(target, maze, false);
    }
    
    private Orientation targetDirection(Point target, Maze maze, boolean doScale){
        
        Orientation[] possibleOrientations = getOrientations(maze);

        if(possibleOrientations.length == 1){
            return possibleOrientations[0];
        }
        
        Orientation thisway = possibleOrientations[0];        
        int dist;
        int tempDist;
        
        if(doScale){
            dist = Math.abs(target.x*MazeViewer.CELL_SIZE - nextPos(GhostPosition,possibleOrientations[0]).x) + Math.abs(target.y*MazeViewer.CELL_SIZE - nextPos(GhostPosition,possibleOrientations[0]).y);
        } else {
            dist = Math.abs(target.x - nextPos(GhostPosition,possibleOrientations[0]).x) + Math.abs(target.y - nextPos(GhostPosition,possibleOrientations[0]).y);
        }

        //find shortest route
        for(int i = 0; i < possibleOrientations.length; i++){
            
            if(doScale){
                tempDist = Math.abs(target.x*MazeViewer.CELL_SIZE - nextPos(GhostPosition,possibleOrientations[i]).x) + Math.abs(target.y*MazeViewer.CELL_SIZE - nextPos(GhostPosition,possibleOrientations[i]).y);
            } else {
                tempDist = Math.abs(target.x - nextPos(GhostPosition,possibleOrientations[i]).x) + Math.abs(target.y - nextPos(GhostPosition,possibleOrientations[i]).y);
            }
            
            if(dist > tempDist){
                dist = tempDist;
                thisway = possibleOrientations[i];
            }
            /*
            if(tempDist <= MazeViewer.CELL_SIZE){
                if(Math.random()<0.9){
                    return possibleOrientations[i];
                }
            }
            */
        }

        return thisway;
    }
    
    private Point nextPos(Point positioncopy, Orientation ori){
        Point position = new Point(positioncopy); //translate copy of point
        int dist = MazeViewer.CELL_SIZE;
        switch (ori){
            case UP: 
                position.translate(0,-dist); break;
            case DOWN: 
                position.translate(0,dist); break;
            case LEFT: 
                position.translate(-dist,0); break;
            case RIGHT: 
                position.translate(dist,0); break;
        }
        return position;
    }
    
    private Point nextPos2(Point positioncopy, Orientation ori){
        Point position = new Point(positioncopy); //translate copy of point
        int dist = GhostSpeed/(MazeViewer.CELL_SIZE/2); //ghost speed
        switch (ori){
            case UP: 
                position.translate(0,-dist); break;
            case DOWN: 
                position.translate(0,dist); break;
            case LEFT: 
                position.translate(-dist,0); break;
            case RIGHT: 
                position.translate(dist,0); break;
        }
        return position;
    }
    
    private Point offset(Point positioncopy, Orientation ori){
        Point position = new Point(positioncopy); //translate copy of point
        int dist = MazeViewer.CELL_SIZE*4; //for offset
        switch (ori){
            case UP: 
                position.translate(0,-dist); break;
            case DOWN: 
                position.translate(0,dist); break;
            case LEFT: 
                position.translate(-dist,0); break;
            case RIGHT: 
                position.translate(dist,0); break;
        }
        return position;
    }
    
    private Orientation[] getOrientations(Maze maze){
        //up down left right
        
        boolean[] temp = new boolean[4];
        
        for(int i = 0; i < 4; i++){
            temp[i] = false; //set all directions not possible
        }
        
        if(!(maze.locationStatus(nextPos(GhostPosition, Orientation.UP)) == Maze.Status.DEAD)){ //if maze is not dead    
            if(GhostOrientation != Orientation.DOWN){ //and haven't just come that way
                temp[0] = true; //direction is possible
            }
        } 
        
        if(!(maze.locationStatus(nextPos(GhostPosition, Orientation.DOWN)) == Maze.Status.DEAD)){ 
            if(GhostOrientation != Orientation.UP){
                temp[1] = true;
            }
        } 
        
        if(!(maze.locationStatus(nextPos(GhostPosition, Orientation.LEFT)) == Maze.Status.DEAD)){
            if(GhostOrientation != Orientation.RIGHT && PreviousOrientation != Orientation.RIGHT){
                temp[2] = true;
            }
        } 
        
        if(!(maze.locationStatus(nextPos(GhostPosition, Orientation.RIGHT)) == Maze.Status.DEAD)){
            if(GhostOrientation != Orientation.LEFT && PreviousOrientation != Orientation.LEFT){
                temp[3] = true;
            }
        }
        
        // Count number of possible directions
        int count = 0;
        for(int i = 0; i < 4; i++)
            if(temp[i])
            	count++;
        
        Orientation[] tempOri = new Orientation[count]; //new array of possible directions
        
        int i = 0;
        
        if(temp[0])
            tempOri[i++] = Orientation.UP;
	    if(temp[1])
	        tempOri[i++] = Orientation.DOWN;
	    if(temp[2])
	        tempOri[i++] = Orientation.LEFT;
	    if(temp[3])
	        tempOri[i++] = Orientation.RIGHT;
        
        return tempOri;
    }
    
    static public void setToOriginal(Ghost[] ghosts){
        
        for(int i = 0; i < ghosts.length; i++){
            
            ghosts[i].GhostPosition = ghosts[i].OriginalPosition;
            
        }
    }
}
