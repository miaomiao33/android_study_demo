package com.example.android_study_demo_project.internetImageUsage;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.example.android_study_demo_project.R;
import com.example.android_study_demo_project.internetImageUsage.model.InternetImageModel;

import java.util.List;

public class InternetImageRecyclerViewAdapter extends
        RecyclerView.Adapter<InternetImageRecyclerViewAdapter.BaseViewHolder> {
    List<InternetImageModel> modelList;
    Context context;

    public InternetImageRecyclerViewAdapter(List<InternetImageModel> modelList,Context context) {
        this.modelList = modelList;
        this.context = context;
    }

    @NonNull
    @Override
    public InternetImageAdapterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //item样式
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.internet_image_item,parent,false);
        Log.i("params1","onCreateViewHolder");
        return new InternetImageAdapterHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        //渲染第几个样式
        Log.i("params","123");
        holder.setData(modelList.get(position));
    }

    @Override
    public int getItemCount() {
        return modelList == null ? 0 : modelList.size();
    }

    //新的ViewHolder写法
    public class BaseViewHolder extends RecyclerView.ViewHolder {
        public BaseViewHolder(View itemView) {
            super(itemView);
        }
        void setData(Object data) {
        }
    }

    class InternetImageAdapterHolder extends BaseViewHolder{
        private ImageView imageView;
        private TextView textView;
        public InternetImageAdapterHolder(@NonNull View itemView) {
            super(itemView);
            imageView = (ImageView)itemView.findViewById(R.id.iv_internet_image);
            textView = (TextView)itemView.findViewById(R.id.tv_internet_image_name);
            int windowWith = ((Activity) itemView.getContext())
                             .getWindowManager()
                             .getDefaultDisplay().getWidth();
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            //设置图片的相对于屏幕的宽高比
            params.width = windowWith/2-30;
            params.height =  (int) (params.width + Math.random() * 400) ;//随机才能变成瀑布流
            Log.i("params1",params.width+"---"+params.height);
            imageView.setLayoutParams(params);
        }

        @Override
        void setData(Object model) {
            if(model != null)
            {
                String URL = ((InternetImageModel)model).getUrl();
                int id = ((InternetImageModel)model).getId();

                //解决瀑布流跳动以及重新绘制问题
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) imageView.getLayoutParams();
                if(((InternetImageModel) model).getHeight() == 0)
                {
                    // 获取布局页面的尺寸和密度
                    Resources resources = context.getResources();
                    DisplayMetrics dm = resources.getDisplayMetrics();
                    // 获取到图片的布局
                    ((InternetImageModel) model).setHeight(layoutParams.height);
                    ((InternetImageModel) model).setWidth(layoutParams.width);
                }else
                {
                    layoutParams.height = ((InternetImageModel) model).getHeight();
                    layoutParams.width = ((InternetImageModel) model).getWidth();
                }
                // 重新给图片设置布局
                imageView.setLayoutParams(layoutParams);

                RequestOptions requestOptions = new RequestOptions()
                        .placeholder(R.mipmap.ic_launcher)//loading时显示
                        .error(R.drawable.ic_launcher_background)
                        .fallback(R.drawable.ic_launcher_background)//加载后图片为空显示

                        //既缓存原始图片，又缓存转化后的图片
                        .diskCacheStrategy(DiskCacheStrategy.ALL);
                DrawableCrossFadeFactory factory = new DrawableCrossFadeFactory.Builder()
                        .setCrossFadeEnabled(true)
                        .build();
                Glide.with(itemView.getContext())
                        .load(URL)
                        .apply(requestOptions)
                        .transition(DrawableTransitionOptions.withCrossFade(factory))
                        .into(imageView);
                if(textView != null)
                {
                    textView.setText(String.format("%d",id));
                }
            }
        }
    }
}
