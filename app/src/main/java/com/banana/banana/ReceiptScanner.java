package com.banana.banana;

import android.util.Log;


import com.google.gdata.data.extensions.Im;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_MEAN_C;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.RETR_EXTERNAL;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY_INV;


/**
 * Created by abbyvansoest on 4/19/17.
 */

public class ReceiptScanner {


    public ReceiptScanner() {}

    /* takes in picture file path -- does canny edge detection on the picture */
    /* returns picture cropped to edges? */

    private Mat cannyEdgeDetector(Mat image) {

        Mat frame = image.clone();
        Mat grayImage = new Mat();
        Mat blurImage = new Mat();
        Mat detectedEdges = new Mat();
        List<MatOfPoint> contours = new ArrayList();


        Imgproc.cvtColor(frame, grayImage, Imgproc.COLOR_BGR2GRAY);

        int erosion_size = 6;
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS,new Size(2*erosion_size+1,2*erosion_size+1));

        Imgproc.blur(grayImage,blurImage,new Size(5,5),new Point(0,0),1);
        Imgproc.dilate(blurImage,blurImage,element);
        Imgproc.erode(blurImage,blurImage,element);
        Imgproc.Canny(blurImage,detectedEdges,15,90);

        return detectedEdges;

    }

    public void refine(File imgFile) {

        Mat image = Imgcodecs.imread(imgFile.getAbsolutePath());
        Mat canny = this.cannyEdgeDetector(image);
        List<MatOfPoint> contours = new ArrayList();
        Mat hierarchy = new Mat();
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image,grayImage, Imgproc.COLOR_BGR2GRAY);

        Imgproc.adaptiveThreshold(grayImage,grayImage,255,ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY,15,8);
        Photo.fastNlMeansDenoising(grayImage,grayImage);
        Imgcodecs.imwrite(imgFile.getAbsolutePath(),grayImage);

    }

}