package com.example.cathleen.testcamera;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MYTAG";
    String photoPath;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO_AND_SAVE_TO_PUBLIC = 2;
    static final int REQUEST_TAKE_PHOTO_AND_SAVE_TO_PRIVATE = 3;
    Button mini,publicOne,privateOne;
    ImageView miniPicture = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mini = (Button) findViewById(R.id.mini);
        publicOne = (Button) findViewById(R.id.publicOne);
        privateOne = (Button) findViewById(R.id.privateOne);
        miniPicture = (ImageView) findViewById(R.id.miniPicture);

        //判断相机有无
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            mini.setEnabled(false);
        }
        //显示缩略图
        mini.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePhotoIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        //判断相机有无
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            mini.setEnabled(false);
        }
        //拍照+公有目录
        publicOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent saveToPublicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (saveToPublicIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFileInPublicDir();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    if (photoFile != null) {
                        saveToPublicIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                        startActivityForResult(saveToPublicIntent, REQUEST_TAKE_PHOTO_AND_SAVE_TO_PUBLIC);
                    }
                }
            }
        });
        //判断相机有无
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            privateOne.setEnabled(false);
        }
        //拍照+私有目录
        privateOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFileInPrivateDir();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO_AND_SAVE_TO_PRIVATE);
                    }
                }
            }
        });
    }
    //
    @Override
    protected void onActivityResult ( int requestCode, int resultCode, Intent data){
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            miniPicture.setImageBitmap(imageBitmap);
        } else if (requestCode == REQUEST_TAKE_PHOTO_AND_SAVE_TO_PUBLIC && resultCode == RESULT_OK) {
            setPic();//显示缩略图
            addPicTogallery();
        } else if (requestCode == REQUEST_TAKE_PHOTO_AND_SAVE_TO_PRIVATE && resultCode == RESULT_OK) {
            setPic();//显示缩略图
        }
    }
    //显示缩略图
    private void setPic() {
        int targetW = 20;// mImageView.getWidth();
        int targetH = 20;// mImageView.getHeight();
        //设置仅加载位图边界信息（相当于位图的信息，但没有加载位图）
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        //返回为NULL，即不会返回bitmap,但可以返回bitmap的横像素和纵像素还有图片类型
        BitmapFactory.decodeFile(photoPath, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, bmOptions);
        miniPicture.setImageBitmap(bitmap);
    }
    //将照片添加到MediaProvider中,以便Android相册等其他程序就可以读取并显示照片
    private void addPicTogallery() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(photoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    //创建公有路径
    private File createImageFileInPublicDir() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        //如果路径不存在，则创建之
        if (!storageDir.exists()) {
            Log.v(TAG, "目录不存在，创建之");
            if (!storageDir.mkdirs()) {
                Log.v(TAG, "目录创建失败");
                return null;
            }
        }
        File image = new File(storageDir, imageFileName + "photo.jpg");
        photoPath = image.getAbsolutePath();
        //输出路径信息
        Log.v(TAG, "photoPath:" + photoPath);
        return image;
    }
    //创建私有路径
    public File createImageFileInPrivateDir() throws IOException {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        //如果路径不存在，则创建之
        if (!storageDir.exists()) {
            Log.v(TAG, "目录不存在，创建之");
            if (!storageDir.mkdirs()) {
                Log.v(TAG, "目录创建失败");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File image = File.createTempFile(imageFileName,".jpg", storageDir);
        photoPath = image.getAbsolutePath();
        Log.v(TAG, "photoPath:" + photoPath);
        return image;
    }
}

