package com.banana.banana;

import android.net.Uri;
import android.util.Log;

import com.google.gdata.data.docs.MaxUploadSize;
import com.google.gdata.data.extensions.Im;
import com.googlecode.leptonica.android.Scale;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.Features2d;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;


import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static java.sql.Types.NULL;
import static org.opencv.core.CvType.CV_8UC3;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.COLOR_BayerBG2BGR;
import static org.opencv.imgproc.Imgproc.HoughLinesP;
import static org.opencv.imgproc.Imgproc.RETR_EXTERNAL;
import static org.opencv.imgproc.Imgproc.RETR_LIST;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY_INV;
import static org.opencv.imgproc.Imgproc.blur;
import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.drawContours;
import static org.opencv.imgproc.Imgproc.findContours;

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

        Imgproc.blur(grayImage,blurImage,new Size(5,5),new Point(0,0),Imgproc.BORDER_DEFAULT);
        Imgproc.dilate(blurImage,blurImage,element);
        Imgproc.erode(blurImage,blurImage,element);
        Imgproc.Canny(blurImage,detectedEdges,20,100);

        return detectedEdges;

    }

    private Mat getHoughPTransform(Mat image, double rho, double theta, int threshold) {

        Mat result = image.clone();
        Mat lines = new Mat();

        Imgproc.HoughLinesP(image, lines, rho, theta, threshold);

        for (int i = 0; i < lines.cols(); i++) {
            double[] val = lines.get(0, i);
            Core.line(result, new Point(val[0], val[1]), new Point(val[2], val[3]), new Scalar(0, 0, 255), 20);

        }
        return result;
    }

    public void refine(File imgFile) {

        Mat image = Highgui.imread(imgFile.getAbsolutePath());
        Mat canny = this.cannyEdgeDetector(image);
        //Mat hough = getHoughPTransform(canny, 1,.017,2);

        List<MatOfPoint> contours = new ArrayList();
        Mat hierarchy = new Mat();
        Imgproc.findContours(canny,contours, hierarchy,RETR_EXTERNAL,CHAIN_APPROX_SIMPLE);

        System.out.println(contours.size());

        Mat contoursFrame = image.clone();
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        for (int i=0; i<contours.size(); i++) {
            //Convert contours(i) from MatOfPoint to MatOfPoint2f
            MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(i).toArray());
            //Processing on mMOP2f1 which is in type MatOfPoint2f
            double approxDistance = Imgproc.arcLength(contour2f, true) * 0.02;
            Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);

            //Convert back to MatOfPoint
            MatOfPoint points = new MatOfPoint(approxCurve.toArray());

            // Get bounding rect of contour
            Rect rect = Imgproc.boundingRect(points);

            // draw enclosing rectangle (all same color, but you could use variable i to make them unique)
            Core.rectangle(contoursFrame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255, 0, 0, 255), 3);
        }

        Highgui.imwrite(imgFile.getAbsolutePath(),contoursFrame);

    }
}