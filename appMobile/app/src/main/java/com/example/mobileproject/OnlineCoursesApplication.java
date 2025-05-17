package com.example.mobileproject;


import android.app.Application;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

public class OnlineCoursesApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Cấu hình Glide để tối ưu tải hình ảnh
        GlideBuilder builder = new GlideBuilder();
        builder.setDefaultRequestOptions(
                new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache tất cả hình ảnh
                        .skipMemoryCache(false) // Sử dụng bộ nhớ cache
        );
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        // Xóa cache của Glide khi thiết bị ít bộ nhớ
        Glide.get(this).clearMemory();
    }
}
