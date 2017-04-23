package com.banana.banana;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.Pixa;
import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Rect;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class OpenCamera extends AppCompatActivity {

    public static final String PACKAGE_NAME = "com.datumdroid.android.ocr.simple";
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";

    private Button takePictureButton;
    private String recognizedText1 = "";
    private String recognizedText0 = "";
    final int REQUEST_TAKE_PHOTO = 1;
    final int CROP_PIC = 2;
    private static String mCurrentPhotoPath;
    private Uri photoURI;

    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + "/SimpleAndroidOCR/";
    // You should have the trained data file in assets folder
    // You can get them at:
    // https://github.com/tesseract-ocr/tessdata
    public static final String lang = "eng";
    private static final String TAG = "SimpleAndroidOCR.java";

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
            System.out.println("UH OH");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_camera);

        Log.v(TAG,"HELLO???");

        String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };

        Log.v(TAG,"hi!");
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

        Log.v(TAG,"nice to see you");
        // lang.traineddata file with the app (in assets folder)
        // You can get them at:
        // http://code.google.com/p/tesseract-ocr/downloads/list
        // This area needs work and optimization
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
                //while ((lenf = gin.read(buff)) > 0) {
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                //gin.close();
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
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                takePictureButton.setEnabled(true);
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
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "in result handler");
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Uri imageUri = Uri.parse(mCurrentPhotoPath);

            ImageView rawView = (ImageView) findViewById(R.id.imageview);
            rawView.setImageURI(imageUri);

            performCrop();
        }
        // user is returning from cropping the image
        else if (requestCode == CROP_PIC) {
            // get the cropped bitmap
            Uri imageUri = Uri.parse(mCurrentPhotoPath);
            onPhotoTaken(false);

            File mFile = new File(imageUri.getPath()); //maybe just have mCurrentPhotoPath?
            //ReceiptScanner scanner = new ReceiptScanner();
            //scanner.refine(mFile);

            ImageView croppedView = (ImageView) findViewById(R.id.imageview);
            croppedView.setImageURI(imageUri);
            onPhotoTaken(true);
        }
    }

    private void performCrop() {
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

        ImageView croppedView = (ImageView) findViewById(R.id.imageview);
        croppedView.setImageURI(photoURI);

        try {
            Log.v(TAG, "in crop");

            try {
                Intent cropIntent = new Intent("com.android.camera.action.CROP");
                // indicate image type and Uri
                cropIntent.setDataAndType(photoURI, "image/*");
                cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                //cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, croppedUri);
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
            storageDir.mkdir();
            System.out.println("don't exist!, so we created it");
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

    protected void onPhotoTaken(Boolean optimized) {
        String outputText;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath.replaceFirst("file:", ""), options);

        //_image.setImageBitmap( bitmap );
        ImageView imageView = (ImageView) findViewById(R.id.imageview);
        imageView.setImageBitmap(bitmap);

        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        if (!baseApi.setVariable("tessedit_write_images", "T"))
            Log.v(TAG, "COULDN'T SET SETTING!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!11");
        else
            Log.v(TAG, "I DID SET THE SETTING!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!11");
        baseApi.init(DATA_PATH, lang);
        baseApi.setImage(bitmap);
        outputText = baseApi.getUTF8Text();
        if (optimized)
            recognizedText1 = outputText;
        else
            recognizedText0 = outputText;
        System.out.println("RECOGNIZED TEXT:\n\n\n"+outputText + "\n\n\n");

        baseApi.end();
    }
    /** Called when the user taps the Send button */
    public void sendMessage(View view) {
        Intent intent = new Intent(this, EditReceipt.class);
        //intent.putExtra(EXTRA_MESSAGE, recognizedText);
        startActivity(intent);
    }
}