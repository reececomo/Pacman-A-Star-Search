import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Stack;
import java.util.PriorityQueue;

/**
 * CITS3001
 * Algorithms, Agents and Artificial Intelligence
 * 
 * 
 * The Pincer Strategy Ghosts
 * 	Ghosts try to envelope the Pac-Man
 *  by blocking off surrounding exits
 * 
 * @author Reece Notargiacomo (21108155)
 * @version Sunday 31st May
 */

public class Ghost
{
    // Constants
    public static final Color 	PANIC_COLOUR = new Color(60,100,175);
    public static final int[] 	SPEED={0,1,2,4,8,16,32};
    public static final int 	OFFSET = 4;
    
    // Built in variables
    private Orientation     PreviousOrientation;
    private int[]           GhostScatterTarget;
    private Point           OriginalPosition;
    private Orientation     GhostOrientation;
    private Point           GhostPosition;
    private Mode            PreviousMode;
    private Target			GhostTarget;
    private Color  			GhostColour;
    private int             GhostSpeed;
    private Mode            GhostMode;
    private boolean         dead;
    
    // Enumerated types
    public static enum 		Target {PACMAN,OFFSET,AIMLESS,SCATTER};
    public static enum 		Orientation{UP,DOWN,LEFT,RIGHT};
    public static enum 		Mode{CHASE,SCATTER,PANIC};
    
    
    // Add in the ghost colors
    private Color 			pink = new Color(255,0,255),
						    red = new Color(255,0,0),
						    green = new Color(24,164,31),
						    orange = new Color(231,143,24);
    
    /*
     *  Reece's Variables
     */
    private Point[]         chokepoints;
    private Stack<Node> 	CurrentPath;
    private Point			CurrentGoal;
    private int[][]         GlobalMaze;
    private PacMan          PacMan;
    
    /*
     *  Constructor
     */
    public Ghost(Point pos, Color colour, int[] scatterTarget) {
        PreviousOrientation = GhostOrientation;
        GhostScatterTarget = scatterTarget;
        GhostOrientation = Orientation.UP; // Default Orientation UP
        OriginalPosition = new Point(pos);
        setSpeed(MazeViewer.CELL_SIZE);
        GhostTarget = Target.SCATTER; // Default Target SCATTER
        GhostMode = Mode.SCATTER;
        PreviousMode = GhostMode;
        GhostColour = colour;
        GhostPosition = pos;
        dead = false;
        
        // A-Star Efficiency Helpers
        CurrentGoal = null;
        CurrentPath = new Stack<Node>();
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
        //changes the travelling speed to newSpeed
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
    
    public Color getColour(){
        //returns the colour of the ghost. 
        return GhostColour;
    }
    
    public String ghostColour() {
    	if(GhostColour.equals(pink))
    		return "pink";
    	if(GhostColour.equals(red))
    		return "red";
    	if(GhostColour.equals(green))
    		return "green";
    	
    	return "orange";
    }
    
    public void setColour(Color colour){
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
    
    public boolean checkForCollision(Point A, Point B) {
        //conditions to collide
        int xdiff = Math.abs(A.x - B.x);
        int ydiff = Math.abs(A.y - B.y);
        
        return (((xdiff == 0 && ydiff <= 2) || (xdiff <= 2 && ydiff == 0)) && !dead);
    }
    
    
    /*
     * The MOVE function
     * 
     *  Minimax and A* search are in here
     * 
     */
    public void move(Maze maze){
        
        if(GlobalMaze==null) {
            PacMan = maze.getPacMan();
            GlobalMaze = calculateMaze(maze);
            chokepoints = getIntersections(GlobalMaze);
        }
        
        // Check if touching Pac-Man
        if(checkForCollision(GhostPosition,PacMan.getPosition())) {
        	maze.doCollide(isPanic(), GhostPosition);
            if(isPanic())
                dead = true;
        }
        
        if(!atGrid()) {
        	doMove();
        	return;
        } else {
        	if(!dead) {
	        	Orientation[] possible = getOrientations(maze);
	        	if(possible.length == 0) {
	        		GhostOrientation = possible[0];
		        	doMove();
		        	return;
	        	}
        	}
        }
       if(dead){
    	    // Go back home
           GhostOrientation = aStarNextDirection(getGridPosition(OriginalPosition),maze);
           if(Math.abs(GhostPosition.x - OriginalPosition.x) <= 4 && Math.abs(GhostPosition.y - OriginalPosition.y) <= 4)
                dead = false;
        } else {
            // Minimax (commented out)
        	//GhostOrientation = MinimaxChooseBest();
    		
        	// A* Search
        	Point pacManPosition = getGridPosition(PacMan.getPosition());
        	Point[] chokepoint = getFourNearestChokePoints(pacManPosition);
        	
        	if(GhostColour.equals(pink))
        		GhostOrientation = aStarNextDirection(chokepoint[0],maze);
        	if(GhostColour.equals(red))
        		GhostOrientation = aStarNextDirection(chokepoint[1],maze);
        	if(GhostColour.equals(green))
        		GhostOrientation = aStarNextDirection(chokepoint[2],maze);
        	if(GhostColour.equals(orange))
        		GhostOrientation = aStarNextDirection(chokepoint[3],maze);
        }

       	// Make the next move
        switch (maze.locationStatus(nextPos(GhostPosition,GhostOrientation))){
            case INVALID:
            	//wrap around to other side of screen
                if (GhostPosition.x == MazeViewer.CELL_SIZE/2){ //left side
                    GhostPosition.x = maze.getMap().length*MazeViewer.CELL_SIZE-MazeViewer.CELL_SIZE/2;
                    
                } else if (GhostPosition.x == maze.getMap().length*MazeViewer.CELL_SIZE-MazeViewer.CELL_SIZE/2){ //right side
                    GhostPosition.x = MazeViewer.CELL_SIZE/2;
                }
            case DEAD:
            	break;
                
            case ENERGISER:
            case LEGAL:
            case DOT:
            	doMove();
        }
    
    }
    
    private class State {
    	public Point Pacman;
    	public Point Ghost;
    	public Orientation GhostDirection;
    	public boolean IS_GHOST_TURN = false;
    	
    	public State() {
    		Ghost = getGridPosition();
    		Pacman = getGridPosition(PacMan.getPosition());
    		GhostDirection = GhostOrientation;
    		IS_GHOST_TURN = true;
    	}
    	
    	public State(Point p, Orientation o, Point pac, boolean ghosts_turn) {
    		Ghost = p;
    		GhostDirection = o;
    		Pacman = pac;
    		IS_GHOST_TURN = ghosts_turn;
    	}
    	
    	private boolean isLeaf() {
    		return heuristic(Ghost, Pacman) == 0;
    	}
    	
    	// Get all children states
    	private ArrayList<State> children() {
    		Node cur_pacman = new Node(Pacman);
    		Node cur_ghost = new Node(Ghost);
    		cur_ghost.orientation = GhostDirection;
    		
    		ArrayList<State> children = new ArrayList<State>();
    		ArrayList<Point> adjacent = null;
    		
    		if(IS_GHOST_TURN) {
        		adjacent = get_adjacent(cur_ghost);

        		for(Point next_ghost : adjacent) {
        			Orientation new_orientation = getOrientationFrom(cur_ghost,next_ghost);
        			children.add(new State(next_ghost,new_orientation, Pacman, false));
        		}
    		} else {
        		adjacent = get_adjacent(cur_pacman);

        		for(Point next_pacman : adjacent)
        			children.add(new State(Ghost, GhostDirection, next_pacman, true));
    		}
    		
    		return children;
    	}
    }
    
    private double Minimax(State n, int depth, double min, double max) {
    	if(n.isLeaf() || depth == 0)
    		return Utility(n);
    	
    	if(n.IS_GHOST_TURN) {
    		double v = min;
    		for(State child : n.children()) {
    			double tempv = Minimax(child,depth-1,v,max);
    			if (tempv > v)
    				v = tempv;
    			if(v>max)
    				return max;
    		}
    		
    		return v;
    	}
    	
    	else // IS_PACMAN_TURN
    	{
    		double v = max;
    		for(State child : n.children()) {
    			double tempv = Minimax(child,depth-1,min,v);
    			if (tempv < v)
    				v = tempv;
    			if(v<min)
    				return min;
    		}
    		
    		return v;
    	}
    }
    
    // Utility function
    private double Utility(State state) {
    	int actual_distance = heuristic(getGridPosition(),getGridPosition(PacMan.getPosition()));
    	int distance = heuristic(state.Ghost, state.Pacman);
    	
    	// Leaf state
    	if (distance == 0) {
    		if(isPanic())
    			return Double.NEGATIVE_INFINITY; // Ghost loses
    		else
    			return Double.POSITIVE_INFINITY; // Ghost wins
    	}
    	
    	if(isPanic())
    		return  distance - actual_distance; // moving closer is bad
    	else
    		return actual_distance - distance; // moving closer is good
    }
    
    // Choosing the best option from minimax
    private Orientation MinimaxChooseBest() {
    	State current_state = new State();
    	Orientation best_choice = GhostOrientation;
    	double score = Double.NEGATIVE_INFINITY;

    	for(State choice : current_state.children()) {
    		double temp_score = Minimax(choice, 15, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    		if(temp_score > score)
    			best_choice = choice.GhostDirection;
    	}
    	
    	return best_choice;
    }
    
    private void doMove() {
        PreviousOrientation = GhostOrientation;
        GhostPosition = nextPos2(GhostPosition, GhostOrientation);
    }

    private Point[] getFourNearestChokePoints(Point pacman) {
    	Point[] max = new Point[4];
    	int amax1 = 99, amax2 = 99, amax3 = 99, amax4 = 99;
    	
        for (int i = 0; i < chokepoints.length; i++) {
        	int heur_calc = heuristic(pacman, chokepoints[i]);
        	
            if (heur_calc < amax1) {
                max[3] = max[2]; max[2] = max[1]; max[1] = max[0]; max[0] = chokepoints[i];
                amax4 = amax3; amax3 = amax2; amax2 = amax1; amax1 = heur_calc;
            }
            else if (heur_calc < amax2)
            {
            	max[3] = max[2]; max[2] = max[1]; max[1] = chokepoints[i];
                amax4 = amax3; amax3 = amax2; amax2 = heur_calc;
            }
            else if (heur_calc < amax3)
            {
            	max[3] = max[2]; max[2] = chokepoints[i];
                amax4 = amax3; amax3 = heur_calc;
            }
            else if (heur_calc < amax4)
            {
                max[3] = chokepoints[i];
                amax4 = heur_calc;
            }
        }
        
        return max;
    }
    
    /*
     * Get the next direction to stay on path
     */
    private Orientation aStarNextDirection(Point Goal, Maze maze) {
    	// If no more moves or new target, then recalculate
    	if(CurrentPath.size() < 2 || !Goal.equals(CurrentGoal)) {
    		CurrentGoal = Goal;
    		CurrentPath = astar(this.getGridPosition(), GhostOrientation, Goal);
    	}

    	// If you are on the path, follow the next orientation
    	if(CurrentPath.pop().equals(this.getGridPosition())
    			&& !CurrentPath.isEmpty()) {
    		return CurrentPath.peek().orientation;
    	}
    	
    	// Erase the current path, stay in same direction
    	CurrentPath.clear();
    	return GhostOrientation;
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
    
    /*
     *  Node for A-Star algorithm
     */
    class Node extends Point implements Comparable<Node> {
		private static final long serialVersionUID = 1L;
		int g_value = -1; //not set
    	int f_value = -1;
    	boolean closed = false;
    	
    	Node came_from = null;
    	Orientation orientation = null;
    	
    	public Node(int px, int py) {
    		this.x = px;
    		this.y = py;
    	}
    	public Node(Point p) {
    		this.x = p.x;
    		this.y = p.y;
    	}
    	
    	// Check if two nodes are equivalent
    	@Override
    	public boolean equals(Object obj) {
    		// The object can be either a Node or a Point
    		if(obj == null || !(obj instanceof Node || obj instanceof Point))
    			return false;
    		
    		if(obj == this)
    			return true;
    		
    		// Test if they have the same x and y
    		if(obj instanceof Node) {
	    		Node node = (Node) obj;
	    		return this.x == node.x
	    				&& this.y == node.y;
    		}
    		if(obj instanceof Point) {
	    		Point node = (Point) obj;
	    		return this.x == node.x
	    				&& this.y == node.y;
    		}
    		
    		return false;
    	}
    	
    	// Compares two nodes
    	@Override
    	public int compareTo(Node node) {
    		return this.f_value - node.f_value;
    	}
    }
    

    /*
     *  A-Star algorithm
     */
    private Stack<Node> astar(Point startPoint, Orientation startOri, Point goalPoint) {
        // Open Set priority queue
        PriorityQueue<Node> openSet = new PriorityQueue<Node>();
        
        Node start = new Node(startPoint);
        Node goal = new Node(goalPoint);
        
        start.orientation = startOri;
        start.g_value = 0; // exact distance from start = 0;
        start.f_value = start.g_value + heuristic(startPoint,goalPoint); // f(a) = g(a) + h(a);
        openSet.add(start);

        while(!openSet.isEmpty()) {
            Node current = openSet.poll(); // Pull the lowest cost node
            
            if(current.equals(goal))
                return construct_path(current);

            ArrayList<Point> adjacent_nodes = get_adjacent(current);

            if(adjacent_nodes.size() == 0)
                current.closed = true;
            
            for(Point point : adjacent_nodes) {
            	Node node = getNodeFrom(point,openSet);
            	
                if(node.closed)
                    break;

                int temp_g_val = current.g_value + 1; // movement cost = 1

                if (node.g_value == -1 || temp_g_val < node.g_value) {
                    node.came_from = current;
                    node.orientation = getOrientationFrom(current,node);
                    node.g_value = temp_g_val;

                    if(!openSet.contains(node)) {
                        node.f_value = node.g_value + heuristic(node, goal);
                        openSet.add(node);
                    }

                }
            }
        }

        throw new NullPointerException("No path to "+goalPoint.x+", "+goalPoint.y);
    }
    
    private Node getNodeFrom(Point point, Collection<Node> collection) {
    	for (Node node : collection)
    		if (node.equals(point))
    			return node;
    	
    	return new Node(point);
    }

    private Orientation getOrientationFrom(Point A, Point B) {
        if (A.x == B.x - 1)
            return Orientation.RIGHT;
        if (A.x == B.x + 1)
            return Orientation.LEFT;
        if (A.y == B.y + 1)
            return Orientation.UP;
        if (A.y == B.y - 1)
            return Orientation.DOWN;

        return Orientation.LEFT;
    }
    
    private int valid_coordinate(int nx, int ny) {
    	// return wrap-around
    	if (!(nx > 0 && nx < GlobalMaze.length - 1 && ny > 0 && ny < GlobalMaze[nx].length -1))
    		return -1;
    	
    	// returns true if coordinate is a valid spot
    	return GlobalMaze[nx][ny];
    }

    private ArrayList<Point> get_adjacent(Node p) {
        ArrayList<Point> neighbours = new ArrayList<Point>();
        
        // If the coordinate is a valid spot and didn't just come from that direction
        if(valid_coordinate(p.x-1,p.y) > 0 && p.orientation != Orientation.RIGHT)
            neighbours.add(new Point(p.x-1,p.y));
        
        if(valid_coordinate(p.x+1,p.y) > 0 && p.orientation != Orientation.LEFT)
            neighbours.add(new Point(p.x+1,p.y));

        if(valid_coordinate(p.x,p.y-1) > 0 && p.orientation != Orientation.DOWN)
            neighbours.add(new Point(p.x,p.y-1));

        if(valid_coordinate(p.x,p.y+1) > 0 && p.orientation != Orientation.UP)
            neighbours.add(new Point(p.x,p.y+1));
        
        // Wrap-around for tunnels
        if(valid_coordinate(p.x-1,p.y) == -1 && p.orientation != Orientation.RIGHT)
        	neighbours.add(new Point(GlobalMaze.length-1,p.y));
        
        if(valid_coordinate(p.x+1,p.y) == -1 && p.orientation != Orientation.LEFT)
        	neighbours.add(new Point(0,p.y));

        return neighbours;
    }

    private int heuristic(Point current, Point goal) {
        // Manhattan distance heuristic
        return Math.abs(current.x - goal.x) + Math.abs(current.y - goal.y);
    }

    private Stack<Node> construct_path(Node current) {
        Stack<Node> path = new Stack<Node>();
        
        path.push(current);

        while(current.came_from != null) {
            current = current.came_from;
            path.push(current);
        }

        return path;
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
    
    public int[][] calculateMaze(Maze inputmaze) {
        Maze.Status[][] statusmap = inputmaze.getMap();

        int[][] maze = new int[statusmap.length][];

        for(int i = 0; i < statusmap.length; i++) {
            maze[i] = new int[statusmap[i].length];
            for(int j = 0; j < statusmap[i].length; j++) {
                if(statusmap[i][j] == Maze.Status.DEAD)
                    maze[i][j] = 0;
                else {
                    maze[i][j] = 1;
                }
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
    
    static public void setToOriginal(Ghost[] ghosts){
        for(int i = 0; i < ghosts.length; i++){
            ghosts[i].GhostPosition = ghosts[i].OriginalPosition;
            ghosts[i].GhostOrientation = Orientation.UP;
        }
    }
}
