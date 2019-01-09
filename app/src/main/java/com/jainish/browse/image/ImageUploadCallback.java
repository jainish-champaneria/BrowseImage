package com.jainish.browse.image;

/**
 * Created on 8/16/18.
 */
public interface ImageUploadCallback {

    void imageUploadCancel(String cancelReason);
    void imageUploadPath(String path);
    void imageRemove();

}
