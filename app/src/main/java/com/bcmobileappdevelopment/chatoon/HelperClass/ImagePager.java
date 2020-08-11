package com.bcmobileappdevelopment.chatoon.HelperClass;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.viewpager.widget.PagerAdapter;
import com.bcmobileappdevelopment.chatoon.R;
import com.bumptech.glide.Glide;
import com.ortiz.touchview.TouchImageView;
import java.util.ArrayList;

public class ImagePager extends PagerAdapter {

    private Context context;
    private ArrayList<String> imageList;

    public ImagePager(Context context, ArrayList<String> images) {
        this.context = context;
        this.imageList = images;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.viewpager_item, null);
        TouchImageView imageView = view.findViewById(R.id.imageView);
        Glide.with(context).load(imageList.get(position)).into(imageView);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object view) {
        container.removeView((View) view);
    }

    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return object == view;
    }

}
