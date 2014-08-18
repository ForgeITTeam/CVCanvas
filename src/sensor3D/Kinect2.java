/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sensor3D;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;

/**
 *
 * @author Alves
 */
public class Kinect2
{
    static
    {
        System.loadLibrary("Kinect2");
    }
    
    public static final int rgbWidth = 1920;
    public static final int rgbHeight = 1080;
    
    public static final int depthWidth = 512;
    public static final int depthHeight = 424;
    
    
    public Mat rgb = new Mat(new Size(rgbWidth, rgbHeight), CvType.CV_8UC4);
    public Mat depth = new Mat(new Size(depthWidth, depthHeight), CvType.CV_16UC1);
    
    public byte frameRGB[] = new byte[rgbWidth * rgbHeight * 4];
    private short frameDepth[] = new short[depthWidth * depthHeight];
    
    public Kinect2()
    {
        init();
    }
    
    public void run()
    {
        runKinect();
        rgb.put(0, 0, frameRGB);
        depth.put(0, 0, frameDepth);
    }
    
    private native void runKinect();
    private native void init();
    public native void close();
}
