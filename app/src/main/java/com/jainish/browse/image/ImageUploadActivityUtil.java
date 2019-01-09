package com.jainish.browse.image;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

/**
 * Created on 8/16/18.
 */
public class ImageUploadActivityUtil extends Activity implements DialogInterface.OnClickListener {

    private int REQUEST_CAMERA = 120;
    private int SELECT_FILE = 115;
    final int REQUEST_PERMISSION_CODE = 109;
    private String PATH = "";
    private Uri mainUri;

    private final String TAKE_PHOTO = "Take photo";
    private final String CHOOSE_LIB = "Choose from library";
    private final String REMOVE_IMAGE = "Remove image";
    private final String CANCEL_DIALOG = "Cancel";

    public final static String RESULT_KEY = "result";
    public final static String RES_CANCELED = "cancel";
    public final static String RES_PERMISSION_ERROR = "no_permission";
    public final static String RES_IMAGE_NOT_FOUND = "image_not_found";
    public final static String RES_REMOVE = "remove";

    private CharSequence[] dialogData;

    private ImageUploadCallback imageUploadCallback = null;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startFromCreate();
    }

    void startFromCreate() {
        if (checkPermission()) {
            ImageUploadModel imageUploadModel = ImageUploadModel.getInstance();
            imageUploadCallback = imageUploadModel.mListener;
            boolean shouldAskToRemove = imageUploadModel.shouldAskToRemove;
            askDialog(shouldAskToRemove, this);
        }
    }

    void sendBackResult(int resultCode) {
        try {

            Intent intent = new Intent();
            intent.putExtra(RESULT_KEY, PATH);
            setResult(resultCode, intent);
            ImageUploadModel.getInstance().destroy();

        } catch (Exception e) {
            e.printStackTrace();
        }

        finish();
    }

    public void askDialog(boolean shouldAskToRemove, final Activity activity) {
        try {

            if (shouldAskToRemove) {
                dialogData = new CharSequence[]{TAKE_PHOTO, CHOOSE_LIB, REMOVE_IMAGE, CANCEL_DIALOG};
            } else {
                dialogData = new CharSequence[]{TAKE_PHOTO, CHOOSE_LIB, CANCEL_DIALOG};
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setCancelable(false);
            builder.setItems(dialogData, this);
            builder.show();

        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }


    @Override
    public void onClick(DialogInterface dialogInterface, int item) {
        try {
            dialogInterface.dismiss();

            Intent intent;

            switch (dialogData[item].toString()) {

                case TAKE_PHOTO:

                    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    Uri mPhotoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
                    mainUri = mPhotoUri;
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivityForResult(intent, REQUEST_CAMERA);

                    break;

                case CHOOSE_LIB:

                    intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);//
                    startActivityForResult(Intent.createChooser(intent, "Select Application"), SELECT_FILE);

                    break;

                case REMOVE_IMAGE:

                    PATH = RES_REMOVE;
                    if (imageUploadCallback != null)
                        imageUploadCallback.imageRemove();
                    sendBackResult(RESULT_OK);


                    break;

                case CANCEL_DIALOG:

                    PATH = RES_CANCELED;
                    if (imageUploadCallback != null)
                        imageUploadCallback.imageUploadCancel("user select cancel");
                    sendBackResult(RESULT_CANCELED);

                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static Bitmap getImageFromPath(String path) {
        try {
            File imgFile = new File(path);

            if (imgFile.exists()) {
                return BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm = null;
        if (data != null) {
            try {
                try {
                    bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                } catch (Exception ee) {
                    String path = ImageUploadHelper.getFilePath(this, data.getData());
                    File imgFile = null;
                    if (path != null) {
                        imgFile = new File(path);
                    }
                    if (imgFile != null && imgFile.exists()) {
                        bm = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    }

                }
                bm = ImageUploadHelper.resizeImageForImageView(bm);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 95, bytes);


                PATH = ImageUploadHelper.saveImageBitmap(bytes, this);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                onSelectFromGalleryResult(data);
            } else if (requestCode == REQUEST_CAMERA) {
                onCaptureImageResult(data);
            }

            if (imageUploadCallback != null) {
                if (TextUtils.isEmpty(PATH)) {
                    PATH = RES_IMAGE_NOT_FOUND;
                    imageUploadCallback.imageUploadCancel("Image not found");
                } else {
                    imageUploadCallback.imageUploadPath(PATH);
                }
            }

            sendBackResult(RESULT_OK);

        } else {
            PATH = RES_CANCELED;

            if (imageUploadCallback != null)
                imageUploadCallback.imageUploadCancel("Back button pressed");

            sendBackResult(RESULT_CANCELED);
        }

    }

    private void onCaptureImageResult(Intent data) {

        try {
            Bitmap thumbnail = null;
            try {
                InputStream is = getContentResolver().openInputStream(mainUri);
                thumbnail = BitmapFactory.decodeStream(is);
                if (thumbnail == null) {
                    thumbnail = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mainUri);
                    if (thumbnail == null) {
                        if (data.getExtras() != null)
                            thumbnail = (Bitmap) data.getExtras().get("data");
                    }
                }
            } catch (Exception ee) {
                String path = ImageUploadHelper.getFilePath(this, mainUri);
                File imgFile = null;
                if (path != null) {
                    imgFile = new File(path);
                }
                if (imgFile != null && imgFile.exists()) {
                    thumbnail = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                }
            }


            thumbnail = ImageUploadHelper.resetOrientation(mainUri.getPath(), thumbnail);

            File fdelete = new File(mainUri.getPath());
            if (fdelete.exists()) {
                boolean deleteResult = fdelete.delete();
                Log.i("File deleted ", String.valueOf(deleteResult));
            }

            thumbnail = ImageUploadHelper.resizeImageForImageView(thumbnail);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 95, bytes);


            PATH = ImageUploadHelper.saveImageBitmap(bytes, this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean checkPermission() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {


                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_PERMISSION_CODE);


                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_PERMISSION_CODE);
                }
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    startFromCreate();

                } else {

                    PATH = RES_PERMISSION_ERROR;
                    if (imageUploadCallback != null)
                        imageUploadCallback.imageUploadCancel("permission not granted");
                    sendBackResult(RESULT_CANCELED);

                }
            }
        }
    }


    @Override
    public void onBackPressed() {
        PATH = RES_CANCELED;
        if (imageUploadCallback != null)
            imageUploadCallback.imageUploadCancel("back button pressed");
        sendBackResult(RESULT_CANCELED);
    }
}


