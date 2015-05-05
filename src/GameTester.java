
/**
 * Initial Tester for setting up a Pac-Man Game
 * 
 * @author Wei Liu
 * @version Semester 2, 2009
 */
import java.awt.Point;
import java.awt.Color;
public class GameTester
{

    public static void main(String[] args){
        boolean[][] map = new boolean[28][36];
        setLegal(map);
                
        int[][] noDots = new int[54][2];
        setNoDots(noDots);
       
        int[][] engPos = {{1,6},{26,6},{1,26},{26,26}};
        
        Ghost[] ghosts = new Ghost[4];
        int[][] scatterTargets = {{3,6},{24,6},{3,31},{24,31}};

        Color[] colours = new Color[4];
        colours[0] = new Color(255,0,0);
        colours[1] = new Color(255,0,255);
        colours[2] = new Color(24,164,31);
        colours[3] = new Color(231,143,24);       
        
        createGhosts(ghosts, 13, 14, scatterTargets, colours);
       
        PacMan pm = new PacMan(new Point(MazeViewer.CELL_SIZE*4 + MazeViewer.CELL_SIZE/2, 
                                MazeViewer.CELL_SIZE*17+MazeViewer.CELL_SIZE/2));

        Maze maze = new Maze(map, noDots,engPos,ghosts, pm);
        
        MazeViewer gv = new MazeViewer(maze);
        
        //You can comment out the line below if you just want to test out the drawings.
        gv.animate();
        
    }  
    
    private static void setLegal(boolean[][] map){
        
        for(int i=0; i<map.length; i++){
            for(int j=0; j<map[0].length; j++){
                map[i][j]= false;
                    
                if(j==8 || j == 32)
                    map[i][j] = true;
                
                if((j==4 || j== 23) && (i!=13&&i!=14))
                   map[i][j] = true;
                
                if((j==11 || j==29) && (i!=7 && i!=8 && i!=13&&i!=14 && i!=19 && i!=20) )
                  map[i][j] = true;
                
                if((j==14 || j== 20) && (i>9 && i<18))
                   map[i][j] = true;
                   
                if((j==26) &&(i!=4 && i!=5 && i!=22 && i!=23))
                  map[i][j] = true;
                  
               if(j<4 || j>32 || i==0 || i==27)
                    map[i][j] = false;
                    
                if((j==17) && (i<9 || i>18))
                  map[i][j] = true;  
                              
                  
                if((i==6 || i ==21) && (j>=5 && j<=28))
                  map[i][j] = true;  
                
                if((i==1 || i ==26) && ((j>=5 && j<=11) || (j>=23 && j<=26) || (j>=29 && j<=32))  )
                  map[i][j] = true;  
                  
                if((i==3 || i==24) &&(j==27 || j==28))
                  map[i][j] = true;
                
                if((i==9 || i==18) &&(j==9 || j==10 || (j>=14 && j<=22) || j==27 || j==28))
                  map[i][j] = true;
                  
               if((i==12 || i==15) &&((j>=5 && j<=7) || j==12 || j==13 || j==24 || j==25|| j==30 || j==31))
                  map[i][j] = true; 
                   
            }            
        }        
    }
    
    
    
    private static void setNoDots(int[][] noDots){
        
        int count=0;
        for(int i=10; i<=17; i++){
                    noDots[count][0] = i;
                    noDots[count][1] = 14;
                    count++;
                    
                    noDots[count][0] = i ;
                    noDots[count][1] = 20;
                    count++;
        }
        
        for(int j=14; j<=22; j++){
                    noDots[count][0] = 9;
                    noDots[count][1] = j;
                    count++;
                    
                    noDots[count][0] = 18;
                    noDots[count][1] = j;
                    count++;
        }
        
        for(int j=12; j<=13; j++){
                    noDots[count][0] = 12;
                    noDots[count][1] = j;
                    count++;
                    
                    noDots[count][0] = 15;
                    noDots[count][1] = j;
                    count++;
        }
        for(int i=0; i<=27; i++){
            if(i!=6 && i!=21 && (i<9 || i>18)){
                noDots[count][0] = i;
                noDots[count][1] = 17;
                count++;
            }
        }
    }
    
    private static void createGhosts(Ghost[] ghosts, int i, int j, int[][] scatterTarget, Color[] colours){
        for(int m = 0; m<ghosts.length; m++){
            ghosts[m] = new Ghost(new Point((i+m)*MazeViewer.CELL_SIZE + MazeViewer.CELL_SIZE/2,
                                                                    j*MazeViewer.CELL_SIZE+MazeViewer.CELL_SIZE/2),
                                                                    colours[m], scatterTarget[m]);
        }
    }
}
