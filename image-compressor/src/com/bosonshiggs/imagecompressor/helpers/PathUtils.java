package com.bosonshiggs.imagecompressor.helpers;

import android.content.Context;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import android.provider.OpenableColumns;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.io.File;

public class PathUtils {
	public static String getPath(Context context, Uri uri) {
        // Check for content scheme
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Check for different types of documents
            if (isExternalStorageDocument(uri)) {
                // External storage document handling
                return getPathFromExtSD(context, uri);
            } else if (isDownloadsDocument(uri)) {
                // Downloads document handling
                return getDownloadsPath(context, uri);
            } else if (isMediaDocument(uri)) {
                // Media document handling
                return getMediaPath(context, uri);
            } else if (isGooglePhotosUri(uri)) {
                // Tratamento para URIs do Google Fotos
                return getGooglePhotosPath(context, uri);
            } else if (isGoogleDriveUri(uri)) {
                // Media document handling
                return getGoogleDrivePath(context, uri);
            } else if (isMediaPickerUri(uri)) {
                // Tratamento para URIs do tipo media picker
                return getMediaPickerPath(context, uri);
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // File scheme handling
            return uri.getPath();
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    
    private static boolean isGooglePhotosUri(Uri uri) {
        return uri.toString().startsWith("content://com.google.android.apps.photos.contentprovider/");
    }

    private static boolean isGoogleDriveUri(Uri uri) {
        return "com.google.android.apps.docs.storage".equals(uri.getAuthority());
    }
    
    private static boolean isMediaPickerUri(Uri uri) {
        return uri.toString().startsWith("content://media/picker/");
    }
    
    private static String getGooglePhotosPath(Context context, Uri uri) {
        Cursor cursor = null;
        String displayName = null;

        try {
            // Consulta para obter o nome do arquivo
            cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                displayName = cursor.getString(nameIndex);
            }

            if (displayName != null) {
                // Cria um arquivo no diretório de cache com o nome do arquivo
                File file = new File(context.getCacheDir(), displayName);
                try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
                     FileOutputStream outputStream = new FileOutputStream(file)) {

                    // Copia o conteúdo do arquivo para o arquivo local
                    int read;
                    byte[] buffer = new byte[1024];
                    while ((read = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, read);
                    }
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }

                // Retorna o caminho do arquivo local
                return file.getPath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }
    
    private static String getMediaPickerPath(Context context, Uri uri) {
        Cursor cursor = null;
        String displayName = null;

        try {
            // Consulta para obter o nome do arquivo
            cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                displayName = cursor.getString(nameIndex);
            }

            if (displayName != null) {
                // Cria um arquivo no diretório de cache com o nome do arquivo
                File file = new File(context.getCacheDir(), displayName);
                try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
                     FileOutputStream outputStream = new FileOutputStream(file)) {

                    // Copia o conteúdo do arquivo para o arquivo local
                    int read;
                    byte[] buffer = new byte[1024];
                    while ((read = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, read);
                    }
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }

                // Retorna o caminho do arquivo local
                return file.getPath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }
    
    private static String getPathFromExtSD(Context context, Uri uri) {
        String docId = DocumentsContract.getDocumentId(uri);
        String[] split = docId.split(":");
        String type = split[0];

        if ("primary".equalsIgnoreCase(type)) {
            return Environment.getExternalStorageDirectory() + "/" + split[1];
        } else {
            // Handle non-primary volumes
            File[] externalCacheDirs = context.getExternalCacheDirs();
            for (File file : externalCacheDirs) {
                if (file != null) {
                    String path = file.getAbsolutePath();
                    if (path.contains(type)) {
                        return path.split("/Android")[0] + "/" + split[1];
                    }
                }
            }
        }
        return null;
    }

    private static String getDownloadsPath(Context context, Uri uri) {
        String id = DocumentsContract.getDocumentId(uri);
        if (id != null && id.startsWith("raw:")) {
            return id.substring(4);
        }
        Uri contentUri = ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
        return getDataColumn(context, contentUri, null, null);
    }

    private static String getMediaPath(Context context, Uri uri) {
        // Logic to handle media document
        String docId = DocumentsContract.getDocumentId(uri);
        String[] split = docId.split(":");
        String type = split[0];

        Uri contentUri = null;
        if ("image".equals(type)) {
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if ("video".equals(type)) {
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else if ("audio".equals(type)) {
            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        String selection = "_id=?";
        String[] selectionArgs = new String[] { split[1] };

        return getDataColumn(context, contentUri, selection, selectionArgs);
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }
        
    private static String getGoogleDrivePath(Context context, Uri uri) {
    	Cursor cursor = null;
        String displayName = null;

        try {
            // Consulta para obter o nome do arquivo
            cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                displayName = cursor.getString(nameIndex);
            }

            if (displayName != null) {
                // Cria um arquivo no diretório de cache com o nome do arquivo
                File file = new File(context.getCacheDir(), displayName);
                try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
                     FileOutputStream outputStream = new FileOutputStream(file)) {

                    // Copia o conteúdo do arquivo para o arquivo local
                    int read;
                    byte[] buffer = new byte[1024];
                    while ((read = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, read);
                    }
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }

                // Retorna o caminho do arquivo local
                return file.getPath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }
}