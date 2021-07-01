package com.nodz.wall.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.nodz.wall.R;
import com.nodz.wall.databinding.ActivitySetWallpaperBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class SetWallpaperActivity extends AppCompatActivity {

    String ImgUrl = "", img_location="";
    Bitmap bitmap;
    String imageFileName = "IMG_" + System.currentTimeMillis() + ".jpg";
    File dir;
    ActivitySetWallpaperBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetWallpaperBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActivityCompat.requestPermissions(SetWallpaperActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        ActivityCompat.requestPermissions(SetWallpaperActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},1);

        //getSupportActionBar().hide();
        ImgUrl = getIntent().getStringExtra("ImgUrl");

        try {
            Glide.with(this)
                    .load(ImgUrl)
                    .into(binding.imageWall);
        } catch (Exception e) {
            e.printStackTrace();
        }

        binding.setwallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetImageFromUrl getImageFromUrl = new GetImageFromUrl();
                getImageFromUrl.execute(ImgUrl);

            }
        });
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("Download","DownNotif",NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

    binding.getWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage();
/*
                Intent notifyIntent = new Intent();
                notifyIntent.setAction(Intent.ACTION_GET_CONTENT);
                notifyIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                notifyIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                notifyIntent.setDataAndType(FileProvider.getUriForFile(SetWallpaperActivity.this,getApplicationContext().getPackageName()+".provider",dir), "file/*");
                PendingIntent pendingIntent = PendingIntent.getActivity(SetWallpaperActivity.this, 1, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
*/
                NotificationCompat.Builder builder = new NotificationCompat.Builder(SetWallpaperActivity.this, "Download");
                builder.setContentTitle("DownloadedWallpaper");
                builder.setContentText("Location : sdCard0/IMAGES/"+imageFileName);
                builder.setSmallIcon(R.drawable.ic_baseline_arrow_downward_24);
                //builder.setContentIntent(pendingIntent);
                builder.setAutoCancel(true);

                NotificationManagerCompat managerCompat = NotificationManagerCompat.from(SetWallpaperActivity.this);
                managerCompat.notify(1,builder.build());
       }
        });
    }

    public void saveImage(){

        BitmapDrawable drawable = (BitmapDrawable) binding.imageWall.getDrawable();
        Bitmap bmp = drawable.getBitmap();

        FileOutputStream outputStream = null;
        File file = Environment.getExternalStorageDirectory();
        dir = new File(file.getAbsolutePath()+"/IMAGES");
        //img_location = file.getAbsolutePath();
        File dir = new File(file.getAbsolutePath()+"/IMAGES");

        if(!dir.exists())
            dir.mkdir();

        File outFile = new File(dir, imageFileName);
        try{
            outputStream = new FileOutputStream(outFile);
            bmp.compress(Bitmap.CompressFormat.PNG,100, outputStream);

        } catch (Exception e) {
            e.printStackTrace();
        }
        try{
            outputStream.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }try{
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Downloaded  :"+dir.getAbsolutePath(), Toast.LENGTH_LONG).show();
        Log.i("PATH :",dir.getAbsolutePath());
    }

    public class GetImageFromUrl extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... url) {
            String stringUrl = url[0];
            bitmap = null;
            InputStream inputStream;
            try {
                inputStream = new URL(stringUrl).openStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int height = metrics.heightPixels;
            int width = metrics.widthPixels;

            int imageSize = 0;
            imageSize = (height > width) ? width : height;

            Bitmap bmp2 = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, true);
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());

            try {
                wallpaperManager.setBitmap(bmp2);
                Toast.makeText(SetWallpaperActivity.this, "Wallpaper Applied", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
