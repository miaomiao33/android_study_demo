package com.example.android_study_demo_project.dataBindingUsage.eachOther;

import android.util.Log;
import android.view.View;

import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.InverseBindingListener;

import java.util.Objects;

//自定义双向绑定的自定义属性
public class EachOtherDataBindingAdapters {

    //正向绑定：定义该自定义CustomView的自定义控件属性time，属性变化就会调用setTime
    @BindingAdapter("time")
    public static void setTime(CustomView view, String newValue)
    {
        Log.i("MyBindingConversion","setTime: "+newValue);
        //比较新旧数值避免死循环
        if(!Objects.equals(view.data, newValue))
        {
            view.data = newValue;
        }
    }

    //反向绑定：定义转换器Adapter，给time属性设置了反向绑定到timeAttrChanged
    @InverseBindingAdapter(attribute = "time",event = "timeAttrChanged")
    public static String getTime(CustomView view)
    {
        Log.i("MyBindingConversion","getTime:");
        return view.data;
    }

    //定义该自定义view的自定义控件属性timeAttrChanged
    @BindingAdapter("timeAttrChanged")
    public static void setListeners(CustomView view, InverseBindingListener attrChange)
    {
        Log.i("MyBindingConversion","setListeners:");
        // 设置 view 改变的监听，看需求可以是点击，滑动，双击，长按什么的。
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view1) {
                view.data = "click";
                attrChange.onChange();
            }
        });
    }
}
