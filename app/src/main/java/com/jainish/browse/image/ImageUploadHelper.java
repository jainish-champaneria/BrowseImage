package com.jainish.browse.image;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created on 8/16/18.
 */
public class ImageUploadHelper {

    public static String saveImageBitmap(ByteArrayOutputStream bytes, Context context) {
        try {
            String path;

            String file_name = getAppName(context) + "_" + System.currentTimeMillis() + ".jpg";

            File file = new File(Environment.getExternalStorageDirectory() + "/" + getAppName(context) + "/");
            if (!file.exists()) {
                boolean fileResult = file.mkdirs();
                Log.i("mkdirs ",String.valueOf(fileResult));
            }
            File destination = new File(file, file_name);
            path = destination.getPath();
            FileOutputStream fo;
            try {
                boolean fileResult = destination.createNewFile();
                Log.i("createNewFile ",String.valueOf(fileResult));

                fo = new FileOutputStream(destination);
                fo.write(bytes.toByteArray());
                fo.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return path;
        } catch (Exception e) {
            return "";
        }
    }


    private static String getAppName(Context context) {
        return context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
    }

    public static Bitmap resizeImageForImageView(Bitmap bitmap) {
        int scaleSize = 1024;
        try {
            if (bitmap.getHeight() > scaleSize || bitmap.getWidth() > scaleSize) {
                Bitmap resizedBitmap;
                int originalWidth = bitmap.getWidth();
                int originalHeight = bitmap.getHeight();
                int newWidth = -1;
                int newHeight = -1;
                float multFactor;
                if (originalHeight > originalWidth) {
                    newHeight = scaleSize;
                    multFactor = (float) originalWidth / (float) originalHeight;
                    newWidth = (int) (newHeight * multFactor);
                } else if (originalWidth > originalHeight) {
                    newWidth = scaleSize;
                    multFactor = (float) originalHeight / (float) originalWidth;
                    newHeight = (int) (newWidth * multFactor);
                } else if (originalHeight == originalWidth) {
                    newHeight = scaleSize;
                    newWidth = scaleSize;
                }
                resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
                return resizedBitmap;
            } else {
                return bitmap;
            }
        } catch (Exception e) {
            return bitmap;
        }
    }


    public static Bitmap resetOrientation(String path,Bitmap bitmap) {
        try {
            if(bitmap==null) {
                return null;
            }

            ExifInterface ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            Bitmap rotatedBitmap;
            switch(orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap = rotateImage(bitmap, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateImage(bitmap, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateImage(bitmap, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    rotatedBitmap = bitmap;
            }
            return rotatedBitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }


    public static String getFilePath(Context context, Uri uri) {
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                if(cursor!=null) {
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    if (cursor.moveToFirst()) {
                        return cursor.getString(column_index);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(cursor!=null)
                cursor.close();

        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
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
}