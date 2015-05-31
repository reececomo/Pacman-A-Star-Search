
/**
 * Write a description of class ScoreText here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class ScoreText
{
    private int score;
    private int frameRemove;
    private java.awt.Point position;

    public ScoreText(int myScore, java.awt.Point myPosition, int frame)
    {
        score = myScore;
        position = myPosition;
        frameRemove = frame + 100;
    }

    public java.awt.Point getPosition()
    {
        return position;
    }
    
    public int getScore(){
        return score;
    }
    
    public boolean canView(int frame){
        return (frame < frameRemove);
    }
    
    public boolean doErase(int frame){
        return (frame == frameRemove);
    }
}
