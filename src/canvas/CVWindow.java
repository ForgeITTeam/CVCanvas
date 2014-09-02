package canvas;

import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.opencv.core.Mat;

/**
 *
 * @author Ricardo
 */
public class CVWindow extends JFrame
{   
    private static final ArrayList<CVWindow> janelas = new ArrayList();
    
    /**
     * Creates a new window to display an OpenCV Mat.
     * @param name The name of the window.
     */
    public static void create(String name)
    {
        if(name == null)
            return;
        for(CVWindow janela : janelas)
            if(janela.getTitle().equals(name))
                return;
        new CVWindow(name);
    }
    
    /**
     * Sets a function that runs on close.
     * @param name Name of the windon.
     * @param r Function that runs on close.
     */
    public static void setOnClose(String name, Runnable r)
    {
        if(name == null)
            return;

        for(CVWindow janela : janelas)
            if(janela.getTitle().equals(name))
            {
                janela.closing = r;
            }
    }
    
    /**
     * Destroy the window with the chosen name.
     * @param name Name of the window to be destroyed.
     */
    public static void destroy(String name)
    {
        if(name == null)
            return;
        for(CVWindow janela : janelas)
            if(janela.getTitle().equals(name))
            {
                janelas.remove(janela);
                janela.dispose();
            }
    }
    
    /**
     * Destroy all windows.
     */
    public static void destroyAll()
    {
        try
        {
            while(janelas.size() > 0)
            {
                CVWindow janela = janelas.get(0);
                janelas.remove(janela);
                janela.dispose();
            }
        }
        catch(java.util.ConcurrentModificationException e)
        {
            
        }
    }
    
    /**
     * Update the window with a mat.
     * @param name The name of the window.
     * @param mat The OpenCV Mat to be displayed.
     * @return
     */
    public static boolean show(String name, Mat mat)
    {
        if(name == null)
            return false;
        if(mat == null)
            return false;
        for(CVWindow janela : janelas)
            if(janela.getTitle().equals(name))
            {
                janela.size((int)mat.size().width, (int)mat.size().height);
                janela.redraw(mat);
                return true;
            }
        return false;
    }
    
    
    
    private Runnable closing;
    private final JLabel area;
    private BufferedImage image;
    
    
    private CVWindow(final String name)
    {
        super();
        area = new JLabel();
        
        setTitle(name);
        getContentPane().setLayout(null);
        getContentPane().add(area);
        pack();
        
        addNotify();
        setVisible(true);
        final CVWindow j = this;
        addKeyListener(new KeyAdapter() {
            @Override
            public synchronized void keyTyped(KeyEvent e)
            {
                if(e.getKeyChar()==KeyEvent.VK_ESCAPE)
                    dispatchEvent(new WindowEvent(j, WindowEvent.WINDOW_CLOSING));
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public synchronized void windowClosing(WindowEvent e)
            {
                if(closing != null)
                    closing.run();
                destroy(name);
            }
        });
        janelas.add(this);
    }
    
    private void size(int width, int height)
    {
        if(this.getWidth() == width && this.getHeight() == height)
            return;
        setMinimumSize(new Dimension(width, height));
        setSize(width, height);
        area.setBounds(0, 0, width, height);
        setLocationRelativeTo(null);

        image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
    }
    
    private void redraw(Mat mat)
    {
        Conversion.Mat2BufferedImage(mat, image);
        area.setIcon(new ImageIcon(image));
    }
}
