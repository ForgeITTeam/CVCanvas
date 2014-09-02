package sensor3D;

import java.nio.ByteOrder;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.openni.Device;
import org.openni.OpenNI;
import org.openni.PixelFormat;
import org.openni.SensorType;
import org.openni.VideoFrameRef;
import org.openni.VideoMode;
import org.openni.VideoStream;

public class Sensor3D
{
    private final Device sensor;
    
    private RGBCam RGBCam;
    private DepthCam depthCam;
    private IRCam IRCam;
    
    public final static int RGB = 1;
    public final static int DEPTH = 2;
    public final static int IR = 4;
    
    public Sensor3D(int i)
    {
        sensor=Device.open(OpenNI.enumerateDevices().get(i).getUri());
        RGBCam = new RGBCam();
        depthCam = new DepthCam();
        IRCam = new IRCam();
    }
    
    public Sensor3D(Device sensor)
    {
        this.sensor=sensor;
        RGBCam = new RGBCam();
        depthCam = new DepthCam();
        IRCam = new IRCam();
    }
    
    
    
    public void start(int modes)
    {
        if((modes&RGB) == RGB)
            RGBCam.start();
        
        if((modes&DEPTH) == DEPTH)
            depthCam.start();
        
        if((modes&IR) == IR)
            IRCam.start();
    }
    
    public void setSize(int modes, int width, int height)
    {
        if((modes&RGB) == RGB)
            RGBCam.setSize(width, height);
        
        if((modes&DEPTH) == DEPTH)
            depthCam.setSize(width, height);
        
        if((modes&IR) == IR)
            IRCam.setSize(width, height);
    }
    
    public Size getSize(int mode)
    {
        if((mode&RGB) == RGB)
            return RGBCam.getSize();
        
        if((mode&DEPTH) == DEPTH)
            return depthCam.getSize();
        
        if((mode&IR) == IR)
            return IRCam.getSize();
        return null;
    }
    
    public void setFrameRate(int modes, int fps)
    {
        if((modes&RGB) == RGB)
            RGBCam.setFrameRate(fps);
        
        if((modes&DEPTH) == DEPTH)
            depthCam.setFrameRate(fps);
        
        if((modes&IR) == IR)
            IRCam.setFrameRate(fps);
    }
    
    public int getFrameRate(int mode)
    {
        if((mode&RGB) == RGB)
            return RGBCam.getFrameRate();
        
        if((mode&DEPTH) == DEPTH)
            return depthCam.getFrameRate();
        
        if((mode&IR) == IR)
            return IRCam.getFrameRate();
        return -1;
    }
    
    public void enableEvent(int modes, boolean evt)
    {
        if((modes&RGB) == RGB)
            RGBCam.enableEvent(evt);
        
        if((modes&DEPTH) == DEPTH)
            depthCam.enableEvent(evt);
        
        if((modes&IR) == IR)
            IRCam.enableEvent(evt);
    }
    
    
    public Mat getRGB()
    {
        RGBCam.readFrame(RGBCam.video.readFrame());
        return RGBCam.image;
    }
    
    public Mat getDepth()
    {
        depthCam.readFrame(depthCam.video.readFrame());
        return depthCam.image;
    }
    
    public Mat getIR()
    {
        IRCam.readFrame(IRCam.video.readFrame());
        return IRCam.image;
    }
    
    
    public void readRGB(Mat RGBFrame){}
    public void readDepth(Mat depthFrame){}
    public void readIR(Mat IRFrame){}
    
    
    public void close()
    {
        if(RGBCam.video != null)
        {
            if(RGBCam.event)
                RGBCam.video.removeNewFrameListener(RGBCam);
            RGBCam.video.stop();
            RGBCam.video.destroy();
            RGBCam.video=null;
        }
        if(depthCam.video != null)
        {
            if(depthCam.event)
                depthCam.video.removeNewFrameListener(depthCam);
            depthCam.video.stop();
            depthCam.video.destroy();
            depthCam.video=null;
        }
        if(IRCam.video != null)
        {
            if(IRCam.event)
                IRCam.video.removeNewFrameListener(IRCam);
            IRCam.video.stop();
            IRCam.video.destroy();
            IRCam.video=null;
        }
        sensor.close();
    }
    
    private final class RGBCam implements VideoStream.NewFrameListener
    {
        private Mat image;
        private byte[] buffer;
        private VideoStream video;
        
        private Size size;
        private int fps;
        private boolean event;
        
        public RGBCam()
        {
            setSize(640, 480);
            setFrameRate(30);
            event = true;
        }
        
        public void enableEvent(boolean evt)
        {
            event = evt;
        }
        
        public Size getSize()
        {
            return size;
        }
        
        public void setSize(int width, int height)
        {
            size = new Size(width, height);
            buffer = new byte[(int)size.width * (int)size.height * 3];
            image = new Mat(size, CvType.CV_8UC3);
        }
        
        public int getFrameRate()
        {
            return fps;
        }
        
        public void setFrameRate(int fps)
        {
            this.fps = fps;
        }
        
        public void start()
        {
            video = VideoStream.create(sensor, SensorType.COLOR);
            
            VideoMode modeVideo = new VideoMode();
            modeVideo.setResolution((int)size.width, (int)size.height);
            modeVideo.setFps(fps);
            modeVideo.setPixelFormat(PixelFormat.RGB888);
            
            video.setVideoMode(modeVideo);
            video.addNewFrameListener(this);
            if(!event)
                video.removeNewFrameListener(this);
            
            video.start();
        }
        
        @Override
        public synchronized void onFrameReady(VideoStream stream)
        {
            readFrame(stream.readFrame());
            readRGB(image);
        }
        
        private void readFrame(VideoFrameRef lastFrame)
        {
            lastFrame.getData().order(ByteOrder.LITTLE_ENDIAN).get(buffer);
            image.put(0, 0, buffer);
            Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2BGR);
            lastFrame.release();
        }
    }
    
    private final class DepthCam implements VideoStream.NewFrameListener
    {
        private Mat image;
        private short[] buffer;
        private VideoStream video;
        
        private Size size;
        private int fps;
        private boolean event;


        public DepthCam()
        {
            setSize(640, 480);
            setFrameRate(30);
            event = true;
        }
        
        public void enableEvent(boolean evt)
        {
            event = evt;
        }
        
        public Size getSize()
        {
            return size;
        }
        
        public void setSize(int width, int height)
        {
            size = new Size(width, height);
            buffer = new short[(int)size.width * (int)size.height];
            image = new Mat(size, CvType.CV_16UC1);
        }
        
        public int getFrameRate()
        {
            return fps;
        }
        
        public void setFrameRate(int fps)
        {
            this.fps = fps;
        }
        
        public void start()
        {
            video = VideoStream.create(sensor, SensorType.DEPTH);
            
            VideoMode modeDepth = new VideoMode();
            modeDepth.setResolution((int)size.width, (int)size.height);
            modeDepth.setFps(fps);
            modeDepth.setPixelFormat(PixelFormat.DEPTH_100_UM);
            
            video.setVideoMode(modeDepth);
            video.addNewFrameListener(this);
            if(!event)
                video.removeNewFrameListener(this);
            video.start();
        }
        
        @Override
        public synchronized void onFrameReady(VideoStream stream)
        {
            readFrame(stream.readFrame());
            readDepth(image);
        }
        
        private void readFrame(VideoFrameRef lastFrame)
        {
            lastFrame.getData().order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(buffer);
            image.put(0, 0, buffer);
            lastFrame.release();
        }
    }
    
    private final class IRCam implements VideoStream.NewFrameListener
    {
        public Mat image;
        private short[] buffer;
        private VideoStream video;
        
        private Size size;
        private int fps;
        private boolean event;


        public IRCam()
        {
            setSize(640, 480);
            setFrameRate(30);
            event = true;
        }
        
        public void enableEvent(boolean evt)
        {
            event = evt;
        }
        
        public Size getSize()
        {
            return size;
        }
        
        public void setSize(int width, int height)
        {
            size = new Size(width, height);
            buffer = new short[(int)size.width * (int)size.height];
            image = new Mat(size, CvType.CV_16UC1);
        }
        
        public int getFrameRate()
        {
            return fps;
        }
        
        public void setFrameRate(int fps)
        {
            this.fps = fps;
        }
        
        public void start()
        {
            video = VideoStream.create(sensor, SensorType.IR);
            
            VideoMode modeIR = new VideoMode();
            modeIR.setResolution((int)size.width, (int)size.height);
            modeIR.setFps(fps);
            modeIR.setPixelFormat(PixelFormat.GRAY16);
            
            video.setVideoMode(modeIR);
            video.addNewFrameListener(this);
            if(!event)
                video.removeNewFrameListener(this);
            
            video.start();
        }
        
        @Override
        public synchronized void onFrameReady(VideoStream stream)
        {
            readFrame(stream.readFrame());
            readIR(image);
        }
        
        private void readFrame(VideoFrameRef lastFrame)
        {
            lastFrame.getData().order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(buffer);
            image.put(0, 0, buffer);
            image.convertTo(image, CvType.CV_16UC1, 255);
            lastFrame.release();
        }
    }
}
