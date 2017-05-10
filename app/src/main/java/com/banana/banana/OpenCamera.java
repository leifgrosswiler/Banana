package com.banana.banana;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.Pixa;
import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.*;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.banana.banana.OrderData.setFoodAndPrice;
import static com.banana.banana.TextParser.parse;

import static com.banana.banana.SelectItems.everything;
import static java.security.AccessController.getContext;

public class OpenCamera extends AppCompatActivity {

    public static final String PACKAGE_NAME = "com.datumdroid.android.ocr.simple";
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";

    private Button takePictureButton;

    private String rawText = "";
    private String processedText = "";
    final int REQUEST_TAKE_PHOTO = 1;
    final int CROP_PIC = 2;
    private static String mCurrentPhotoPath;
    private Uri photoURI;

    private SurfaceView preview=null;
    private SurfaceHolder previewHolder=null;
    private Camera camera=null;
    private boolean inPreview=false;
    private boolean cameraConfigured=false;
    private Camera.PictureCallback jpegCallback;
    private PackageManager pm;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;


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

        // Get proper permissions
//        pm = this.getPackageManager();
//        int hasPerm = pm.checkPermission(
//                Manifest.permission.CAMERA,
//                this.getPackageName());
//        if (hasPerm != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.CAMERA},
//                    MY_PERMISSIONS_REQUEST_CAMERA);
//        } else {
//            setThingsUp();
//        }
        setThingsUp();

    }

    private void setThingsUp() {

        previewHolder=preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        if (everything == null)
            getContactNames();

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

        // lang.traineddata file with the app (in assets folder)
        if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
            try {
                AssetManager assetManager = getAssets();
                InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
                //GZIPInputStream gin = new GZIPInputStream(in);
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

        Log.v(TAG, "HERE");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            takePictureButton.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA }, 0);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            takePictureButton.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }
        takePictureButton.setEnabled(true);

        jpegCallback = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {
                FileOutputStream outStream = null;
                try {
                    File photoFile = createImageFile();
                    System.out.println(photoFile.exists() + " " + photoFile.toString());
                    outStream = new FileOutputStream(photoFile.getAbsoluteFile());
                    outStream.write(data);
                    outStream.close();
                    Log.v(TAG, "entering crop");
                    photoURI = FileProvider.getUriForFile(OpenCamera.this,
                            BuildConfig.APPLICATION_ID + ".provider",
                            photoFile);
                    performCrop(photoFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                }
                Toast.makeText(getApplicationContext(), "Picture Saved", Toast.LENGTH_SHORT).show();
            }
        };

        preview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (camera != null) {
                    camera.cancelAutoFocus();
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
                        camera.autoFocus(new Camera.AutoFocusCallback() {
                            @Override
                            public void onAutoFocus(boolean success, Camera camera) {
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

    public void captureImage(View v) throws IOException {
        //take the picture
        takePictureButton.setEnabled(false);
        camera.takePicture(null, null, jpegCallback);
    }

    public void refreshCamera() {
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

//STEP #2: Set the 'rotation' parameter
            Camera.Parameters params = camera.getParameters();
            params.setRotation(rotate);
            camera.setParameters(params);
            setCameraDisplayOrientation(this, 0, camera);
            takePictureButton.setEnabled(true);

        } catch (Exception e) {

        }
    }

    private void refineImg(Uri fileUri) {
        System.out.println("Refining image");
        Mat image = Imgcodecs.imread(fileUri.getPath());
        Mat gray = new Mat();
        Imgproc.cvtColor(image,gray,Imgproc.COLOR_BGR2GRAY);
//        Imgproc.GaussianBlur(gray, gray, new Size(3, 3), 0);
//        Imgproc.adaptiveThreshold(gray, gray,255,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc.THRESH_BINARY,15,8);
        Mat dest = new Mat();
        Imgproc.cvtColor(image,gray,Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(gray, dest, new Size(0, 0), 3);
        Core.addWeighted(gray, 1.5, dest, -0.5, 0, gray);
        Imgproc.threshold(gray, gray, 0, 255, Imgproc.THRESH_BINARY+Imgproc.THRESH_OTSU);
        Imgcodecs.imwrite(fileUri.getPath(), gray);
    }

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
                setContentView(R.layout.activity_open_camera);
                setThingsUp();
            }
        }
    }

    public void takePicture(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.v(TAG, "Could not create first file for camera");
                return;
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(OpenCamera.this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore
                        .EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.v(TAG, "in result handler");
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Uri imageUri = Uri.parse(mCurrentPhotoPath);
            File mFile = new File(imageUri.getPath());
            Log.v(TAG, "entering crop");
            System.out.println("Hi " + mFile.getAbsolutePath());
            performCrop(mFile);

        }
        // user is returning from cropping the image
        else if (requestCode == CROP_PIC) {
            // get the cropped bitmap
            System.out.println("processing crop result");
            onPhotoTaken(false);
            Uri imageUri = Uri.parse(mCurrentPhotoPath);
            File mFile = new File(imageUri.getPath());

            // If photo is discarded, return to home screen
            if (data == null) return;
//            ImageView croppedView = (ImageView) findViewById(R.id.imageview);
//            croppedView.setImageURI(imageUri);

            refineImg(imageUri);
            onPhotoTaken(true);
            Log.v(TAG, "THIS IS THE RAW OUTPUT\n" + rawText);
            Log.v(TAG, "THIS IS THE PROCESSED OUTPUT\n" + processedText);
            sendMessage(takePictureButton);
        }
    }

    private void performCrop(File imageFile) {
        Log.v(TAG, "at top of crop");

        File cropFile = null;
        try {
            cropFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
            return;
        }

        Uri croppedUri = FileProvider.getUriForFile(OpenCamera.this,
                BuildConfig.APPLICATION_ID + ".provider", cropFile);

        Log.v(TAG, mCurrentPhotoPath);
        Log.v(TAG, imageFile.getPath());
//        mCurrentPhotoPath = "file:" + imageFile.getAbsolutePath();

//        ImageView croppedView = (ImageView) findViewById(R.id.imageview);
//        croppedView.setImageURI(photoURI);

        try {
            Log.v(TAG, "in crop");

            try {
                Intent cropIntent = new Intent("com.android.camera.action.CROP");
                // indicate image type and Uri
                cropIntent.setDataAndType(photoURI, "image/*");
                cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                cropIntent.putExtra("crop", "true");
                cropIntent.putExtra("output", croppedUri);
                cropIntent.putExtra("outputFormat", "PNG");

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
                System.out.println("no crop app");
                String errorMessage = "Whoops - your device doesn't support the crop action!";
                Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            Toast toast = Toast
                    .makeText(this, "This device doesn't support the crop action!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private static File createImageFile() throws IOException{
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        if (storageDir.exists())
            System.out.println("the file/directory exists!");
        else {
            boolean result = storageDir.mkdir();
            System.out.println(result);

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

    protected void onPhotoTaken(boolean test) {
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
        try {
            ExifInterface exif = new ExifInterface(mCurrentPhotoPath.replaceFirst("file:", ""));
            int exifOrientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            int rotate = 90;

            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 0;
                    break;
            }

            if (rotate != 90) {

                // Getting width & height of the given image.
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();

                // Setting pre rotate
                Matrix mtx = new Matrix();
                mtx.preRotate(rotate);

                // Rotating Bitmap
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
            }

        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        //_image.setImageBitmap( bitmap );
        //ImageView imageView = (ImageView) findViewById(R.id.imageview);
        //imageView.setImageBitmap(bitmap);

        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        baseApi.init(DATA_PATH, lang);
        baseApi.setImage(bitmap);
        recognizedText = baseApi.getUTF8Text();
        //System.out.println("RECOGNIZED TEXT:\n\n\n"+recognizedText + "\n\n\n");

        baseApi.end();

        //Log.v(TAG, "Input in OpenCamera:");
        if (test) {
            processedText = recognizedText;
        } else {
            rawText = recognizedText;
        }
        //Log.v(TAG, parse(recognizedText).toString());
        if (test) {
            parseResult = parse(recognizedText);
            setFoodAndPrice();
        }
    }

    /** Called when the user taps the Send button */
    public void sendMessage(View view) {

        Intent intent = new Intent(this, MainReceipt.class);
        //EditText editText = (EditText) findViewById(R.id.editText);
        //String message = editText.getText().toString();

        //Delete pics
        //new File(photoFile.getPath()).delete();
        //new File(cropFile.getPath()).delete();
        //getContentResolver().delete(file, null, null);
        intent.putExtra(EXTRA_MESSAGE, recognizedText);

        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        camera=Camera.open();
        startPreview();
        refreshCamera();
    }

    @Override
    public void onPause() {
        if (inPreview) {
            camera.stopPreview();
        }

        camera.release();
        camera=null;
        inPreview=false;

        super.onPause();
    }

    private Camera.Size getBestPreviewSize(int width, int height,
                                           Camera.Parameters parameters) {
        Camera.Size result=null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width<=width && size.height<=height) {
                if (result==null) {
                    result=size;
                }
                else {
                    int resultArea=result.width*result.height;
                    int newArea=size.width*size.height;

                    if (newArea>resultArea) {
                        result=size;
                    }
                }
            }
        }

        return(result);
    }

    private void initPreview(int width, int height) {
        if (camera!=null && previewHolder.getSurface()!=null) {
            try {
                camera.setPreviewDisplay(previewHolder);
            }
            catch (Throwable t) {
                Log.e("CD-surfaceCallback",
                        "Exception in setPreviewDisplay()", t);
                Toast
                        .makeText(OpenCamera.this, t.getMessage(), Toast.LENGTH_LONG)
                        .show();
            }

            if (!cameraConfigured) {
                Camera.Parameters parameters=camera.getParameters();
//                Camera.Size size=getBestPreviewSize(width, height,
//                        parameters);
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

    private void startPreview() {
        if (cameraConfigured && camera!=null) {
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

//STEP #2: Set the 'rotation' parameter
            Camera.Parameters params = camera.getParameters();
            params.setRotation(rotate);
            camera.setParameters(params);
            setCameraDisplayOrientation(this, 0, camera);
            inPreview=true;
        }
    }

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

        public void surfaceChanged(SurfaceHolder holder,
                                   int format, int width,
                                   int height) {
            initPreview(width, height);
            startPreview();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // no-op
        }
    };

//    Camera.AutoFocusCallback autoFocus=new Camera.AutoFocusCallback() {
//        Camera.ShutterCallback shutterCallback =new Camera.ShutterCallback() {
//
//            @Override
//            public void onShutter() {
//                AudioManager mgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//                mgr.playSoundEffect(AudioManager.FLAG_PLAY_SOUND);
//
//            }
//        };
//        Camera.PictureCallback photoCallback = new Camera.PictureCallback() {
//            @Override
//            public void onPictureTaken(final byte[] data, final Camera camera) {
//                new Thread() {
//                    @Override
//                    public void run() {
//                        try {
//                            Thread.sleep(1000);
//                        } catch (Exception ex) {}
//                    }
//                }.start();
//            }
//        };
//
//        @Override
//        public void onAutoFocus(boolean success, Camera camera) {
//            camera.takePicture(shutterCallback,null, null, photoCallback);
//        }
//    };

    private void getContactNames() {

        List<CoolList> all = new ArrayList<>();
        Map<String, ArrayList<String>> tempAll = new HashMap<>();

        String[] EMAIL_PROJECTION = new String[] {
                ContactsContract.CommonDataKinds.Email.CONTACT_ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Email.DATA
        };

        String[] PHONE_PROJECTION = new String[] {
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.DATA
        };


        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, EMAIL_PROJECTION, null, null, null);
        Cursor phoneCursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PHONE_PROJECTION, null, null, null);

        if (cursor != null && phoneCursor != null) {
            try {
                final int displayNameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                final int emailIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
                final int displayNameIndexNum = phoneCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                final int phoneIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA);

                String displayName, address, displayNameNum, number;

                while (cursor.moveToNext()) {
                    ArrayList<String> curContact = new ArrayList<>();
                    displayName = cursor.getString(displayNameIndex);
                    address = cursor.getString(emailIndex);
                    curContact.add(0, address);
                    curContact.add(1, null);
                    tempAll.put(displayName, curContact);
                }

                while (phoneCursor.moveToNext()) {
                    displayNameNum = phoneCursor.getString(displayNameIndexNum);
                    number = phoneCursor.getString(phoneIndex);
                    ArrayList<String> curContact = tempAll.get(displayNameNum);

                    if (curContact != null) {
                        curContact.add(1, number);
                        tempAll.put(displayNameNum, curContact);
                    }
                    else {
                        curContact = new ArrayList<>();
                        curContact.add(0, null);
                        curContact.add(1, number);
                        tempAll.put(displayNameNum, curContact);
                    }
                }

            } finally {
                cursor.close();
                phoneCursor.close();
            }
        }

        for(String key : tempAll.keySet()) {
            CoolList curContact = new CoolList();
            String curEmail = tempAll.get(key).get(0);
            String curPhone = tempAll.get(key).get(1);
            curContact.add(key);
            curContact.add(curEmail); // email
            curContact.add(curPhone); // number
            all.add(curContact);
        }

        everything = all;
    }
}