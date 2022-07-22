package com.example.android_study_demo_project;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "feng";
//   private ProgressBar progressBar;
//    private ProgressBar progressBar2;

  private  NotificationManager manager;
  private   Notification notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//      Button button =  findViewById(R.id.btn_ok);

      //按钮的点击事件
//      button.setOnClickListener(new View.OnClickListener() {
//          @Override
//          public void onClick(View view) {
//              Log.e(TAG, "onClick: " );
//          }
//      });

//      //长按事件
//        button.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                Log.e(TAG, "onLongClick: ");
//                return false;
//            }
//        });
//
//        //触摸事件，分为三种
//        button.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                Log.e(TAG, "onTouch: "+motionEvent.getAction());
//                return false;//如果返回为true就表示这个事件被消费了
//            }
//        });

        //将对应id的textview修改
//      TextView tv_one =  findViewById(R.id.tv_one);
//      tv_one.setText("liu");

//        获取输入的内容
//        Button  loginButton=findViewById(R.id.bt_login);
//
//       final EditText userInput = findViewById(R.id.ET_User);
//
//       loginButton.setOnClickListener(new View.OnClickListener() {
//           @Override
//           public void onClick(View view) {
//               Log.e(TAG, "用户名: " + userInput.getText().toString());
//           }
//       });

//        //loading进度条
//         progressBar = findViewById(R.id.pb);
//        progressBar2 = findViewById(R.id.pb2);

        //通知
       manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

       //版本判断，8.0以上的
       if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
       {
           NotificationChannel channel = new NotificationChannel("feng","测试通知",
                   NotificationManager.IMPORTANCE_HIGH);//importance重要程度
           //使用
           manager.createNotificationChannel(channel);
       }


        Intent intent = new Intent(this,NotificationActivity.class);
       PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0 );

         notification = new NotificationCompat.Builder(this, "feng")
                .setContentTitle("官方通知")//标题
                .setContentText("世界那么大，去走走吧")//内容
                .setSmallIcon(R.drawable.ic_person_black_24dp)//小图标
                 .setColor(Color.parseColor("#ff0000"))//小图标颜色
                 .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ceshi))//大图标
                 .setContentIntent(pendingIntent)//点击后的跳转意图
                 .setAutoCancel(true)//设计点击后自动取消
                .build();
    }

    public void sendNotification(View view) {
        manager.notify(123,notification);
    }

    public void cancleNotification(View view) {
        manager.cancel(123);//点击取消通知
    }
}
