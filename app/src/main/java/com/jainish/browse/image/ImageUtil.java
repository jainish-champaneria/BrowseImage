package com.jainish.browse.image;

import android.app.Activity;
import android.content.Intent;

/**
 * Created on 8/16/18.
 */
public class ImageUtil {

    public static void pickUpImageListener(Activity activity,ImageUploadCallback imageUploadCallback){
        try {
            ImageUploadModel.getInstance().setListener(imageUploadCallback);
            Intent intent = new Intent(activity, ImageUploadActivityUtil.class);
            activity.startActivity(intent);
        }catch (Exception e)
        {e.printStackTrace();}
    }

    public static void pickUpImageListener(Activity activity,ImageUploadCallback imageUploadCallback,boolean shouldAskToRemove){
        try {
            ImageUploadModel.getInstance().setListener(imageUploadCallback, shouldAskToRemove);
            Intent intent = new Intent(activity, ImageUploadActivityUtil.class);
            activity.startActivity(intent);
        }catch (Exception e)
        {e.printStackTrace();}
    }

    public static void pickUpImageActivityResult(Activity activity,int requestCode) {
        try {
            Intent intent = new Intent(activity, ImageUploadActivityUtil.class);
            activity.startActivityForResult(intent, requestCode);
        }catch (Exception e)
        {e.printStackTrace();}
    }
}
