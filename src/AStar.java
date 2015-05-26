
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class AStar {
    private Orientation     GhostOrientation;
    private Point           GhostPosition;
    private int[][]         globalmaze;

    public static enum Orientation{UP,DOWN,LEFT,RIGHT};

	public static void main(String[] args) {
		int[][] map = new int[][]{
						{0,0,0,0,0,0,0,0,0},
						{0,1,1,1,1,1,1,1,0},
						{0,1,0,0,0,1,0,1,0},
						{0,1,1,0,1,1,1,1,0},
						{0,0,1,0,1,0,0,0,0},
						{0,0,1,1,1,0,0,0,0}
					};
		new AStar(map);
	}

	public AStar(int[][] map) {
		globalmaze = map;
		GhostOrientation = Orientation.LEFT;
		for(Point xy : astar(new Point(5,4))) {
            System.out.println(">>>>"+xy.x+", "+xy.y);
        }
	}
	
    private ArrayList<Point> astar(Point goal) {
        // ClosedSet, OpenSet and Path
        HashMap<Point,Integer> openSet = new HashMap<Point,Integer>();
        HashMap<Point,Integer> g_score = new HashMap<Point,Integer>();
        HashMap<Point,Point> came_from = new HashMap<Point,Point>();
        HashMap<Point,Orientation> coming_from = new HashMap<Point,Orientation>();
        ArrayList<Point> closedSet = new ArrayList<Point>();
        
        Point start = new Point(2,1);
        coming_from.put(start,GhostOrientation);

        g_score.put(start,0); // start g score = 0;
        openSet.put(start,heuristic(start,goal)); // 0 + heuristic;

        while(!openSet.isEmpty()) {
            //choose the lowest cost node
            Point current = lowest_cost_node(openSet);

        	for(Entry<Point,Integer> item : openSet.entrySet()) {
        		System.out.println(item.getKey().y+", "+item.getKey().x + ": "+item.getValue());
        	}
        	System.out.println(">"+current.y+", "+current.x+": "+openSet.get(current));
        	System.out.println();

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
        System.out.println("Found path!");
        ArrayList<Point> total_path = new ArrayList<Point>();
        total_path.add(current);

        while(came_from.containsKey(current)) {
            current = came_from.get(current);
            total_path.add(current);
        }

        return total_path;
    }
}
