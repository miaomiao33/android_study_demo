package com.example.android_study_demo_project;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.bumptech.glide.util.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;

import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.android_study_demo_project.espressoUsage.EspressoUsageActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class EspressoUsageActivityTest {

    // 使用 @Rule 注解指定测试的 Activity 为 EspressoUsageActivity
    @Rule
    public ActivityScenarioRule<EspressoUsageActivity> activityRule =
            new ActivityScenarioRule<>(EspressoUsageActivity.class);

    EspressoUsageActivity espressoUsageActivity;

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
        onView(withId(R.id.tv_espresso_text)).check(matches(withText("item8")));
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
}