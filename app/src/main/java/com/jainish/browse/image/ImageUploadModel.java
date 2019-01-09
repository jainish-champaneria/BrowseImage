package com.jainish.browse.image;

/**
 * Created on 8/16/18.
 */
public class ImageUploadModel {

    private static ImageUploadModel mInstance;

    public ImageUploadCallback mListener;

    public boolean shouldAskToRemove = true;

    public static ImageUploadModel getInstance() {
        if(mInstance == null) {
            mInstance = new ImageUploadModel();
        }
        return mInstance;
    }

    public void setListener(ImageUploadCallback listener) {
        setListener(listener,true);
    }

    public void setListener(ImageUploadCallback listener, boolean shouldAskToRemove) {
        mListener = listener;
        this.shouldAskToRemove = shouldAskToRemove;
    }

    public void destroy(){
        mListener=null;
        mInstance=null;
    }
}