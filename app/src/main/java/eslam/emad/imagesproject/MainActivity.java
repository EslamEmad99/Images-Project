package eslam.emad.imagesproject;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tv)
    EditText tv;
    @BindView(R.id.imgv)
    ImageView imgv;
    @BindView(R.id.internet_btn)
    Button internetBtn;
    @BindView(R.id.phone_btn)
    Button phoneBtn;
    @BindView(R.id.camera_btn)
    Button cameraBtn;
    @BindView(R.id.submit_btn)
    Button submitBtn;
    @BindView(R.id.delete_btn)
    Button deleteBtn;
    public static final int RESULT_GALLERY = 0;
    public static final int RESULT_CAMERA = 1;
    private static final int WRITE_EXTERNAL_STORAGE_CODE = 1;
    private String currentPath;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.internet_btn)
    public void onInternetBtnClicked() {
        //Using try catch block here because picasso throws exception when inserting null string object
        //Using placeholder to handle invalid url
        try {
            String url = tv.getText().toString();
            Picasso.get().load(url).placeholder(R.drawable.invalid_url).into(imgv);
            afterLoad();
        } catch (Exception e) {
            Toast.makeText(this, "Enter url", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.phone_btn)
    public void onPhoneBtnClicked() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, RESULT_GALLERY);
    }

    @OnClick(R.id.camera_btn)
    public void onCameraBtnClicked() {
        String fileName = "photo";
        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try {
            File imageFile = File.createTempFile(fileName, ".jpg", storageDirectory);
            currentPath = imageFile.getAbsolutePath();
            imageUri = FileProvider.getUriForFile(MainActivity.this,
                    "eslam.emad.imagesproject.fileprovider", imageFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, RESULT_CAMERA);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.delete_btn)
    public void onDeleteBtnClicked() {
        delete();
    }

    @OnClick(R.id.submit_btn)
    public void onSubmitBtnClicked() {
        Bitmap bitmap = ((BitmapDrawable) imgv.getDrawable()).getBitmap();
        //Android api level 23 or higher we need WRITE_EXTERNAL_STORAGE permission to save image to external storage
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED) {
                String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permission, WRITE_EXTERNAL_STORAGE_CODE);
            } else {
                try {
                    saveImage(bitmap);
                    Toast.makeText(this, "Imaged saved successfully", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "ERROR... " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            try {
                saveImage(bitmap);
                Toast.makeText(this, "Imaged saved successfully", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "ERROR... " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveImage(Bitmap bitmap) throws Exception {
        OutputStream fileOutputStream;
        String time = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(System.currentTimeMillis());
        String imageName = time + ".jpg";
        String folderName = "Images project APP";

        //getExternalStoragePublicDirectory is deprecated in api level 29 and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, imageName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + folderName);
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            fileOutputStream = resolver.openOutputStream(imageUri);
        } else {
            String imagesDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).toString() + File.separator + folderName;

            File imagesFolder = new File(imagesDir);

            if (!imagesFolder.exists()) {
                imagesFolder.mkdir();
            }

            File image = new File(imagesDir, imageName);
            fileOutputStream = new FileOutputStream(image);
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
        fileOutputStream.flush();
        fileOutputStream.close();
        delete();
    }

    public void afterLoad() {
        imgv.setVisibility(View.VISIBLE);
        submitBtn.setVisibility(View.VISIBLE);
        deleteBtn.setVisibility(View.VISIBLE);

        tv.setVisibility(View.GONE);
        internetBtn.setVisibility(View.GONE);
        phoneBtn.setVisibility(View.GONE);
        cameraBtn.setVisibility(View.GONE);
    }

    public void delete() {
        imgv.setVisibility(View.GONE);
        submitBtn.setVisibility(View.GONE);
        deleteBtn.setVisibility(View.GONE);

        tv.setVisibility(View.VISIBLE);
        internetBtn.setVisibility(View.VISIBLE);
        phoneBtn.setVisibility(View.VISIBLE);
        cameraBtn.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_GALLERY) {
            if (resultCode == RESULT_OK && data != null) {
                Uri selectedImage = data.getData();
                imgv.setImageURI(selectedImage);
                afterLoad();
            }
        }

        if (requestCode == RESULT_CAMERA && resultCode == RESULT_OK) {
//            Bitmap bitmap = BitmapFactory.decodeFile(currentPath);
//            imgv.setImageBitmap(bitmap);
            imgv.setImageURI(imageUri);
            afterLoad();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case WRITE_EXTERNAL_STORAGE_CODE: {
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(this, "Permission enabled", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
