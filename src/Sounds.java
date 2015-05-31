
/**
 * Write a description of class Sounds here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */

import java.io.InputStream;
import java.io.File;
import javax.sound.sampled.*;

import java.net.MalformedURLException;
import java.io.IOException;

public class Sounds
{

    private Clip clip;
    
    public enum Sound {MUSIC_OPENING, MUSIC_INTERMISSION,
                            EAT_CHERRY, EAT_DOT, EAT_GHOST,
                            PACMAN_DEATH, PACMAN_LIVES };

    public Sounds(Sound type, boolean play)
    {
        playSound(type, play);
    }

    public void play(Sound type){
        
        if(!clip.isActive()){
            playSound(type, true);
        }
    }
    
    public void loop(boolean doLoop){
        if(doLoop){
            //clip.loop(Clip.LOOP_CONTINUOUSLY);
        } else {
            //clip.stop();
        }
    }
    
    private void playSound(Sound type, boolean play){
        
        try {
            
            String filelocation;
            
            switch(type){
                case MUSIC_OPENING:      filelocation = "sounds/music_opening.wav";         break;
                case MUSIC_INTERMISSION: filelocation = "sounds/music_intermission.wav";    break;
                case EAT_CHERRY:         filelocation = "sounds/eat_cherry.wav";            break;
                case EAT_DOT:            filelocation = "sounds/eat_dot.wav";               break;
                case EAT_GHOST:          filelocation = "sounds/eat_ghost.wav";             break;
                case PACMAN_DEATH:       filelocation = "sounds/pacman_death.wav";          break;
                default: filelocation = "";
            }
            
            AudioInputStream stream = AudioSystem.getAudioInputStream(new File(filelocation));
        
            AudioFormat format = stream.getFormat();

            // Create the clip
            DataLine.Info info = new DataLine.Info(
                Clip.class, stream.getFormat(), ((int)stream.getFrameLength()*format.getFrameSize()));
            
                clip = (Clip) AudioSystem.getLine(info);
            
                // This method does not return until the audio file is completely loaded
                clip.open(stream);
                // Start playing
                //if(play)
                //clip.start();
            
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        } catch (LineUnavailableException e) {
        } catch (UnsupportedAudioFileException e) {
        }
        
    }
    
}



/* Usage: 
 * 
 *        sound = new Sounds(Sounds.Sound.PACMAN_DEATH);
 * 
 */