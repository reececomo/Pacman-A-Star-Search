    /**
     * 
     * Canvas with some modifications
     * 
     * @author BlueJ team, modifications by Alex Louden
     * @version July 2003
     */
    
    import java.awt.geom.*;
    import java.awt.Color;
    import java.awt.Dimension;
    import java.awt.Font;
    import java.awt.Graphics;
    import java.awt.Graphics2D;
    import java.awt.Image;
    import java.awt.Rectangle;
    import java.awt.Shape;
    import java.awt.BasicStroke; //for fat lines
    import java.awt.RenderingHints; //antianialising
    
    import java.awt.event.*; //for close upon canvas close
    
    import javax.swing.JFrame;
    import javax.swing.JPanel;
    
    /**
     * Class Canvas - a class to allow for simple graphical
     * drawing on a canvas.
     */
    
    public class Canvas
    {
        
        private PacMan pacman;
        private MazeViewer mazeviewer;
        
        private static final String TITLE = "Alex's PacMan";
        private static final java.awt.Color BGCOLOUR = java.awt.Color.black;
        
        private JFrame frame;
        private CanvasPane canvas;
        private Graphics2D graphic;
        private Color backgroundColour;
        private Image canvasImage;
    
        /**
         * Create a Canvas.
         * @param title  title to appear in Canvas Frame
         * @param width  the desired width for the canvas
         * @param height  the desired height for the canvas
         * @param bgClour  the desired background colour of the canvas
         */
        
        public Canvas(int width, int height, PacMan pm, MazeViewer mv)
        {
            pacman = pm;
            mazeviewer = mv;
            
            frame = new JFrame();
            canvas = new CanvasPane();
            frame.setContentPane(canvas);
            frame.addKeyListener(new MyKeyListener());
            frame.setTitle(TITLE);
            frame.setResizable(false); //no resizing
            canvas.setPreferredSize(new Dimension(width, height));
            backgroundColour = BGCOLOUR;
            frame.pack();
            setVisible(true);
            
    //Close upon window exit
            frame.addWindowListener(new WindowAdapter(){
                public void windowClosing(WindowEvent we){
                    System.exit(0);
                }
            });
    
        }
    
        /**
         * Set the canvas visibility and brings canvas to the front of screen
         * when made visible. This method can also be used to bring an already
         * visible canvas to the front of other windows.
         * @param visible  boolean value representing the desired visibility of
         * the canvas (true or false)
         */
        public void setVisible(boolean visible)
        {
            if(graphic == null) {
    //first time: instantiate the offscreen image and fill it with the background colour
                Dimension size = canvas.getSize();
                canvasImage = canvas.createImage(size.width, size.height);
                graphic = (Graphics2D)canvasImage.getGraphics();
                //graphic.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); makes lines skinny :(
                graphic.setColor(backgroundColour);
                graphic.fillRect(0, 0, size.width, size.height);
                graphic.setColor(Color.black);
            }
            frame.setVisible(visible);
        }
    
        /**
         * Draw a given shape onto the canvas.
         * @param  shape  the shape object to be drawn on the canvas
         */
        public void draw(Shape shape)
        {
            graphic.draw(shape);
            canvas.repaint();
        }
    
        /**
         * Fill the internal dimensions of a given shape with the current
         * foreground colour of the canvas.
         * @param  shape  the shape object to be filled
         */
        public void fill(Shape shape)
        {
            graphic.fill(shape);
            canvas.repaint();
        }
    
        /**
         * Erase the whole canvas.
         */
        public void erase()
        {
            Color original = graphic.getColor();
            graphic.setColor(backgroundColour);
            Dimension size = canvas.getSize();
            graphic.fill(new Rectangle(0, 0, size.width, size.height));
            graphic.setColor(original);
            canvas.repaint();
        }
    
        /**
         * Erase a given shape's interior on the screen.
         * @param  shape  the shape object to be erased
         */
        public void erase(Shape shape)
        {
            Color original = graphic.getColor();
            graphic.setColor(backgroundColour);
            graphic.fill(shape);              // erase by filling background colour
            graphic.setColor(original);
            canvas.repaint();
        }
    
        /**
         * Erases a given shape's outline on the screen.
         * @param  shape  the shape object to be erased
         */
        public void eraseOutline(Shape shape)
        {
            Color original = graphic.getColor();
            graphic.setColor(backgroundColour);
            graphic.draw(shape);  // erase by drawing background colour
            graphic.setColor(original);
            canvas.repaint();
        }
    
        /**
         * Draws an image onto the canvas.
         * @param  image   the Image object to be displayed
         * @param  x       x co-ordinate for Image placement
         * @param  y       y co-ordinate for Image placement
         * @return  returns boolean value representing whether the image was
         *          completely loaded
         */
        public boolean drawImage(Image image, int x, int y)
        {
            boolean result = graphic.drawImage(image, x, y, null);
            canvas.repaint();
            return result;
        }
    
        /**
         * Draws a String on the Canvas.
         * @param  text   the String to be displayed
         * @param  x      x co-ordinate for text placement
         * @param  y      y co-ordinate for text placement
         */
        public void drawString(String text, int x, int y)
        {
            graphic.drawString(text, x, y);
            canvas.repaint();
        }
    
        /**
         * Erases a String on the Canvas.
         * @param  text     the String to be displayed
         * @param  x        x co-ordinate for text placement
         * @param  y        y co-ordinate for text placement
         */
        public void eraseString(String text, int x, int y)
        {
            Color original = graphic.getColor();
            graphic.setColor(backgroundColour);
            graphic.drawString(text, x, y);
            graphic.setColor(original);
            canvas.repaint();
        }
    
        /**
         * Draws a line on the Canvas.
         * @param  x1   x co-ordinate of start of line
         * @param  y1   y co-ordinate of start of line
         * @param  x2   x co-ordinate of end of line
         * @param  y2   y co-ordinate of end of line
         */
        public void drawLine(int x1, int y1, int x2, int y2)
        {
            graphic.setStroke(new BasicStroke(2));
            graphic.drawLine(x1, y1, x2, y2);
            canvas.repaint();
        }
    
        /**
         * Sets the foreground colour of the Canvas.
         * @param  newColour   the new colour for the foreground of the Canvas
         */
        public void setForegroundColour(Color newColour)
        {
            graphic.setColor(newColour);
        }
    
        /**
         * Returns the current colour of the foreground.
         * @return   the colour of the foreground of the Canvas
         */
        public Color getForegroundColour()
        {
            return graphic.getColor();
        }
    
        /**
         * Sets the background colour of the Canvas.
         * @param  newColour   the new colour for the background of the Canvas
         */
        public void setBackgroundColour(Color newColour)
        {
            backgroundColour = newColour;
            graphic.setBackground(newColour);
        }
        
        /**
         * changes the current Font used on the Canvas
         * @param  newFont   new font to be used for String output
         */
        public void setFont(Font newFont)
        {
            graphic.setFont(newFont);
        }
    
        /************************************************************************
         * Nested class CanvasPane - the actual canvas component contained in the
         * Canvas frame. This is essentially a JPanel with added capability to
         * refresh the image drawn on it.
         */
        private class CanvasPane extends JPanel
        {
            public void paint(Graphics g)
            {
                g.drawImage(canvasImage, 0, 0, null);
            }
        }
        
        public class MyKeyListener extends KeyAdapter{
            public void keyPressed(KeyEvent e){
                if(e.getKeyCode() != KeyEvent.VK_D){ //demo
                    mazeviewer.keypress(); 
                }
                
                switch(e.getKeyCode()){
                    case KeyEvent.VK_LEFT:  pacman.setNextOrientation(Ghost.Orientation.LEFT);    break;
                    case KeyEvent.VK_RIGHT: pacman.setNextOrientation(Ghost.Orientation.RIGHT);   break;
                    case KeyEvent.VK_UP:    pacman.setNextOrientation(Ghost.Orientation.UP);      break;
                    case KeyEvent.VK_DOWN:  pacman.setNextOrientation(Ghost.Orientation.DOWN);    break;
                    case KeyEvent.VK_D:     pacman.doDemo(); break;
                }
            }
        }
    }
    
    
