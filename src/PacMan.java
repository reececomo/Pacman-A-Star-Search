/**
 * Write a description of class PacMan here.
 * 
 * @author Reece Notargiacomo
 * @version 19th May 2015
 */

import java.awt.Point;

public class PacMan
{

    private Mode                  PacmanMode;
    private Ghost.Orientation     PacmanOrientation;
    private Ghost.Orientation     nextOrientation;
    private int                   PacmanSpeed;
    private Point                 PacmanPosition;
    
    private Point                 OriginalPosition;
    private Ghost.Orientation     OriginalOrientation;
    
    public enum Mode {CONTROLLED, AUTONOMOUS};
    
    public static final int[] SPEED = {0,1,2,4,8,16,32};
      
      /*
       * The SPEED indicates the legal speed, representing how many pixels the center of Pac-Man or the ghost moves per frame. 
       * Same as the ghost speeds, they need to be divisors of MazeViewer.CELL_SIZE.
       * The Ghost.ORIENTATION is used here to record the Pac-Man's moving direction as well. 
       */
      
    public PacMan(Point pos){
        
        /*
        * creates a Pac-Man with an initial position, and sets its 
        * initial orientation to Orientation.UP, 
        * mode to Mode.CONTROLLED, 
        * initial travelling speed to 16 (i.e. MazeViewer.CELL_SIZE).
        */
       
        PacmanPosition = pos;
        PacmanOrientation = Ghost.Orientation.UP;
        nextOrientation = PacmanOrientation;
        PacmanMode = Mode.AUTONOMOUS;      
        setSpeed(MazeViewer.CELL_SIZE);
        OriginalPosition = PacmanPosition;
        OriginalOrientation = PacmanOrientation;
    }
    
    public PacMan(Point pos, Ghost.Orientation ori, int speed, Mode m) {
        //creates a ghost with client specified position, orientation, speed, and mode. 
        
        System.out.println("Hello");
        PacmanPosition = pos;
        PacmanOrientation = ori;
        setMode(m);
        setSpeed(speed);
        nextOrientation = PacmanOrientation;
        
        OriginalPosition = PacmanPosition;
        OriginalOrientation = PacmanOrientation;
    }
    
    public int getSpeed() { return PacmanSpeed; }
    
    public void setSpeed(int newSpeed){
        //changes the travelling speed to newSpeed.
        
        //if in speed array
        boolean found = false;
        for(int i = 0; i < SPEED.length; i++){
            if (SPEED[i] == newSpeed) {
                found = true;
                break;
            }
        }
        
        if (found) {
            PacmanSpeed = newSpeed;
        } else {
            throw new IllegalArgumentException("Error - Newspeed not in speed array");
        }
    }
    
    public void setMode(Mode mode){
        //changes the mode to m. Again your code should ensure consistencies between the input.
        //??
        PacmanMode = mode;
    }
    
    public Ghost.Orientation getOrientation(){ 
        //returns the current orientation of Pac-Man.
        return PacmanOrientation;
    }
    
    public Point getPosition(){
        //returns the current position of Pac-Man.
        return PacmanPosition;
    }
    
    public void setNextOrientation(Ghost.Orientation ori){ 
        nextOrientation = ori;
    }
    
    public Ghost.Orientation nextOrientation(){
        return nextOrientation;
    }   
    
    public boolean atGrid(){
        boolean canx = false;
        if ((PacmanPosition.x-MazeViewer.CELL_SIZE/2)%MazeViewer.CELL_SIZE == 0) canx = true;
        boolean cany = false;
        if ((PacmanPosition.y-MazeViewer.CELL_SIZE/2)%MazeViewer.CELL_SIZE == 0) cany = true;

        return canx && cany;
    }
    
    public void doMove(Maze maze){
        
        if(PacmanMode == Mode.AUTONOMOUS){
            nextOrientation = closestDot(maze);
        }
        
        Maze.Status nextStatus = maze.locationStatus(nextPos(PacmanPosition,nextOrientation));
        boolean canturn = false;
        if(nextStatus == Maze.Status.DOT || nextStatus == Maze.Status.LEGAL || nextStatus == Maze.Status.ENERGISER){
            canturn = true;
        }
        
        //if on grid 
        if(canturn && atGrid()){
            move(maze,nextOrientation);
        } else {
            move(maze,PacmanOrientation); //keep going
        }
    }
    
    private Ghost.Orientation closestDot(Maze maze){
        
        Maze.Status[][] tempMap = maze.getMap();
        Ghost[] ghosts = maze.getGhosts();
        Point closest;
        
        //find possible orientations
        Ghost.Orientation[] possibleOrientations = getOrientations(maze);

        //if only one orientation possible
        if(possibleOrientations.length == 1){
            return possibleOrientations[0];
        }
        
        //find closest dot
        Point closestDot = new Point(0,0);
        int closestDotDist = 0;
        
        for (int i = 0; i < tempMap.length; i++){
            for (int j = 0; j < tempMap[0].length; j++){
                if(tempMap[i][j] == Maze.Status.DOT || tempMap[i][j] == Maze.Status.ENERGISER){
                    
                    int tempDist = Math.abs(PacmanPosition.x-i*MazeViewer.CELL_SIZE)+Math.abs(PacmanPosition.y-j*MazeViewer.CELL_SIZE);
                    
                    if(closestDotDist > tempDist || closestDotDist == 0){
                        closestDotDist = tempDist;      //distance from pacman to closest dot
                        closestDot = new Point (i, j);  //grid location of closest dot
                    }
                }
            }
        }
        
        //find closest ghost
        java.awt.Point closestGhost = new Point (ghosts[0].getPosition().x/MazeViewer.CELL_SIZE, ghosts[0].getPosition().y/MazeViewer.CELL_SIZE);
        int closestGhostDist = Math.abs(PacmanPosition.x-ghosts[0].getPosition().x)+Math.abs(PacmanPosition.y-ghosts[0].getPosition().y); //0th ghost = initial
        
        //go through all ghosts
        for(int i = 0; i < ghosts.length; i++){
            if(!ghosts[i].isDead()){
                    
                int tempDist = Math.abs(PacmanPosition.x-ghosts[i].getPosition().x)+Math.abs(PacmanPosition.y-ghosts[i].getPosition().y);
                
                if(closestGhostDist > tempDist){ //if closer than previous
                    closestGhostDist = tempDist;
                    closestGhost = new Point (ghosts[i].getPosition().x/MazeViewer.CELL_SIZE, ghosts[i].getPosition().y/MazeViewer.CELL_SIZE);
                }
            }
        }
                    
        //System.out.println(closestGhostDist+"");
                    
        if(!ghosts[0].isPanic()){
            //towards ghosts if they're close enough to be a problem
            if(closestGhostDist < MazeViewer.CELL_SIZE*5){
                //distance of first possible direction to closest ghost
                int runAwayDist = Math.abs(closestGhost.x*MazeViewer.CELL_SIZE - nextPos(PacmanPosition,possibleOrientations[0]).x) + Math.abs(closestGhost.y*MazeViewer.CELL_SIZE - nextPos(PacmanPosition,possibleOrientations[0]).y);
                Ghost.Orientation runAway = possibleOrientations[0]; //initial orientation and distance
                
                //find orientation away from ghost
                for(int i = 0; i < possibleOrientations.length; i++){
                    
                    int tempDist = Math.abs(closestGhost.x*MazeViewer.CELL_SIZE - nextPos(PacmanPosition,possibleOrientations[i]).x) + Math.abs(closestGhost.y*MazeViewer.CELL_SIZE - nextPos(PacmanPosition,possibleOrientations[i]).y);
            
                    if(tempDist > runAwayDist){
                        runAwayDist = tempDist;
                        runAway = possibleOrientations[i];
                    }
                }
                return runAway;
            } else {
                closest = closestDot;
            }
        } else { //if panicking
            //if ghost in range of easy hunting - don't go too far away from dots course
            if(closestGhostDist < MazeViewer.CELL_SIZE*10){
                closest = closestGhost;
            } else {
                closest = closestDot;
            }
        }
        
        //find shortest route to target
        Ghost.Orientation thisway = possibleOrientations[0];
        int dist = Math.abs(closest.x*MazeViewer.CELL_SIZE - nextPos(PacmanPosition,possibleOrientations[0]).x) + Math.abs(closest.y*MazeViewer.CELL_SIZE - nextPos(PacmanPosition,possibleOrientations[0]).y);
        
        //go through possible orientations
        for(int i = 0; i < possibleOrientations.length; i++){

            if(maze.locationStatus(nextPos(PacmanPosition,possibleOrientations[i])) == Maze.Status.DOT){
                return possibleOrientations[i];
            }
            
            int tempDist = Math.abs(closest.x*MazeViewer.CELL_SIZE - nextPos(PacmanPosition,possibleOrientations[i]).x) + Math.abs(closest.y*MazeViewer.CELL_SIZE - nextPos(PacmanPosition,possibleOrientations[i]).y);
            
            if(tempDist < MazeViewer.CELL_SIZE){
                if(Math.random()<0.9)
                    return possibleOrientations[i];
            }
            
            if(dist == tempDist){
                if(Math.random()<(1/possibleOrientations.length))
                    return possibleOrientations[i];
            }
            
            if(dist > tempDist){
                dist = tempDist;
                thisway = possibleOrientations[i];
            }
        }
      
        return thisway;
    }
    
    public void move(Maze maze, Ghost.Orientation ori){
        //moves Pac-Man in the maze if there is a legal tile in that direction ori, and change the orientation to ori.
        
        boolean doMove = false;
        
        if(atGrid()){
            switch (maze.locationStatus(nextPos(PacmanPosition,ori))){
                case INVALID:
                    //wrap around to other side of screen
                    if (PacmanPosition.x == MazeViewer.CELL_SIZE/2){ //left side
                        PacmanPosition.x = maze.getMap().length*MazeViewer.CELL_SIZE-MazeViewer.CELL_SIZE/2;
                        
                    } else if (PacmanPosition.x == maze.getMap().length*MazeViewer.CELL_SIZE-MazeViewer.CELL_SIZE/2){ //right side
                        PacmanPosition.x = MazeViewer.CELL_SIZE/2;
                    }
                    
                    break;
        
                case DEAD:
                    //don't move
                    maze.doStopDot(); //no more dot sounds
                    break;
                    
                case ENERGISER:
                    //eat energiser
                    //add 50 score
                    maze.doEnergiser();
                    maze.setStatus(nextPos(PacmanPosition,ori).x/MazeViewer.CELL_SIZE,nextPos(PacmanPosition,ori).y/MazeViewer.CELL_SIZE, Maze.Status.LEGAL);
                    //panic!

                    Ghost.setPanic(maze.getGhosts(), true);
                    maze.doPanicCountdown();
                    
                    doMove = true;
                    break;
                    
                case DOT:
                    //eat dot
                    //add 10 score
                    maze.doDot();
                    maze.setStatus(nextPos(PacmanPosition,ori).x/MazeViewer.CELL_SIZE,nextPos(PacmanPosition,ori).y/MazeViewer.CELL_SIZE, Maze.Status.LEGAL);
                    doMove = true;
                    break;
                    
                case LEGAL: 
                    //move
                    doMove = true;
                    maze.doStopDot();
            }
        } else {
            doMove = true;
        }
        
        if(doMove){
            
            PacmanPosition = nextPos2(PacmanPosition, ori); //try for smoothness
            PacmanOrientation = ori;
            
        }
    }
    
    private Point nextPos(Point positioncopy, Ghost.Orientation ori){
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
    
    private Point nextPos2(Point positioncopy, Ghost.Orientation ori){
        Point position = new Point(positioncopy); //translate copy of point
        int dist = (PacmanSpeed/MazeViewer.CELL_SIZE)*2;
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
    
    public void move(Maze maze){
        //HOT
        //autonomously move the Pac-Man as a demo.
        PacmanMode = Mode.AUTONOMOUS;
    }
    
    public void doDemo(){
        if(PacmanMode == Mode.AUTONOMOUS){
            PacmanMode = Mode.CONTROLLED;
        } else {
            PacmanMode = Mode.AUTONOMOUS;
        }
    }
    
    private Ghost.Orientation[] getOrientations(Maze maze){
        //up down left right
        
        boolean[] temp = new boolean[4];
        
        for(int i = 0; i < 4; i++){
            temp[i] = false;
        }
        
        if(!(maze.locationStatus(nextPos(PacmanPosition, Ghost.Orientation.UP)) == Maze.Status.DEAD)){     
            if(PacmanOrientation != Ghost.Orientation.DOWN){
                temp[0] = true;
            }
        } 
        
        if(!(maze.locationStatus(nextPos(PacmanPosition, Ghost.Orientation.DOWN)) == Maze.Status.DEAD)){ 
            if(PacmanOrientation != Ghost.Orientation.UP){
                temp[1] = true;
            }
        } 
        
        if(!(maze.locationStatus(nextPos(PacmanPosition, Ghost.Orientation.LEFT)) == Maze.Status.DEAD)){    
            if(PacmanOrientation != Ghost.Orientation.RIGHT){
                temp[2] = true;
            }
        } 
        
        if(!(maze.locationStatus(nextPos(PacmanPosition, Ghost.Orientation.RIGHT)) == Maze.Status.DEAD)){
            if(PacmanOrientation != Ghost.Orientation.LEFT){
                temp[3] = true;
            }
        }
        
        int count = 0;
        
        for(int i = 0; i < 4; i++){
            if(temp[i]){
                count++;
            }
        }
        
        Ghost.Orientation[] tempOri = new Ghost.Orientation[count];
        
        int i = 0;
        
        if(temp[0]){
            tempOri[i]=Ghost.Orientation.UP;
            i++;
        }
        
        if(temp[1]){
            tempOri[i]=Ghost.Orientation.DOWN;
            i++;
        }
        
        if(temp[2]){
            tempOri[i]=Ghost.Orientation.LEFT;
            i++;
        }
        
        if(temp[3]){
            tempOri[i]=Ghost.Orientation.RIGHT;
        }
        
        return tempOri;
    }
    
    public boolean isDemo(){
        return PacmanMode == Mode.AUTONOMOUS;
    }
    
    public void setToOriginal(){
        PacmanPosition = OriginalPosition;
        PacmanOrientation = OriginalOrientation;
    }
          
}
