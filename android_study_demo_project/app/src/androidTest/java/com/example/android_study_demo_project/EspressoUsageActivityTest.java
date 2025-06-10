package com.example.android_study_demo_project;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasData;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import com.example.android_study_demo_project.espressoUsage.EspressoUsageActivity;
import com.example.android_study_demo_project.espressoUsage.IdlingDelayResources;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class EspressoUsageActivityTest {

    // 使用 @Rule 注解指定测试的 Activity 为 EspressoUsageActivity
    @Rule
    public ActivityScenarioRule<EspressoUsageActivity> activityRule =
            new ActivityScenarioRule<>(EspressoUsageActivity.class);
    //用于测试Intent跳转
    @Rule
    public IntentsTestRule<EspressoUsageActivity> intentsTestRule =
            new IntentsTestRule<>(EspressoUsageActivity.class);
    //跳过权限
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule
            .grant(Manifest.permission.READ_CONTACTS,//跳过联系人访问权限
                    Manifest.permission.CALL_PHONE);//跳过通话权限

    EspressoUsageActivity espressoUsageActivity;
    Activity currentActivity;

    IdlingDelayResources mIdlingResource;//延时测试

    @Before
    public void setUp() {
        //清除应用数据
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand("pm clear com.example.app");
        // 可以在这里进行测试前的初始化操作
        ActivityScenario<EspressoUsageActivity> espressoUsageActivityActivityScenario = activityRule.getScenario();
        // 使用 onActivity 方法获取 Activity 实例
        espressoUsageActivityActivityScenario.onActivity(activity -> {
            // 在这里对 Activity 进行操作或断言
            assertNotNull("Activity should not be null", activity);
            this.espressoUsageActivity = activity;
        });
        //延时测试网络加载情况
        mIdlingResource= (IdlingDelayResources) espressoUsageActivity.getIdlingResource();
        IdlingRegistry.getInstance().register(mIdlingResource);
    }

    @After
    public void tearDown() {
        IdlingRegistry.getInstance().unregister(mIdlingResource);
    }

    @Test
    public void listGoesOverTheFold()
    {
        TextView textView = (TextView) espressoUsageActivity.findViewById(R.id.tv_espresso_text);
        System.out.println("TextView----->"+textView.getText());
        //检查文字
        //不建议这样写
//        onView(withText("Hello World")).check(matches(isDisplayed()));
        //应该改成这样
        onView(withId(R.id.tv_espresso_text)).check(matches(withText("Hello World")));
    }

    @Test
    public void clickTest()
    {
        //点击按钮
        onView(withId(R.id.bt_change_espresso_text))
                .perform(click());
        //检测修改后的文字是否为Hello Espresso!
        onView(withId(R.id.tv_espresso_text)).check(matches(withText("Hello Espresso")));
    }

    @Test
    public void listViewTest()
    {
        // 点击指定的ListView项
        onData(allOf(is(instanceOf(String.class)),is("item8"))).perform(click());

        //检验点击了是否修改
        onView(withId(R.id.tv_espresso_text)).check(matches(withText("item8")));
    }

    @Test
    public void listViewCustomizedMatcherTest()
    {
        // 点击指定的ListView项
        //根据匹配器滑动到相应的位置
        onData(listViewMatcher("item8")) // 确保视图可见
                .perform(click()); // 执行点击操作

        //检验点击了是否修改
        onView(withId(R.id.tv_espresso_text)).check(matches(withText("点击了RecyclerView中：item8")));
    }

    public static Matcher<Object> listViewMatcher(final String expectedString){
        return new TypeSafeMatcher<Object>(){

            @Override
            protected boolean matchesSafely(Object item) {
                //用于定义匹配逻辑
                // 确保 item 是 String 类型并检查其值是否等于 expectedString
                return item instanceof String && item.equals(expectedString);
            }
            @Override
            public void describeTo(Description description) {
                //方法用于生成描述信息，便于调试和错误定位。
                description.appendText("测试item");
            }
        };
    }

    @Test
    public void testRecyclerView(){
        //定位到RecyclerView的第7位
        onView(ViewMatchers.withId(R.id.rv_espresso_recyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(7,click()));//position从0开始的
        //检验点击了是否修改
        onView(withId(R.id.tv_espresso_text)).check(matches(withText("点击了RecyclerView中：item8")));
    }

    //Intent跳转测试
    //测试去一个界面选号码然后打电话的动作:
    // app中的步骤是这样的，点击“获取号码”按钮，通过startActivityForResult去联系人页面选择一个号码返回保存到成员变量中，
    // 然后点击“打电话”按钮通过Intent打开系统打电话的界面
    @Test
    public void testPickIntent()
    {
        //当activity启动时创建一个将要返回的结果
        Intent resultData = new Intent();
        String phoneNumber = "123456789";
        resultData.putExtra("phoneNumber", phoneNumber);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);


//        //当intent发送到com.android.contacts时设置返回结果
        intending(toPackage("com.example.android_study_demo_project")).respondWith(result);

        //验证如果有相应的startActivityOnResult发生，就返回前面自己创建的结果。返回到onActivityResult中(检测跳系统的联系人列表比较麻烦暂未找到目标包等信息)
//        intending(IntentMatchers.hasComponent(ComponentNameMatchers.hasShortClassName(".ContactsActivity")))
//                .respondWith(result);

        //点击去联系人页面的按钮
        onView(withId(R.id.bt_goto_contacts)).perform(click());
        //点击打电话的按钮
        onView(withId(R.id.bt_call)).perform(click());
        //验证打电话的Intent是否发送
        intended(allOf(
                hasAction(Intent.ACTION_CALL),
                hasData("tel:123456789")
        ));
    }

    //测试打开系统相机获取图片的动作
    //动作：一个Button，一个ImageView，点击按钮拍照，拍照确定后将图片显示到ImageView上面
    @Test
    public void testTackPhoto()
    {
        //自定义一个拍照返回drawable图片的Intent
        Intent picIntent = new Intent();
        //把drawable放到bundle中传递
        Bundle bundle = new Bundle();
        Bitmap bitmap = BitmapFactory.decodeResource(intentsTestRule.getActivity().getResources(), R.mipmap.ic_launcher);
        bundle.putParcelable("data", bitmap);
        picIntent.putExtras(bundle);
        //等会返回的ActivityResult
        Instrumentation.ActivityResult result =
                new Instrumentation.ActivityResult(Activity.RESULT_OK, picIntent);
        //判断是否有包含ACTION_IMAGE_CAPTURE的Intent出现，出现就给它返回result
        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(result);

        //判断ImageView上没有显示drawable
        onView(withId(R.id.iv_espresso_take_photo)).check(matches(not(hasDrawable())));
        //点击拍照按钮去拍照界面
        onView(withId(R.id.bt_espresso_take_photo)).perform(click());
        //判断ImageView上显示了一个drawable
        onView(withId(R.id.iv_espresso_take_photo)).check(matches(hasDrawable()));
    }

    //判断一个ImageView上面是否有图片
    private BoundedMatcher<View, ImageView> hasDrawable(){

        return new BoundedMatcher<View,ImageView>(ImageView.class){

            @Override
            public void describeTo(Description description) {
                description.appendText("是否有drawable");
            }

            @Override
            protected boolean matchesSafely(ImageView item) {
                //判断imageview的图片不为空
                return item.getDrawable() != null;
            }
        };
    }

    //Espresso获取当前的Activity
    public Activity getActivityInstance(){
        try {
            intentsTestRule.runOnUiThread(new Runnable() {
                public void run() {
                    Collection<Activity> resumedActivities =
                            ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
                    for (Activity act: resumedActivities){
                        Log.d("Your current activity: ", act.getClass().getName());
                        currentActivity = act;
                        break;
                    }
                }
            });
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return currentActivity;
    }

    //测试网络加载
    @Test
    public void testInternetLoading()
    {
        //执行点击事件，进入耗时操作
        onView(withId(R.id.bt_espresso_test_internet_load)).perform(click());
        //3秒后自动进行验证
        onView(withId(R.id.tv_espresso_internet_text)).check(matches(withText("网络信息加载成功")));
    }
}