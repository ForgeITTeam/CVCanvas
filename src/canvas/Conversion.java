package canvas;

import java.awt.image.BufferedImage;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author Ricardo
 */
public class Conversion
{

    /**
     *
     * @param src
     * @param result
     */
    public static void Mat2BufferedImage(Mat src, BufferedImage result)
    {
        Mat aux = new Mat();
        byte[] data;
	switch(result.getType())
	{
            case BufferedImage.TYPE_3BYTE_BGR:
                    switch(src.channels())
                    {
                        case 4: Imgproc.cvtColor(src, aux, Imgproc.COLOR_BGRA2RGB); break;
                        case 1: Imgproc.cvtColor(src, aux, Imgproc.COLOR_GRAY2RGB); break;
                        default: Imgproc.cvtColor(src, aux, Imgproc.COLOR_BGR2RGB); break;
                    }
                    
                    if(aux.type() == CvType.CV_64FC3)
                            aux.convertTo(aux, CvType.CV_8UC3, 255.0);
                    else if(aux.type() == CvType.CV_32FC3)
                            aux.convertTo(aux, CvType.CV_8UC3, 255.0);
                    else if(aux.type() == CvType.CV_32SC3)
                            aux.convertTo(aux, CvType.CV_8UC3, 1.0/16777216.0, -128);
                    else if(aux.type() == CvType.CV_16SC3)
                            aux.convertTo(aux, CvType.CV_8UC3, 1.0/255.0, -128);
                    else if(aux.type() == CvType.CV_16UC3)
                            aux.convertTo(aux, CvType.CV_8UC3, 1.0/255.0);
                    else if(aux.type() == CvType.CV_8SC3)
                            aux.convertTo(aux, CvType.CV_8UC3, 1.0, -128);
                    
                    data = new byte[aux.cols() * aux.rows() * (int)aux.elemSize()];
                    aux.get(0, 0, data);
                    result.getRaster().setDataElements(0, 0, aux.cols(), aux.rows(), data);
            break;
            case BufferedImage.TYPE_4BYTE_ABGR:
                    switch(src.channels())
                    {
                        case 3: Imgproc.cvtColor(src, aux, Imgproc.COLOR_BGR2RGBA); break;
                        case 1: Imgproc.cvtColor(src, aux, Imgproc.COLOR_GRAY2RGBA); break;
                        default: Imgproc.cvtColor(src, aux, Imgproc.COLOR_BGRA2RGBA); break;
                    }
                    
                    if(aux.type() == CvType.CV_64FC4)
                            aux.convertTo(aux, CvType.CV_8UC4, 255.0);
                    else if(aux.type() == CvType.CV_32FC4)
                            aux.convertTo(aux, CvType.CV_8UC4, 255.0);
                    else if(aux.type() == CvType.CV_32SC4)
                            aux.convertTo(aux, CvType.CV_8UC4, 1.0/16777216.0, -128);
                    else if(aux.type() == CvType.CV_16SC4)
                            aux.convertTo(aux, CvType.CV_8UC4, 1.0/255.0, -128);
                    else if(aux.type() == CvType.CV_16UC4)
                            aux.convertTo(aux, CvType.CV_8UC4, 1.0/255.0);
                    else if(aux.type() == CvType.CV_8SC4)
                            aux.convertTo(aux, CvType.CV_8UC4, 1.0, -128);
                    
                    data = new byte[aux.cols() * aux.rows() * (int)aux.elemSize()];
                    aux.get(0, 0, data);
                    result.getRaster().setDataElements(0, 0, aux.cols(), aux.rows(), data);
            break;
            case BufferedImage.TYPE_BYTE_GRAY:
                    switch(src.channels())
                    {
                        case 4: Imgproc.cvtColor(src, aux, Imgproc.COLOR_BGRA2GRAY); break;
                        case 3: Imgproc.cvtColor(src, aux, Imgproc.COLOR_BGR2GRAY); break;
                    }
                    
                    if(aux.type() == CvType.CV_64FC1)
                            aux.convertTo(aux, CvType.CV_8UC1, 255.0);
                    else if(aux.type() == CvType.CV_32FC1)
                            aux.convertTo(aux, CvType.CV_8UC1, 255.0);
                    else if(aux.type() == CvType.CV_32SC1)
                            aux.convertTo(aux, CvType.CV_8UC1, 1.0/16777216.0, -128);
                    else if(aux.type() == CvType.CV_16SC1)
                            aux.convertTo(aux, CvType.CV_8UC1, 1.0/255.0, -128);
                    else if(aux.type() == CvType.CV_16UC1)
                            aux.convertTo(aux, CvType.CV_8UC1, 1.0/255.0);
                    else if(aux.type() == CvType.CV_8SC1)
                            aux.convertTo(aux, CvType.CV_8UC1, 1.0, -128);
                    
                    data = new byte[aux.cols() * aux.rows() * (int)aux.elemSize()];
                    aux.get(0, 0, data);
                    result.getRaster().setDataElements(0, 0, aux.cols(), aux.rows(), data);
            break;
	}
        aux.release();
    }
    
    /**
     *
     * @param src
     * @param result
     */
    public static void Mat2Drawable(Mat src, Mat result)
    {
        switch(src.channels())
        {
            case 4: Imgproc.cvtColor(src, result, Imgproc.COLOR_BGRA2BGR); break;
            case 1: Imgproc.cvtColor(src, result, Imgproc.COLOR_GRAY2BGR); break;
            default: src.copyTo(result); break;
        }

        if(result.type() == CvType.CV_64FC3)
                result.convertTo(result, CvType.CV_8UC3, 255.0);
        else if(result.type() == CvType.CV_32FC3)
                result.convertTo(result, CvType.CV_8UC3, 255.0);
        else if(result.type() == CvType.CV_32SC3)
                result.convertTo(result, CvType.CV_8UC3, 1.0/16777216.0, -128);
        else if(result.type() == CvType.CV_16SC3)
                result.convertTo(result, CvType.CV_8UC3, 1.0/255.0, -128);
        else if(result.type() == CvType.CV_16UC3)
                result.convertTo(result, CvType.CV_8UC3, 1.0/255.0);
        else if(result.type() == CvType.CV_8SC3)
                result.convertTo(result, CvType.CV_8UC3, 1.0, -128);
    }
}
