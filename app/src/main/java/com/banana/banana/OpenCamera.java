package com.banana.banana;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.banana.banana.OrderData.setFoodAndPrice;
import static com.banana.banana.TextParser.parse;

public class OpenCamera extends AppCompatActivity {

    // Intent stuff
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";

    // Picture button
    private Button takePictureButton;

    // Parsing stuff
    private String rawText = "";
    private String processedText = "";

    // Request stuff
    final int REQUEST_TAKE_PHOTO = 1;
    final int CROP_PIC = 2;

    // Camera stuff
    private SurfaceView preview=null;
    private SurfaceHolder previewHolder=null;
    private Camera camera=null;
    private boolean inPreview=false;
    private boolean cameraConfigured=false;
    private Camera.PictureCallback jpegCallback;

    // Permissions stuff
    private PackageManager pm;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;

    // Storage stuff
    public static File photoFile = null;
    public static File cropFile = null;
    private static String mCurrentPhotoPath;
    private Uri photoURI;

    // Tesseract OCR stuff
    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + "/SimpleAndroidOCR/";
    // You should have the trained data file in assets folder
    // You can get them at:
    // https://github.com/tesseract-ocr/tessdata
    public static final String lang = "eng";
    private static final String TAG = "SimpleAndroidOCR.java";
    private String recognizedText;

    public static List<List<String>> parseResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_camera);
        preview=(SurfaceView)findViewById(R.id.cameraView);

        // Hide the status bar.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        setThingsUp();
    }

    // Function for setting up the screen
    private void setThingsUp() {

        // Set up camera
        previewHolder=preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        if (!OpenCVLoader.initDebug()) {
            Log.e(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), not working.");
        } else {
            Log.d(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), working.");
        }

        String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };

        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
                    return;
                } else {
                    Log.v(TAG, "Created directory " + path + " on sdcard");
                }
            }

        }

        // Transfer the traindata file into the phone
        if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
            try {
                AssetManager assetManager = getAssets();
                InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
                OutputStream out = new FileOutputStream(DATA_PATH
                        + "tessdata/" + lang + ".traineddata");

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();

                Log.v(TAG, "Copied " + lang + " traineddata");
            } catch (IOException e) {
                Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
            }
        }

        //THIS IS THE PICTURE TAKING CODE//
        takePictureButton = (Button) findViewById(R.id.button_image);

        // Check for camera and storage permissions before starting (won't function without)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            takePictureButton.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA }, 0);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            takePictureButton.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }
        takePictureButton.setEnabled(true);

        // Callback for when picture is taken by camera
        jpegCallback = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {
                FileOutputStream outStream = null;
                try {
                    // Storage stuff
                    photoFile = createImageFile();
                    System.out.println(photoFile.exists() + " " + photoFile.toString());
                    outStream = new FileOutputStream(photoFile.getAbsoluteFile());
                    outStream.write(data);
                    outStream.close();
                    Log.v(TAG, "entering crop");
                    photoURI = FileProvider.getUriForFile(OpenCamera.this,
                            BuildConfig.APPLICATION_ID + ".provider",
                            photoFile);
                    // Enters cropping function
                    performCrop();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                }
            }
        };

        // Specifically for autofocus of camera
        preview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (camera != null) {
                    camera.cancelAutoFocus();

                    // Creates parameters for the camera to achieve autofocus
                    Camera.Parameters parameters = camera.getParameters();
                    if (parameters.getFocusMode() != Camera.Parameters.FOCUS_MODE_AUTO) {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    }
                    if (parameters.getMaxNumFocusAreas() > 0) {
                        parameters.setFocusAreas(null);
                    }
                    try {
                        camera.setParameters(parameters);
                        camera.startPreview();

                        // Start the autofocus with the above parameters
                        camera.autoFocus(new Camera.AutoFocusCallback() {
                            @Override
                            public void onAutoFocus(boolean success, Camera camera) {
                                // Hide the status bar.
                                View decorView = getWindow().getDecorView();
                                int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
                                decorView.setSystemUiVisibility(uiOptions);

                                // Give the camera a new FocusArea to analyse (null means it'll find the best area)
                                if (camera.getParameters().getFocusMode() != Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) {
                                    Camera.Parameters parameters = camera.getParameters();
                                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                                    if (parameters.getMaxNumFocusAreas() > 0) {
                                        parameters.setFocusAreas(null);
                                    }
                                    camera.setParameters(parameters);
                                    try {
                                        camera.startPreview();
                                    } catch (Exception e) {
                                        System.out.println(e.toString());
                                    }

                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        });
    }

    //take the picture
    public void captureImage(View v) throws IOException {
        takePictureButton.setEnabled(false);
        camera.takePicture(null, null, jpegCallback);
    }

    // Refresh camera screen to current camera view
    public void refreshCamera() {
        // Hide the status bar.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        if (previewHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }
        // stop preview before making changes
        try {
            camera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings
        try {
            camera.setPreviewDisplay(previewHolder);
            camera.startPreview();
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
            int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0: degrees = 0; break; //Natural orientation
                case Surface.ROTATION_90: degrees = 90; break; //Landscape left
                case Surface.ROTATION_180: degrees = 180; break;//Upside down
                case Surface.ROTATION_270: degrees = 270; break;//Landscape right
            }
            int rotate = (info.orientation - degrees + 360) % 360;

            // Set the 'rotation' parameter
            Camera.Parameters params = camera.getParameters();
            params.setRotation(rotate);
            camera.setParameters(params);
            setCameraDisplayOrientation(this, 0, camera);
            takePictureButton.setEnabled(true);

        } catch (Exception e) {

        }
    }

    //Preprocess image before giving to Tesseract
    private void refineImg(Uri fileUri) {
        Mat image = Imgcodecs.imread(fileUri.getPath());
        Mat gray = new Mat();
        Imgproc.cvtColor(image,gray,Imgproc.COLOR_BGR2GRAY);
        Mat dest = new Mat();
        Imgproc.cvtColor(image,gray,Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(gray, dest, new Size(0, 0), 3);
        Core.addWeighted(gray, 1.5, dest, -0.5, 0, gray);
        Imgproc.threshold(gray, gray, 0, 255, Imgproc.THRESH_BINARY+Imgproc.THRESH_OTSU);
        Imgcodecs.imwrite(fileUri.getPath(), gray);
    }

    //Get required permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                takePictureButton.setEnabled(true);
            }
        } else if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {
            int hasPerm = pm.checkPermission(
                    Manifest.permission.CAMERA,
                    this.getPackageName());
            if (hasPerm != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Requires Camera Permission", Toast.LENGTH_SHORT).show();
            } else {
                camera = Camera.open();
                startPreview();
                refreshCamera();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Uri imageUri = Uri.parse(mCurrentPhotoPath);
            performCrop();
        }

        // User is returning from cropping the image
        else if (requestCode == CROP_PIC) {
            // get the cropped bitmap
            Uri imageUri = Uri.parse(mCurrentPhotoPath);

            // If photo is discarded, return to home screen
            if (data == null) {
                photoFile.delete();
                return;
            }
            refineImg(imageUri);
            onPhotoTaken();
            Log.v(TAG, "THIS IS THE PROCESSED OUTPUT\n" + processedText);
            sendMessage(takePictureButton);
        }
    }

    //Crop the photo after picture is taken
    private void performCrop() {
        cropFile = null;
        try {
            cropFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
            Log.v(TAG, "COULD NOT CREATE FILE FOR CROP OUTPUT");
            return;
        }
        Uri croppedUri = FileProvider.getUriForFile(OpenCamera.this,
                BuildConfig.APPLICATION_ID + ".provider", cropFile);

        //Initiate crop intent
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // indicate image type and Uri
            cropIntent.setDataAndType(photoURI, "image/*");
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("output", croppedUri);
            cropIntent.putExtra("outputFormat", "PNG");

            //Grant crop intent the permissions necessary to access photo files
            List<ResolveInfo> resInfoList = OpenCamera.this.getPackageManager().queryIntentActivities(cropIntent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                OpenCamera.this.grantUriPermission(packageName, croppedUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(cropIntent, CROP_PIC);
        } catch (ActivityNotFoundException anfe) {
            // display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    //Create file to store image in
    private static File createImageFile() throws IOException{
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    //Run Tesseract on photo
    protected void onPhotoTaken() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        options.inScaled = false;
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath.replaceFirst("file:", ""), options);
        if (bitmap == null) {
            Log.v(TAG, "Bitmap is null");
            return;
        }

        //Setup and call Tesseract
        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        baseApi.init(DATA_PATH, lang);
        baseApi.setImage(bitmap);
        recognizedText = baseApi.getUTF8Text();
        baseApi.end();

        processedText = recognizedText;
        parseResult = parse(recognizedText);
        setFoodAndPrice();
    }

    /** Transfers us over to list of receipt items*/
    public void sendMessage(View view) {
        Intent intent = new Intent(this, MainReceipt.class);
        intent.putExtra(EXTRA_MESSAGE, recognizedText);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Request all important permissions for this screen before doing anything
        pm = this.getPackageManager();
        List<String> permRequests = new ArrayList<>();
        String[] permList;
        int pCount = 0;

        int hasPermCam = pm.checkPermission(
                Manifest.permission.CAMERA,
                this.getPackageName());
        if (hasPermCam != PackageManager.PERMISSION_GRANTED) {
            pCount++;
            permRequests.add(Manifest.permission.CAMERA);
        }
        int hasPermStore = pm.checkPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                this.getPackageName());
        if (hasPermStore != PackageManager.PERMISSION_GRANTED) {
            pCount++;
            permRequests.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (pCount > 0) {
            permList = new String[pCount];
            ActivityCompat.requestPermissions(this,
                    permRequests.toArray(permList),
                    MY_PERMISSIONS_REQUEST_CAMERA);
        }
        else {
            camera = Camera.open();
            startPreview();
            refreshCamera();
        }
    }

    @Override
    public void onPause() {

        // Request all important permissions for this screen before doing anything
        pm = this.getPackageManager();
        List<String> permRequests = new ArrayList<>();
        String[] permList;
        int pCount = 0;

        int hasPermCam = pm.checkPermission(
                Manifest.permission.CAMERA,
                this.getPackageName());
        if (hasPermCam != PackageManager.PERMISSION_GRANTED) {
            pCount++;
            permRequests.add(Manifest.permission.CAMERA);
        }
        int hasPermStore = pm.checkPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                this.getPackageName());
        if (hasPermStore != PackageManager.PERMISSION_GRANTED) {
            pCount++;
            permRequests.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (pCount > 0) {
            permList = new String[pCount];
            ActivityCompat.requestPermissions(this,
                    permRequests.toArray(permList),
                    MY_PERMISSIONS_REQUEST_CAMERA);
        }
        else if (camera != null){
            if (inPreview) {
                camera.stopPreview();
            }
            camera.release();
            camera = null;
            inPreview = false;
        }

        super.onPause();
    }

    // Initialize the preview for the camera to work
    private void initPreview() {
        if (camera!=null && previewHolder.getSurface()!=null) {
            try {
                camera.setPreviewDisplay(previewHolder);
            }
            catch (Throwable t) {
                Toast.makeText(OpenCamera.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }

            // Configure camera details if necessary
            if (!cameraConfigured) {
                Camera.Parameters parameters=camera.getParameters();
                List<Camera.Size> sizes=parameters.getSupportedPreviewSizes();
                Camera.Size size = sizes.get(0);

                if (size!=null) {
                    parameters.setPreviewSize(size.width, size.height);
                    camera.setParameters(parameters);
                    cameraConfigured=true;
                }
            }
        }
    }

    // Perform necessary steps to show the camera screen appropriately
    private void startPreview() {

        // camera configured if not so already
        if (cameraConfigured && camera!=null) {
            camera.startPreview();
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);

            // set up rotation details
            int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0: degrees = 0; break; //Natural orientation
                case Surface.ROTATION_90: degrees = 90; break; //Landscape left
                case Surface.ROTATION_180: degrees = 180; break;//Upside down
                case Surface.ROTATION_270: degrees = 270; break;//Landscape right
            }
            int rotate = (info.orientation - degrees + 360) % 360;
            Camera.Parameters params = camera.getParameters();
            params.setRotation(rotate);
            camera.setParameters(params);
            setCameraDisplayOrientation(this, 0, camera);
            inPreview=true;
        }
    }

    // Correctly orient the camera for the surfaceview
    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {

        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    SurfaceHolder.Callback surfaceCallback=new SurfaceHolder.Callback() {
        public void surfaceCreated(SurfaceHolder holder) {
            // no-op -- wait until surfaceChanged()
        }

        // detects when surface has changed
        public void surfaceChanged(SurfaceHolder holder,
                                   int format, int width,
                                   int height) {
            initPreview();
            startPreview();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // no-op
        }
    };

}