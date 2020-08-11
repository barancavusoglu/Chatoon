package com.bcmobileappdevelopment.chatoon.Adapters;

import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.bcmobileappdevelopment.chatoon.HelperClass.Countries;
import com.bcmobileappdevelopment.chatoon.R;

import java.util.List;
import java.util.Locale;

public class CountryAdapter extends BaseQuickAdapter<Countries.CountriesBean, BaseViewHolder> {

    public CountryAdapter(int layout, List data)
    {
        super(layout,data);
    }

    @Override
    protected void convert(BaseViewHolder viewHolder, Countries.CountriesBean item) {
        TextView tvCountryName;
        ImageView ivFlag;
        Locale enUsLocale = new Locale("en_US");

        tvCountryName = viewHolder.getView(R.id.tvCountryName);
        ivFlag = viewHolder.getView(R.id.ivFlag);

        RequestOptions requestOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        tvCountryName.setText(item.getName());
        Glide.with(mContext).load(mContext.getResources().getIdentifier(item.getCountryCode().toLowerCase(enUsLocale), "drawable", mContext.getPackageName())).apply(requestOptions).into(ivFlag);
    }
}
