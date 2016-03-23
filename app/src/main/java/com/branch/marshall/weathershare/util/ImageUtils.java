package com.branch.marshall.weathershare.util;

import android.content.Context;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created by marshall on 3/23/16.
 */
public class ImageUtils {
    private static ImageUtils sInstance;
    private static Object sLock = new Object();

    public static ImageUtils getInstance() {
        if (sInstance == null) {
            synchronized (sLock) {
                if (sInstance == null)
                    sInstance = new ImageUtils();
            }
        }

        return sInstance;
    }

    private ImageUtils() {

    }

    public void init(Context context) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .build();

        ImageLoader.getInstance().init(config);
    }

    public void loadImage(String imageUri, ImageView imageView) {
        ImageLoader.getInstance().displayImage(imageUri, imageView);
    }
}
