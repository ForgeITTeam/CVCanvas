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

public abstract class Sensor3D
{
    private Device sensor;
    
    public RGBCam RGBCam;
    public DepthCam depthCam;
    public IRCam IRCam;
    
    public final static int RGB = 0;
    public final static int DEPTH = 1;
    public final static int IR = 2;
    
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
    
    public void start(int mode)
    {
        if(mode==RGB)
            RGBCam.start();
        else if(mode==DEPTH)
            depthCam.start();
        else if(mode==IR)
            IRCam.start();
    }
    
    public Mat getRGB()
    {
        return RGBCam.image;
    }
    
    public Mat getDepth()
    {
        return depthCam.image;
    }
    
    public Mat getIR()
    {
        return IRCam.image;
    }
    
    public abstract void readRGB(Mat RGBFrame);
    public abstract void readDepth(Mat depthFrame);
    public abstract void readIR(Mat IRFrame);
    
    public void close()
    {
        if(RGBCam.video != null)
        {
            RGBCam.video.removeNewFrameListener(RGBCam);
            RGBCam.video.stop();
            RGBCam.video.destroy();
            RGBCam.video=null;
        }
        if(depthCam.video != null)
        {
            depthCam.video.removeNewFrameListener(depthCam);
            depthCam.video.stop();
            depthCam.video.destroy();
            depthCam.video=null;
        }
        if(IRCam.video != null)
        {
            IRCam.video.removeNewFrameListener(IRCam);
            IRCam.video.stop();
            IRCam.video.destroy();
            IRCam.video=null;
        }
        sensor.close();
    }
    
    public final class RGBCam implements VideoStream.NewFrameListener
    {
        private Mat image;
        private byte[] buffer;
        private VideoStream video;
        
        private Size size;
        private int fps;
        
        public RGBCam()
        {
            setSize(640, 480);
            setFrameRate(30);
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
            video.start();
        }
        
        @Override
        public synchronized void onFrameReady(VideoStream stream)
        {
            VideoFrameRef lastFrame = stream.readFrame();
            
            lastFrame.getData().order(ByteOrder.LITTLE_ENDIAN).get(buffer);
            image.put(0, 0, buffer);
            Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2BGR);
            lastFrame.release();
            readRGB(image);
        }
    }
    
    public final class DepthCam implements VideoStream.NewFrameListener
    {
        private Mat image;
        private short[] buffer;
        private VideoStream video;
        
        private Size size;
        private int fps;

        public DepthCam()
        {
            setSize(640, 480);
            setFrameRate(30);
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
            video.start();
        }
        
        @Override
        public synchronized void onFrameReady(VideoStream stream)
        {
            VideoFrameRef ref = stream.readFrame();
            
            ref.getData().order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(buffer);
            image.put(0, 0, buffer);
            ref.release();
            readDepth(image);
        }
    }
    
    public final class IRCam implements VideoStream.NewFrameListener
    {
        public Mat image;
        private short[] buffer;
        private VideoStream video;
        
        private Size size;
        private int fps;

        public IRCam()
        {
            setSize(640, 480);
            setFrameRate(30);
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
            video.start();
        }
        
        @Override
        public synchronized void onFrameReady(VideoStream stream)
        {
            VideoFrameRef ref = stream.readFrame();
            
            ref.getData().order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(buffer);
            image.put(0, 0, buffer);
            image.convertTo(image, CvType.CV_16UC1, 255);
            ref.release();
            readIR(image);
        }
    }
}
