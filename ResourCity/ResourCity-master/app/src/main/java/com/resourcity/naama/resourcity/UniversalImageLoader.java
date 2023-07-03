package com.resourcity.naama.resourcity;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;

// watch : https://www.youtube.com/watch?v=rY7IUXT-AHY&index=16&t=0s&list=PLgCYzUzKIBE9XqkckEJJA0I1wVKbUAOdv
public class UniversalImageLoader {

    private static final int defaultImage = R.drawable.img;
    private Context mContext;

    public UniversalImageLoader(Context context) {
        mContext = context;
    }

    public ImageLoaderConfiguration getConfig(){
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(defaultImage)
                .showImageForEmptyUri(defaultImage)
                .showImageOnFail(defaultImage)
                .considerExifParams(true)
                .cacheOnDisk(true).cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(mContext)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .diskCacheSize(100 * 1024 * 1024).build();

        return configuration;
    }

    /**
     * this method can be sued to set images that are static. It can't be used if the images
     * are being changed in the Fragment/Activity - OR if they are being set in a list or a grid
     * @param imgURL
     * @param image
     * @param mProgressBar
     * @param append
     */

//     TODO: see all options for imgURL !!!
//    "http://site.com/image.png" // from Web
//    "file:///mnt/sdcard/image.png" // from SD card
//    "file:///mnt/sdcard/video.mp4" // from SD card (video thumbnail)
//    "content://media/external/images/media/13" // from content provider
//    "content://media/external/video/media/13" // from content provider (video thumbnail)
//    "assets://image.png" // from assets
//    "drawable://" + R.drawable.img // from drawables (non-9patch images)
    public static void setImage(String imgURL, ImageView imageView, final ProgressBar mProgressBar, String append){

//        String decodedImgUri = Uri.fromFile(new File(imgURL)).toString();
//        ImageLoader.getInstance().displayImage(decodedImgUri, imageView);
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(append + imgURL, imageView, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                if(mProgressBar != null){
                    mProgressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if(mProgressBar != null){
                    mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if(mProgressBar != null){
                    mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                if(mProgressBar != null){
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        });
    }
}
