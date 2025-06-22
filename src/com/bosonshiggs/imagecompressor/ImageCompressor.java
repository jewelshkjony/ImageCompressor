package com.bosonshiggs.imagecompressor;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.google.appinventor.components.annotations.Asset;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.runtime.ActivityResultListener;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;

@DesignerComponent(
    version = 7, 
    versionName = "1.0", 
    description = "An extension for compressing images to JPEG, PNG, or WebP.", 
    iconName = "icon.png"
)
public class ImageCompressor extends AndroidNonvisibleComponent implements ActivityResultListener {
    private final ComponentContainer container;
    private boolean flagLog = false;
    private File imageFile = null;
    private static final String myDirName = "CompressedImages";

    // Constructor
    public ImageCompressor(ComponentContainer container) {
        super(container.$form());
        this.container = container;
        container.$form().registerForActivityResult(this);
    }

    @SimpleProperty(description = "Enable or disable logging to Logcat")
    public void LogEnabled(boolean enabled) {
        this.flagLog = enabled;
    }

    @SimpleFunction(description = "Converts a content URI to a file system path.")
    public String PathFromUri(String stringUri) {
        if (flagLog)
            Log.d("ImageCompressor", "Converting URI to path: " + stringUri);
        if (stringUri == null || stringUri.isEmpty()) {
            return "";
        }
        Uri uri = Uri.parse(stringUri);
        String[] projection = { MediaStore.Images.Media.DATA };
        try (Cursor cursor = container.$context().getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                return cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            if (flagLog)
                Log.e("ImageCompressor", "Error converting URI to path", e);
        }
        return stringUri;
    }

    @SimpleFunction(description = "Compresses the image at the given path to the specified format and quality.")
    public void CompressImage(@Asset final String imagePath, final String imageName, final int quality, final int id) {
        if (flagLog)
            Log.d("ImageCompressor", "Starting image compression: " + imagePath);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File inputFile = new File(imagePath);
                    Bitmap bitmap = BitmapFactory.decodeFile(inputFile.getAbsolutePath());

                    String imageType = getFileExtension(imagePath).toLowerCase();

                    File picturesPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    File appImagesPath = new File(picturesPath, myDirName);
                    if (!appImagesPath.exists()) {
                        appImagesPath.mkdirs();
                    }

                    String baseName = imageName;
                    imageFile = new File(appImagesPath, imageName + "." + imageType);
                    int counter = 1;

                    // Check if file exists and adjust name
                    while (imageFile.exists()) {
                        String newImageName = baseName + "_" + counter;
                        imageFile = new File(appImagesPath, newImageName + "." + imageType);
                        counter++;
                    }

                    FileOutputStream fos = new FileOutputStream(imageFile);

                    if (imageType.equals("jpg") || imageType.equals("jpeg")) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
                    } else if (imageType.equals("png")) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, quality, fos);
                    } else if (imageType.equals("webp")) {
                        // Always use WEBP_LOSSY to avoid deprecated constant
                        bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, quality, fos);
                    } else {
                        if (flagLog)
                            Log.e("ImageCompressor", "Unsupported file format: " + imageType);
                        fos.close();
                        return;
                    }

                    fos.close();
                    MediaScannerConnection.scanFile(form, new String[] { imageFile.getAbsolutePath() }, null, null);

                    container.$form().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ImageSaved(id, imageFile.getAbsolutePath());
                            if (flagLog)
                                Log.i("ImageCompressor", "Image compressed and saved: " + imageFile.getAbsolutePath());
                        }
                    });
                } catch (final Exception e) {
                    if (flagLog)
                        Log.e("ImageCompressor", "Error compressing and saving image", e);
                    container.$form().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ErrorOccurred(e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public void resultReturned(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri selectedImageUri = data.getData();
            String path = PathFromUri(selectedImageUri.toString());
            ImageSelected(path);
        }
    }

    @SimpleEvent(description = "Triggered when an image is selected.")
    public void ImageSelected(String imagePath) {
        EventDispatcher.dispatchEvent(this, "ImageSelected", imagePath);
    }

    @SimpleEvent(description = "Triggered when an image is saved.")
    public void ImageSaved(int id, String filePath) {
        EventDispatcher.dispatchEvent(this, "ImageSaved", id, filePath);
    }

    @SimpleEvent(description = "Triggered when an error occurs during image compression.")
    public void ErrorOccurred(String errorMessage) {
        EventDispatcher.dispatchEvent(this, "ErrorOccurred", errorMessage);
    }

    // Helper method to extract the file extension
    private String getFileExtension(String fileName) {
        int lastIndexOf = fileName.lastIndexOf('.');
        if (lastIndexOf == -1) {
            return ""; // no extension found
        }
        return fileName.substring(lastIndexOf + 1);
    }
}
