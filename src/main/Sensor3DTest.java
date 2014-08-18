/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package main;

import canvas.CVCanvas;
import org.opencv.core.Mat;
import sensor3D.Sensor3D;

/**
 *
 * @author Alves
 */
public class Sensor3DTest extends CVCanvas
{
    Sensor3D sensor;
    
    @Override
    public void setup()
    {
        sensor = new Sensor3D(0){
            @Override
            public void readDepth(Mat depth)
            {
                image(depth, 0, 0);
            }
            
            @Override
            public void readIR(Mat ir)
            {
                image(ir, 640, 0);
                redraw();
            }

            @Override
            public void readRGB(Mat RGBFrame) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        
        sensor.IRCam.setSize(640, 480);
        sensor.IRCam.setFrameRate(30);
        
        sensor.depthCam.setSize(640, 480);
        sensor.depthCam.setFrameRate(30);
        
        sensor.IRCam.start();
        sensor.depthCam.start();
        
        size(640*2, 480);
        background(255, 0, 0);
        noLoop();
    }

    @Override
    public void draw()
    {
    }
    
    @Override
    public void closing()
    {
        sensor.close();
    }
}
