
/**
 * Write a description of class Ghost here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class Ghost
{
    //my variables

    private Mode            GhostMode;
    private Mode            PreviousMode;
    private Orientation     GhostOrientation;
    private Orientation     PreviousOrientation;
    private int[]           GhostScatterTarget;
    private Point           scatter_target;
    private Target          GhostTarget;
    private boolean         dead;
    private PacMan          pacman = null;
    private Point           pacmanpos;
    private java.awt.Color  pink;
    
    private int             GhostSpeed;

    private java.awt.Color  GhostColour;
    private Point           GhostPosition;
    private Point           OriginalPosition; //to go back home when dead
    
    //given
    public static enum Mode{CHASE,SCATTER,PANIC};
    public static enum Orientation{UP,DOWN,LEFT,RIGHT};
    public static enum Target {PACMAN,OFFSET,AIMLESS,SCATTER};
    public static final int[] SPEED={0,1,2,4,8,16,32};
    public static final int OFFSET = 4;
    
    public static final java.awt.Color PANIC_COLOUR = new java.awt.Color(60,100,175);

    // Reece choking AI
    private int[][]         globalmaze;
    private Point[]         chokepoints;
    private ArrayList<Point> current_path;
    
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
        scatter_target = new Point (GhostScatterTarget[0], GhostScatterTarget[1]);
        GhostOrientation = Orientation.UP;
        PreviousOrientation = GhostOrientation;
        setSpeed(MazeViewer.CELL_SIZE);
        GhostMode = Mode.SCATTER;
        
        dead = false;
        OriginalPosition = GhostPosition;
        PreviousMode = GhostMode;
        pink = new java.awt.Color(255,0,255);
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

    public int[][] calculateMaze(Maze inputmaze) {
        Maze.Status[][] statusmap = inputmaze.getMap();

        int[][] maze = new int[statusmap.length][];

        for(int i = 0; i < statusmap.length; i++) {
            maze[i] = new int[statusmap[i].length];
            for(int j = 0; j < statusmap[i].length; j++) {
                if(statusmap[i][j] == Maze.Status.DEAD)
                    maze[i][j] = 0;
                else
                    maze[i][j] = 1;
            }
        }

        return maze;
    }

    private int isValid(int numb) {
        if (numb>0)
            return 1;
        else
            return 0;
    }

    public Point[] getIntersections(int[][] map) {
        ArrayList<Point> chokepoints = new ArrayList<Point>();
        for(int i = 1; i < map.length - 1; i++) {
            for(int j = 0; j < map[i].length; j++) {
                // Recalculate spots that arent dead (1s)
                if(map[i][j] == 1)
                    map[i][j] = isValid(map[i-1][j]) + isValid(map[i+1][j]) + isValid(map[i][j-1]) + isValid(map[i][j+1]) - 1;
                // If the spot has become an intersection, add to list of intersections
                if(map[i][j]>1)
                    chokepoints.add(new Point(i,j));
            }
        }
        return chokepoints.toArray(new Point[chokepoints.size()]);
    }
    
    public Mode getMode(){
        //returns the current mode of the ghost
        
        return GhostMode;
        
    }

    public int getSpeed() {
        //returns the travelling speed of the ghost.
        return GhostSpeed;
    }
    
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
            GhostSpeed = newSpeed;
        } else {
            throw new IllegalArgumentException("Error - Newspeed not in speed array");
        }
            
    }
    
    public java.awt.Color getColour(){
        //returns the colour of the ghost. 
        return GhostColour;
    }
    
    public void setColour(java.awt.Color colour){
        //sets the colour of the ghost. 
        GhostColour = colour;
    }
    
    public void setMode(Mode m, Target t){
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

    // default to using ghost
    public Point getGridPosition() {
        return getGridPosition(GhostPosition);
    }
    public Point getGridPosition(Point target) {
        return new Point(target.x/MazeViewer.CELL_SIZE,target.y/MazeViewer.CELL_SIZE);
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
    
    public boolean atGrid() {
        return ((GhostPosition.x-MazeViewer.CELL_SIZE/2)%MazeViewer.CELL_SIZE == 0) && ((GhostPosition.y-MazeViewer.CELL_SIZE/2)%MazeViewer.CELL_SIZE == 0);
    }
    
    public void move(Maze maze){
        if(globalmaze==null) {
            globalmaze = calculateMaze(maze);
            chokepoints = getIntersections(globalmaze);
        }

        current_path = astar(chokepoints[0]);
        for(Point xy : current_path) {
            System.out.println(xy.x+", "+xy.y);
        }

        //automatically moves the ghost in the user specified maze to the next position with proper orientation.
        boolean doMove = false;
        
        pacman = maze.getPacMan();
        pacmanpos = pacman.getPosition();
        
        //conditions to collide
        int xdiff = Math.abs(GhostPosition.x - pacmanpos.x);
        int ydiff = Math.abs(GhostPosition.y - pacmanpos.y);

        if(((xdiff == 0 && ydiff <= 2) || (xdiff <= 2 && ydiff == 0)) && !dead){
            maze.doCollide(isPanic(), GhostPosition);
            if(isPanic())
                dead = true;
        }
        
        if(atGrid()){
            Orientation[] possibleChoices = getOrientations(maze);
            
           if(dead){
                GhostOrientation = targetDirection(getGridPosition(OriginalPosition),maze);
                
                if(Math.abs(GhostPosition.x - OriginalPosition.x) <= 4 && Math.abs(GhostPosition.y - OriginalPosition.y) <= 4)
                    dead = false;

            } else {
                // If Alive!
                switch(GhostMode){
                    case PANIC:
                        //random from possible directions
                        GhostOrientation = possibleChoices[(int) (Math.random()*possibleChoices.length)];
                        break;
                    
                    case SCATTER: 
                        //follow scatter target
                        GhostOrientation = targetDirection(scatter_target, maze, true);
                        break;
                    
                    case CHASE:
                        if(GhostTarget == Target.PACMAN){
                            //follow pacman
                            GhostOrientation = targetDirection(maze.getPacMan().getPosition(), maze);
                        } else {
                            //offset
                            System.out.println("This never gets called");
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

    private Point getNearestChokePoint(Point pacman) {
        ArrayList<Point> candidates = new ArrayList<Point>();

        // Ghost is on opposite side
        // Pacman cant beat the ghost there
        for(Point chokepoint : chokepoints) {
            // If pacman 
            if(pacman.x > 1) {

            }
        }

        return new Point();
    }

    private Orientation targetDirection(ArrayList<Point> steps) {

        return Orientation.LEFT;
    }
    
    private Orientation targetDirection(Point target, Maze maze, boolean doScale){
        int scale = 1;

        if(doScale)
            scale = MazeViewer.CELL_SIZE;
        
        Orientation[] possibleOrientations = getOrientations(maze);
        Point ghostpos = getGridPosition(nextPos(GhostPosition,GhostOrientation));

        // If only one option, go that way
        if(possibleOrientations.length == 1)
            return possibleOrientations[0];

        // ELSE: Calculate the next move
        Orientation nextOrientation = possibleOrientations[0];
        int bestDist = 99;

        //find path that gets you shortest manhattan distance
        for(int i = 0; i < possibleOrientations.length; i++) {

            Point other_move = getGridPosition(nextPos(GhostPosition,possibleOrientations[i]));

            // Set target to pacman if chasing
            if(GhostMode == Mode.CHASE && !dead)
                target = getGridPosition(nextPacmanPos());

            int tempDist = Math.abs(target.x - other_move.x) + Math.abs(target.y - other_move.y);
            
            // If this distance is better
            if(tempDist == bestDist && (int)(Math.random() * (2)) == 1) {
                //nextOrientation = possibleOrientations[i];
            } //else
                if(tempDist <= bestDist){
                    bestDist = tempDist;
                    nextOrientation = possibleOrientations[i];
                }
        }
        return nextOrientation;
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
    private Point nextPacmanPos() {
        //System.out.println(GhostPosition.x+", "+GhostPosition.y);
        //System.out.println(pacmanpos.x+", "+pacmanpos.y);
        Point position = new Point(pacmanpos); //translate copy of point
        int dist = pacman.getSpeed()/(MazeViewer.CELL_SIZE/2);

        switch (pacman.getOrientation()){
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

    /*
     *  A-Star algorithm
     */
    private ArrayList<Point> astar(Point goal) {
        // ClosedSet, OpenSet and Path
        HashMap<Point,Integer> openSet = new HashMap<Point,Integer>();
        HashMap<Point,Integer> g_score = new HashMap<Point,Integer>();
        HashMap<Point,Point> came_from = new HashMap<Point,Point>();
        HashMap<Point,Orientation> coming_from = new HashMap<Point,Orientation>();
        ArrayList<Point> closedSet = new ArrayList<Point>();
        
        Point start = getGridPosition();
        coming_from.put(start,GhostOrientation);

        g_score.put(start,0); // start g score = 0;
        openSet.put(start,heuristic(start,goal)); // 0 + heuristic;

        while(!openSet.isEmpty()) {
            //choose the lowest cost node
            Point current = lowest_cost_node(openSet);
            if(current.equals(goal))
                return reconstruct_path(came_from,came_from.get(goal));

            openSet.remove(current);
            ArrayList<Point> neighbours = neighbour_nodes(current,coming_from.get(current));

            if(neighbours.size() > 0)
                closedSet.add(current);

            for(Point neighbour : neighbours) {
                if(closedSet.contains(neighbour))
                    break;

                int temp_g_score = g_score.get(current) + 1; // add one distance between the node

                if (!g_score.containsKey(neighbour) || temp_g_score < g_score.get(neighbour)) {

                    came_from.put(neighbour,current);
                    coming_from.put(neighbour,getOrientationFrom(neighbour,current));
                    g_score.put(neighbour,temp_g_score);

                    if(!openSet.containsKey(neighbour)) {
                        int temp_f_val = g_score.get(neighbour) + heuristic(neighbour, goal);
                        openSet.put(neighbour,temp_f_val);
                    }

                }
            }
        }

        return new ArrayList<Point>();
    }

    private Orientation getOrientationFrom(Point nEW, Point old) {
        if (nEW.x == old.x - 1)
            return Orientation.RIGHT;
        if (nEW.x == old.x + 1)
            return Orientation.LEFT;
        if (nEW.y == old.y + 1)
            return Orientation.UP;
        if (nEW.y == old.y - 1)
            return Orientation.DOWN;

        return null;
    }

    private ArrayList<Point> neighbour_nodes(Point p, Orientation coming_from) {
        ArrayList<Point> neighbours = new ArrayList<Point>();
        try {
            if(globalmaze[p.x-1][p.y] != 0 && coming_from!=Orientation.LEFT) //left
                neighbours.add(new Point(p.x-1,p.y));
        } catch(Exception er) {}

        try {
            if(globalmaze[p.x+1][p.y] != 0 && coming_from!=Orientation.RIGHT) //right
                neighbours.add(new Point(p.x+1,p.y));
        } catch(Exception er) {}

        try {
            if(globalmaze[p.x][p.y-1] != 0 && coming_from!=Orientation.UP) //up
                neighbours.add(new Point(p.x,p.y-1));
        } catch(Exception er) {}

        try {
            if(globalmaze[p.x][p.y+1] != 0 && coming_from!=Orientation.DOWN) //down
                neighbours.add(new Point(p.x,p.y+1));
        } catch(Exception er) {}

        return neighbours;
    }

    private Point lowest_cost_node(HashMap<Point,Integer> set) {
        Point current = null;
        int f_value = -1; // not set

        for(Entry<Point, Integer> entry : set.entrySet()) {
            if(entry.getValue() < f_value || f_value == -1) {
                current = entry.getKey();
                f_value = entry.getValue();
            }
        }
        return current;
    }

    private int heuristic(Point current, Point goal) {
        // Manhattan distance heuristic
        return Math.abs(current.x - goal.x) + Math.abs(current.y - goal.y);
    }

    private ArrayList<Point> reconstruct_path(HashMap<Point,Point> came_from, Point current) {
        ArrayList<Point> total_path = new ArrayList<Point>();
        total_path.add(current);

        while(came_from.containsKey(current)) {
            current = came_from.get(current);
            total_path.add(current);
        }

        return total_path;
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
        
        ArrayList<Orientation> tempOri = new ArrayList<Orientation>();
        
        if(!(maze.locationStatus(nextPos(GhostPosition, Orientation.UP)) == Maze.Status.DEAD)) //if maze is not dead    
            if(GhostOrientation != Orientation.DOWN) //and haven't just come that way
                tempOri.add(Orientation.UP);
        
        if(!(maze.locationStatus(nextPos(GhostPosition, Orientation.DOWN)) == Maze.Status.DEAD))
            if(GhostOrientation != Orientation.UP)
                tempOri.add(Orientation.DOWN);
        
        if(!(maze.locationStatus(nextPos(GhostPosition, Orientation.LEFT)) == Maze.Status.DEAD))
            if(GhostOrientation != Orientation.RIGHT && PreviousOrientation != Orientation.RIGHT)
                tempOri.add(Orientation.LEFT);
        
        if(!(maze.locationStatus(nextPos(GhostPosition, Orientation.RIGHT)) == Maze.Status.DEAD))
            if(GhostOrientation != Orientation.LEFT && PreviousOrientation != Orientation.LEFT)
                tempOri.add(Orientation.RIGHT);
        
        Orientation[] orientations = tempOri.toArray(new Orientation[tempOri.size()]);
        
        return orientations;
    }
    
    static public void setToOriginal(Ghost[] ghosts){
        
        for(int i = 0; i < ghosts.length; i++){
            
            ghosts[i].GhostPosition = ghosts[i].OriginalPosition;
            
        }
    }
}
