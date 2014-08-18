package canvas;

import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author Ricardo Alves
 */
public abstract class CVCanvas extends JFrame
{
    static
    {
        System.loadLibrary("opencv_java247");
    }
    
    /**
     * Drawing mode.
     */
    public final static int CORNER = 1;

    /**
     * Drawing mode.
     */
    public final static int CENTER = 2;

    /**
     * Drawing mode.
     */
    public final static int CORNERS = 3;
    
    private final JLabel area;
    private volatile Mat mat;
    private volatile Mat matAux;
    private BufferedImage image;
    private byte[] data;
    private ScheduledExecutorService timerLoop;

    /**
     * The true frame rate of the sketch.
     */
    public float frameRate = 0;
    private int fps = 30;
    private int rectMode = CORNER;
    private int ellipseMode = CORNER;
    private Scalar strokeColor = new Scalar(0, 0, 0, 0);
    private Scalar fillColor = new Scalar(0, 0, 0, 0);
    private boolean fill = true;
    private int stroke = 1;

    /**
     * Stores information of key events.
     */
    public char key;

    /**
     * Stores information of mouse events.
     */
    public Point mouse = new Point(0, 0);
    private volatile boolean loop = true;

    /**
     * Constructs a Canvas
     */
    @SuppressWarnings({"OverridableMethodCallInConstructor", "CallToThreadStartDuringObjectConstruction"})
    public CVCanvas()
    {
        super();
        area = new JLabel();
        init();
        
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0];
        DisplayMode oldMode = device.getDisplayMode();  

        int width = oldMode.getWidth();
        int height = oldMode.getHeight();
        size(width, height);
        
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        CVCanvas cv = this;
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e)
            {
                key = e.getKeyChar();
                if(e.getKeyChar()==KeyEvent.VK_ESCAPE)
                {
                    closing();
                    System.exit(0);
                }
                else
                    cv.keyPressed();
            }
            
            @Override
            public void keyReleased(KeyEvent e)
            {
                key = e.getKeyChar();
                cv.keyReleased();
            }
            
            @Override
            public void keyTyped(KeyEvent e)
            {
                key = e.getKeyChar();
                cv.keyTyped();
            }
        });
        addMouseListener(new MouseAdapter(){
            @Override
            public void mousePressed(MouseEvent e)
            {
                mouse.x = e.getX();
                mouse.y = e.getY();
                cv.mousePressed();
            }
            
            @Override
            public void mouseReleased(MouseEvent e)
            {
                mouse.x = e.getX();
                mouse.y = e.getY();
                cv.mouseReleased();
            }
        });
        addMouseMotionListener(new MouseAdapter(){
            @Override
            public void mouseDragged(MouseEvent e)
            {
                mouse.x = e.getX();
                mouse.y = e.getY();
                cv.mouseDragged();
            }
        });
        setup();
        redraw();
        timerLoop = Executors.newScheduledThreadPool(1);
        if(loop)
            loop();
    }
    
    /**
     * Initializes a jFrame. Can be overrided to change its properties.
     */
    public void init()
    {        
        setResizable(false);
        setUndecorated(true);
        getContentPane().setLayout(null);
        getContentPane().add(area);
        pack();
        
        setExtendedState(getExtendedState()|MAXIMIZED_BOTH);
        addNotify();
    }

    /**
     * Sets the size of the Canvas.
     * @param s Size of the Canvas.
     */
    public void size(Size s)
    {
        size((int)s.width, (int)s.height);
    }
    
    /**
     * Sets the size of the Canvas.
     * @param width Width of the Canvas.
     * @param height Height of the Canvas.
     */
    public void size(int width, int height)
    {
        setMinimumSize(new Dimension(width, height));
        setSize(width, height);
        area.setBounds(0, 0, width, height);
        if(mat != null)
            mat.release();
        mat = new Mat(height, width, CvType.CV_8UC3);
        mat.setTo(new Scalar(0, 0, 0));
        matAux = new Mat(height, width, CvType.CV_8UC3);
        image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        data = new byte[mat.cols() * mat.rows() * (int)mat.elemSize()];
        setLocationRelativeTo(null);
    }
    
    public long millis()
    {
        return System.currentTimeMillis();
    }
    
    /**
     * Stops the loop.
     */
    public void noLoop()
    {
        loop = false;
        timerLoop.shutdownNow();
        while(!timerLoop.isShutdown());
    }
    
    /**
     * Starts the loop. It is called automatically by the constructor.
     */
    public void loop()
    {
        if(timerLoop == null)
            return;
        timerLoop.shutdownNow();
        while(!timerLoop.isShutdown());
        timerLoop = Executors.newScheduledThreadPool(1);
        timerLoop.scheduleAtFixedRate(new TimerTask(){
            long timer[] = new long[10];
            int index = 0;
            boolean full = false;

            @Override
            public void run()
            {
                draw();
                redraw();

                long aux = System.currentTimeMillis();
                if(full)
                    frameRate = (10000/(float)(aux-timer[index]));
                timer[index] = aux;
                index++;
                if(index == 10)
                {
                    index = 0;
                    full = true;
                }
            }
        }, 1000, 1000/fps, TimeUnit.MILLISECONDS);
    }

    /**
     * Sets the sketch to the specified frame rate. It is not recommended to be called on loop function.
     * @param fps
     */
    public void frameRate(int fps)
    {
        this.fps = fps;
        loop();
    }
    
//<editor-fold defaultstate="collapsed" desc="Stroke">

    /**
     * Sets the stroke color.
     * @param color The desired color.
     */
    public void stroke(Scalar color)
    {
        stroke((int)color.val[2], (int)color.val[1], (int)color.val[0], (int)color.val[3]);
    }
    
    /**
     * Sets the stroke color.
     * @param gray The desired gray tone.
     */
    public void stroke(int gray)
    {
        stroke(gray, gray, gray, 255);
    }
    
    /**
     * Sets the stroke color.
     * @param r The desired red component.
     * @param g The desired green component.
     * @param b The desired blue component.
     */
    public void stroke(int r, int g, int b)
    {
        stroke(r, g, b, 255);
    }
    
    /**
     * Sets the stroke color.
     * @param r The desired red component.
     * @param g The desired green component.
     * @param b The desired blue component.
     * @param a The desired alpha component.
     */
    public void stroke(int r, int g, int b, int a)
    {
        strokeColor.val[0] = b;
        strokeColor.val[1] = g;
        strokeColor.val[2] = r;
        strokeColor.val[3] = a;
    }
    
    /**
     * Sets the stroke weight,
     * @param s The desired stroke weight.
     */
    public void strokeWeight(int s)
    {
        stroke = s;
    }
    
    /**
     * Sets stroke to 0.
     */
    public void noStroke()
    {
        stroke = 0;
    }
//</editor-fold>
    
//<editor-fold defaultstate="collapsed" desc="Fill">

    /**
     * Sets the fill color.
     * @param g The color in gray scale.
     */
    public void fill(int g)
    {
        fill(g, g, g);
    }
    
    /**
     * Sets the fill color.
     * @param r The red component.
     * @param g The green component.
     * @param b the blue component.
     */
    public void fill(int r, int g, int b)
    {
        fill(r, g, b, 255);
    }
    
    /**
     * Sets the fill color.
     * @param c The desired color.
     */
    public void fill(Scalar c)
    {
        fill((int)c.val[2], (int)c.val[1], (int)c.val[0], (int)c.val[3]);
    }
    
    /**
     * Sets the fill color.
     * @param r The red component.
     * @param g The green component.
     * @param b The blue component.
     * @param a The alpha component.
     */
    public void fill(int r, int g, int b, int a)
    {
        fill = true;
        fillColor.val[0] = b;
        fillColor.val[1] = g;
        fillColor.val[2] = r;
        fillColor.val[3] = a;
    }
    
    /**
     * Sets the fill color to none.
     */
    public void noFill()
    {
        fill = false;
    }
//</editor-fold>
    
//<editor-fold defaultstate="collapsed" desc="Line">

    /**
     * Draws a line.
     * @param x1 The first point x.
     * @param y1 The first point y.
     * @param x2 The second point x.
     * @param y2 The second point y.
     */
    public void line(int x1, int y1, int x2, int y2)
    {
        Point p1 = new Point(x1, y1);
        Point p2 = new Point(x2, y2);
        line(p1, p2, stroke, strokeColor);
    }
    
    /**
     * Draws a line.
     * @param p1 The first point.
     * @param p2 The second point.
     */
    public void line(Point p1, Point p2)
    {
        line(p1, p2, stroke, strokeColor);
    }
    
    /**
     * Draws a line.
     * @param x1 The first point x.
     * @param y1 The first point y.
     * @param x2 The second point x.
     * @param y2 The second point y.
     * @param thickness The desired thickness.
     */
    public void line(int x1, int y1, int x2, int y2, int thickness)
    {
        Point p1 = new Point(x1, y1);
        Point p2 = new Point(x2, y2);
        line(p1, p2, thickness, strokeColor);
    }
    
    /**
     * Draws a line.
     * @param p1 The first point.
     * @param p2 The second point.
     * @param thickness The desired thickness.
     */
    public void line(Point p1, Point p2, int thickness)
    {
        line(p1, p2, thickness, strokeColor);
    }
    
    /**
     * Draws a line.
     * @param x1 The first point x.
     * @param y1 The first point y.
     * @param x2 The second point x.
     * @param y2 The second point y.
     * @param color The desired color.
     */
    public void line(int x1, int y1, int x2, int y2, Scalar color)
    {
        Point p1 = new Point(x1, y1);
        Point p2 = new Point(x2, y2);
        line(p1, p2, stroke, color);
    }
    
    /**
     * Draws a line.
     * @param p1 The first point.
     * @param p2 The second point.
     * @param color The desired color.
     */
    public void line(Point p1, Point p2, Scalar color)
    {
        line(p1, p2, stroke, color);
    }
    
    /**
     * Draws a line.
     * @param x1 The first point x.
     * @param y1 The first point y.
     * @param x2 The second point x.
     * @param y2 The second point y.
     * @param thickness The desired thickness.
     * @param color The desired color.
     */
    public void line(int x1, int y1, int x2, int y2, int thickness, Scalar color)
    {
        Point p1 = new Point(x1, y1);
        Point p2 = new Point(x2, y2);
        line(p1, p2, thickness, color);
    }
    
    /**
     * Draws a line.
     * @param p1 The first point.
     * @param p2 The second point.
     * @param thickness The desired thickness.
     * @param color The desired color.
     */
    public void line(Point p1, Point p2, int thickness, Scalar color)
    {
        if(stroke == 0)
            return;
        Core.line(mat, p1, p2, color, thickness, Core.LINE_AA, 0);
    }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Rect">

    /**
     * Selects the rectangle drawing mode to the specified mode.
     * @param mode The desired mode.
     */
    public void rectMode(int mode)
    {
        rectMode = mode;
    }
    
    /**
     * Draws a rectangle depending on the selected mode.
     * @param x If rectMode is set to CORNER or CORNERS, then x is the coordinate of the top left corner. If rectMode is set to CENTER, then x is the coordinate of the rectangle center.
     * @param y If rectMode is set to CORNER or CORNERS, then y is the coordinate of the top left corner. If rectMode is set to CENTER, then y is the coordinate of the rectangle center.
     * @param param1 If rectMode is set to CORNER or CENTER, then param1 is the width of the rectangle. If rectMode is set to CORNERS, then param1 is the x coordinate of the bottom right corner.
     * @param param2 If rectMode is set to CORNER or CENTER, then param2 is the height of the rectangle. If rectMode is set to CORNERS, then param2 is the y coordinate of the bottom right corner.
     */
    public void rect(int x, int y, int param1, int param2)
    {
        rect(x, y, param1, param2, rectMode, (int)fillColor.val[2], (int)fillColor.val[1], (int)fillColor.val[0], (int)fillColor.val[3]);
    }
    
    /**
     * Draws a rectangle. The selected color overrides the selected fillColor and does not set it.
     * @param x If rectMode is set to CORNER or CORNERS, then x is the coordinate of the top left corner. If rectMode is set to CENTER, then x is the coordinate of the rectangle center.
     * @param y If rectMode is set to CORNER or CORNERS, then y is the coordinate of the top left corner. If rectMode is set to CENTER, then y is the coordinate of the rectangle center.
     * @param param1 If rectMode is set to CORNER or CENTER, then param1 is the width of the rectangle. If rectMode is set to CORNERS, then param1 is the x coordinate of the bottom right corner.
     * @param param2 If rectMode is set to CORNER or CENTER, then param2 is the height of the rectangle. If rectMode is set to CORNERS, then param2 is the y coordinate of the bottom right corner.
     * @param mode The desired mode. This overrides the rectMode and does not set it.
     * @param r The red component.
     * @param g The green component.
     * @param b The blue component.
     * @param a The alpha component.
     */
    public void rect(int x, int y, int param1, int param2, int mode, int r, int g, int b, int a)
    {
        Scalar fillC = new Scalar(b, g, r, a);
        switch(mode)
        {
            case CORNER:
                drawRect(new Point(x, y), new Point(x+param1, y+param2), fillC, stroke, strokeColor);
            break;
            case CENTER:
                float aux1 = param1/(float)2;
                float aux2 = param2/(float)2;
                drawRect(new Point(x-aux1, y-aux2), new Point(x+aux1, y+aux2), fillC, stroke, strokeColor);
            break;
            case CORNERS:
                drawRect(new Point(x, y), new Point(param1, param2), fillC, stroke, strokeColor);
            break;
        }
    }
    
    private void drawRect(Point p1, Point p2, Scalar fillColor, int stroke, Scalar strokeColor)
    {
        if(fill)
            Core.rectangle(mat, p1, p2, fillColor, -1, Core.LINE_AA, 0);
        if(stroke > 0)
            Core.rectangle(mat, p1, p2, strokeColor, stroke, Core.LINE_AA, 0);
    }
//</editor-fold>
    
//<editor-fold defaultstate="collapsed" desc="Background">

    /**
     * Sets the background to the specified color.
     * @param s The desired color.
     */
    public void background(Scalar s)
    {
        mat.setTo(s);
    }
    
    /**
     * Sets the background to the specified color.
     * @param gray Tthe desired color in grayscale.
     */
    public void background(int gray)
    {
        mat.setTo(new Scalar(gray, gray, gray));
    }
    
    /**
     * Sets the background to the specified color.
     * @param r The desired red component.
     * @param g The desired green component.
     * @param b  The desired blue component.
     */
    public void background(int r, int g, int b)
    {
        mat.setTo(new Scalar(b, g, r));
    }
//</editor-fold>
    
//<editor-fold defaultstate="collapsed" desc="Load Image">

    /**
     * Loads an image file.
     * @param image The relative path to the image.
     * @return A Mat containing the loaded image.
     */
    public Mat loadImage(String image)
    {
        return Highgui.imread(image);
    }
    
    /**
     * Loads an image file.
     * @param image The relative path to the image.
     * @param flag The opencv flag to load tje image.
     * @return A Mat containing the loaded image.
     */
    public Mat loadImage(String image, int flag)
    {
        return Highgui.imread(image, flag);
    }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Save">

    /**
     * Saves the current canvas to a file.
     * @param name The name of the file to save.
     */
    public void save(String name)
    {
        Highgui.imwrite(name, mat);
    }
    
    /**
     * Saves an image to a file
     * @param name The name of the file to save.
     * @param mat The image to save.
     */
    public void save(String name, Mat mat)
    {
        Highgui.imwrite(name, mat);
    }
//</editor-fold>
    
//<editor-fold defaultstate="collapsed" desc="Draw Image">

    /**
     * Draws an image on the specified point of the canvas.
     * @param src The image to be drawn.
     * @param p The starting point of the canvas.
     */
    public void image(Mat src, Point p)
    {
        image(src, (int)p.x, (int)p.y);
    }
    
    /**
     * Draws an image on the specified point of the canvas.
     * @param src The image to be drawn.
     * @param x The starting x of the canvas.
     * @param y The starting y of the canvas.
     */
    public void image(Mat src, int x, int y)
    {
        if(x>mat.width() || y>mat.height())
            return;
        
        int bottom=0, right=0;
        int bottomE=0, rightE=0;
        
        Mat image = new Mat();
        Conversion.Mat2Drawable(src, image);
        
        if(image.width()+x<mat.width())
            right = mat.width()-(image.width()+x);
        else if(image.width()+x>mat.width())
            rightE = image.width()-(mat.width()-x);
        
        if(image.height()+y<mat.height())
            bottom = mat.height()-(image.height()+y);
        else if(image.height()+y>mat.height())
            bottomE = image.height()-(mat.height()-y);
        
        image.adjustROI(0, -bottomE, 0, -rightE);
        mat.adjustROI(-y, -bottom, -x, -right);
        if(image.elemSize()!=3)
            image.convertTo(image, CvType.CV_8UC3, 1, 0);
        if(image.channels()==1)
            Imgproc.cvtColor(image, mat, Imgproc.COLOR_GRAY2BGR);
        else if(image.channels()==4)
            Imgproc.cvtColor(image, mat, Imgproc.COLOR_BGRA2BGR);
        else
            image.copyTo(mat);
        
        mat.adjustROI(y, bottom, x, right);
        image.release();
    }
//</editor-fold>
    
    /**
     * Redraws the canvas updating it. Used and called automatically after draw function. Can be used to update canvas.
     */
    public final void redraw()
    {
        convertMat();
        area.setIcon(new ImageIcon(image));
    }
    
    private void convertMat()
    {
        Imgproc.cvtColor(mat, matAux, Imgproc.COLOR_BGR2RGB);
        matAux.get(0, 0, data);
        image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);
   }
    
    
    
//<editor-fold defaultstate="collapsed" desc="Extendable code">
    /**
     * Overridable method. Called once in the constructor.
     */
    public void setup(){};

    /**
     * Ooverridable method. Running in loop.
     */
    public void draw(){};

    /**
     * Overridable method. Called when canvas is closing.
     */
    public void closing(){};

    /**
     * Overridable method. Called when a key is typed.
     */
    public void keyTyped(){};

    /**
     * Overridable method. Called when a key is pressed.
     */
    public void keyPressed(){};

    /**
     * Overridable method. Called when a key is released.
     */
    public void keyReleased(){};

    /**
     * Overridable method. Called when a mouse button is pressed.
     */
    public void mousePressed(){};

    /**
     * Overridable method. Called when mouse is dragged.
     */
    public void mouseDragged(){};

    /**
     * Overridable method. Called when a mouse button is released.
     */
    public void mouseReleased(){};
//</editor-fold>
}
