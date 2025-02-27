package com.example.android_study_demo_project.media;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MediaSoundPoolAdapter extends RecyclerView.Adapter<MediaSoundPoolAdapter.MediaSoundPoolViewHolder> {

    private final Context context;
    private final RecyclerView recyclerView;
    private List<Sound> modelList;
    private OnItemClickListener listener;

    public MediaSoundPoolAdapter(Context context, RecyclerView recyclerView,List<Sound> modelList) {
        this.context = context;
        this.recyclerView = recyclerView;
        this.modelList = modelList;
    }

    @NonNull
    @Override
    public MediaSoundPoolViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //手动写简单的item布局
        TextView textView = new TextView(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = 18;
        layoutParams.bottomMargin = 18;
        layoutParams.leftMargin = 18;
        textView.setLayoutParams(layoutParams);
        textView.setOnClickListener(new itemClick());

        return new MediaSoundPoolViewHolder(textView);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaSoundPoolViewHolder holder, int position) {
        TextView itemTextView = (TextView) holder.itemView;//因为就一个，所以itemView就是TextView
        itemTextView.setText(modelList.get(position).name);
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }


    class MediaSoundPoolViewHolder extends RecyclerView.ViewHolder{

        //每项item拥有的TextView这些
        public MediaSoundPoolViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    interface OnItemClickListener{
        //把位置和点击的model暴露出去
        void onItemClick(int position, Sound model);
    }

    public void  setOnItemClickListener(OnItemClickListener listener)
    {
        this.listener = listener;
    }

    //把item点击暴露出去的写法
    class itemClick implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            //获得点击的view位置
            if(listener != null)
            {
                int position = recyclerView.getChildAdapterPosition(view);
                listener.onItemClick(position,modelList.get(position));
            }
        }
    }

}
