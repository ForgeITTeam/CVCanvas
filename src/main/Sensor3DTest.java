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
        sensor = new Sensor3D(0);
        
        sensor.setSize(Sensor3D.DEPTH | Sensor3D.IR, 640, 480);
        sensor.setFrameRate(Sensor3D.DEPTH | Sensor3D.IR, 30);
        
        sensor.enableEvent(Sensor3D.DEPTH | Sensor3D.IR, false);
        sensor.start(Sensor3D.DEPTH | Sensor3D.IR);
        
        size(640*2, 480);
        background(255, 0, 0);
    }

    @Override
    public void draw()
    {
        image(sensor.getDepth(), 0, 0);
        image(sensor.getIR(), 640, 0);
    }
    
    @Override
    public void closing()
    {
        sensor.close();
    }
}
