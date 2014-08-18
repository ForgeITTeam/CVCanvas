package main;

import canvas.CVWindow;
import org.opencv.core.Mat;
import org.openni.OpenNI;
import sensor3D.Sensor3D;

public class Canvas
{
    static
    {
        System.loadLibrary("opencv_java247");
        System.loadLibrary("OpenNI2");
        OpenNI.initialize();
    }
    
    public static void main(String[] args)
    {
//        CVWindow.create("Depth");
//        CVWindow.create("IR");
//        
//        Sensor3D sensor = new Sensor3D(0){
//            
//            @Override
//            public void readDepth(Mat depthCam)
//            {
//                CVWindow.show("Depth", depthCam);
//            }
//            
//            @Override
//            public void readRGB(Mat rgbCam)
//            {
//                CVWindow.show("RGB", rgbCam);
//            }
//            
//            @Override
//            public void readIR(Mat irCam)
//            {
//                CVWindow.show("IR", irCam);
//            }
//        };
//        
//        sensor.depthCam.setSize(320, 240);
//        sensor.depthCam.setFrameRate(60);
//        
//        sensor.IRCam.setSize(320, 240);
//        sensor.IRCam.setFrameRate(60);
//        
//        sensor.start(Sensor3D.DEPTH);
//        sensor.start(Sensor3D.IR);
//        
//        CVWindow.setOnClose("Depth", ()->{sensor.close();CVWindow.destroyAll();});
//        CVWindow.setOnClose("IR", ()->{sensor.close();CVWindow.destroyAll();});
        
        new sample1();
        
//        Kinect2 k = new Kinect2();
//        CVWindow.create("Kinect RGB");
//        CVWindow.create("Kinect Depth");
//        while(CVWindow.show("Kinect RGB", k.rgb) | CVWindow.show("Kinect Depth", k.depth))
//            k.run();
//        k.close();
    }
}
