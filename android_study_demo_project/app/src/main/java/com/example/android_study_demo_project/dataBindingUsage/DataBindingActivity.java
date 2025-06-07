package com.example.android_study_demo_project.dataBindingUsage;

import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.android_study_demo_project.R;
import com.example.android_study_demo_project.dataBindingUsage.eachOther.CustomViewModel;
import com.example.android_study_demo_project.databinding.ActivityDatabindingUsageBinding;

public class DataBindingActivity extends AppCompatActivity {

    private String informationText = "text初始值";
    private boolean hasChanged = false;
    private String includeViewContent = "这是includeView的内容";
    private String stubViewContent = "这是stubView的内容";
    private Presenter presenter;
    private ActivityDatabindingUsageBinding activityDatabindingUsageBinding;
    private Person person1;

    private ObservableField<String> content;

    private CustomViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityDatabindingUsageBinding = DataBindingUtil.setContentView(
                DataBindingActivity.this, R.layout.activity_databinding_usage);
        initView();
    }

    void initView()
    {
        //简单使用
        //设置初始值
        activityDatabindingUsageBinding.setInformationText(informationText);
        activityDatabindingUsageBinding.setHasChanged(hasChanged);
        //include和StubView的使用
        activityDatabindingUsageBinding.setIncludeViewContent(includeViewContent);
        activityDatabindingUsageBinding.setStubViewContent(stubViewContent);
        ViewStub stub = findViewById(R.id.view_stub);
        //避免重复调用 inflate()
        if (stub != null && stub.getParent() != null) {
            View inflated = stub.inflate();
        }
        //设置关联的点击事件
        presenter = new Presenter();
        activityDatabindingUsageBinding.setPresenter(presenter);

        //单向绑定
        //设置关联的person实例
        person1 = new Person(18,"初始化name","初始化的Id");
        activityDatabindingUsageBinding.setPerson(person1);
        //设置关联的ObservablePerson实例
        activityDatabindingUsageBinding.setPerson2(new ObservablePerson("初始化name",18,123L));
        //双向绑定
        //非自定义
        content = new ObservableField<>("Content ");
        activityDatabindingUsageBinding.setContent(content);

        //自定义
        viewModel = new ViewModelProvider(this).get(CustomViewModel.class);
        MutableLiveData<String> mutableLiveData = new MutableLiveData<>("start");
        viewModel.setMutableLiveData(mutableLiveData);
        activityDatabindingUsageBinding.setViewModel(viewModel);
        //只会输出end，因为LiveData 确保了每个生命周期事件内只会通知一次观察者，尽量减少UI刷新
        // value=end时由于liveData未发生变化，观察者不会再次被触发
        mutableLiveData.setValue("end");
        //MutableLiveData观察数据变化
        viewModel.getMutableLiveData().observe(this, text -> {
            // 观察 LiveData 并更新 UI
            if (text != null) {
                System.out.println("MyBindingConversion observe: "+ text);
            }
        });

    }

    public class Presenter{
        //不带参数的点击事件
        public void clickChangeText(View view)
        {
            //修改text值
            activityDatabindingUsageBinding.tvInformationText.setText("修改text值");
            activityDatabindingUsageBinding.setHasChanged(true);
        }

        //带参数的点击事件
        public void clickChangeTextWithParameter(boolean hasChanged)
        {
            //判断是否修改了值
            String text = null;
            if(hasChanged)
            {
                text = "修改text值成功";
            }else {
                text = "修改text值失败";
            }
            Toast.makeText(DataBindingActivity.this,text,Toast.LENGTH_SHORT).show();
        }


        //单向绑定
        //BaseObservable:修改name，age自动+1并更新所有字段
        public void baseObservableChangeName()
        {
            person1.setName("新的name");
            person1.setAge(20);
        }
        //修改age，只更新age
        public void baseObservableChangeAge()
        {
            person1.setAge(21);
            person1.setId("新的ageID");
        }
        //修改id，不自动更新
        public void baseObservableChangeId()
        {
            person1.setId("新的ID");
        }

        //observableField:修改name或Age自动更新
        public void observableFieldNameAge(ObservablePerson person2)
        {
            person2.name.set("新的observableField name");
            person2.age.set(20);
        }

        //修改id自动更新
        public void observableLongId(ObservablePerson person2)
        {

            person2.id.set(12312312312312L);
        }

        //双向绑定：修改content的值
        public void changeContent()
        {
            content.set("改变content，EditText中也改变");
        }

    }
}
