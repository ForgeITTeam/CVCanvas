package main;

import canvas.CVCanvas;

public class sample1 extends CVCanvas
{
    int x, y;
    
    @Override
    public void setup()
    {
        size(640, 480);
        
        fill(255, 0, 0);
        stroke(255);
        rectMode(CENTER);
        frameRate(60);
    }
    
    @Override
    public void draw()
    {
        background(0);
        x++;
        if(x == getWidth())
        {
            x = 0;
            y++;
        }
        if(y == getHeight())
            y = 0;
        rect(x, y, 10, 10);
    }
}
