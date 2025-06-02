package com.example.android_study_demo_project.androidCleanArchitecture.CompositionRoot;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_study_demo_project.R;
import com.example.android_study_demo_project.androidCleanArchitecture.Entities.User;
import com.example.android_study_demo_project.androidCleanArchitecture.Gateway.UserGateway;
import com.example.android_study_demo_project.androidCleanArchitecture.Gateway.UserGatewayImpl;
import com.example.android_study_demo_project.androidCleanArchitecture.UseCases_Interactor.GetUserUseCase;
import com.example.android_study_demo_project.androidCleanArchitecture.UseCases_Interactor.UserInteractor;
import com.example.android_study_demo_project.androidCleanArchitecture.presentation.UserPresenter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

//在主应用程序入口点中配置依赖关系。
public class CleanArchitectureActivity extends AppCompatActivity {
    private Button addButton;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clean_architecture);
        addButton = (Button) findViewById(R.id.bt_add_user);
        listView = (ListView) findViewById(R.id.lv_user_list_view);

        //Gateway层
        UserGateway userGateway = new UserGatewayImpl();//接口、数据库操作
        //Interactor层
        GetUserUseCase userInteractor = new UserInteractor(userGateway);//业务操作，获取user，添加user

        UserListViewAdapter adapter = new UserListViewAdapter(userInteractor,
                CleanArchitectureActivity.this);
        // presentation层
        UserPresenter userPresenter = new UserPresenter(adapter,userInteractor);//ui点击等处理

        listView.setAdapter(adapter);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance(); // 获取当前时间实例
                SimpleDateFormat dateFormat = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault()); // 定义时间格式
                User user = new User(dateFormat.format(calendar.getTime()),
                        "John Doe",
                        "john.doe@example.com");
                userPresenter.onAddUserButtonClicked(user,CleanArchitectureActivity.this);
            }
        });
    }
}