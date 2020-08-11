package com.bcmobileappdevelopment.chatoon.Adapters;

import android.widget.ImageView;
import android.widget.TextView;
import com.bcmobileappdevelopment.chatoon.GsonResponse.GetPremiumListResponse;
import com.bcmobileappdevelopment.chatoon.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

public class PremiumAdapter extends BaseQuickAdapter<GetPremiumListResponse.PremiumListBean, BaseViewHolder> {

    public PremiumAdapter(int layout, List data)
    {
        super(layout,data);
    }

    @Override
    protected void convert(BaseViewHolder viewHolder, GetPremiumListResponse.PremiumListBean item) {
        TextView tvUsername;
        ImageView ivProfilePic;
        tvUsername = viewHolder.getView(R.id.tvUsername);
        ivProfilePic = viewHolder.getView(R.id.ivProfilePic);

        RequestOptions requestOptions = new RequestOptions()
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        tvUsername.setText(item.getUsername());
        Glide.with(mContext).load(item.getProfilePicURL()).apply(requestOptions).into(ivProfilePic);
    }
}
