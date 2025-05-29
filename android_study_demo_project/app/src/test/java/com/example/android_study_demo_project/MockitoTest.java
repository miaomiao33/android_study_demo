package com.example.android_study_demo_project;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class MockitoTest {
    @Mock
    List<String> mockedList;

    @Test
    public void mockitoUnitTest()
    {
        //创建mock对象
        List mockedList2 = Mockito.mock(List.class);

        //定义行为
        when(mockedList.size()).thenReturn(100);//调用size返回100
        when(mockedList2.get(0)).thenReturn("first");
        when(mockedList2.get(1)).thenThrow(new RuntimeException());//抛出异常

        //验证
        System.out.println(mockedList.size());
        System.out.println(mockedList2.get(0));
//        System.out.println(mockedList2.get(1));

        //验证交互
        mockedList2.add("one");
        //验证mockedList2.add("one")被调用一次
        verify(mockedList2).add("one");
        //验证mockedList.clear()从未被调用
        verify(mockedList,never()).clear();
        //mockedList.add("two")至少被调用两次
//        verify(mockedList,atLeast(2)).add("two");

    }

    @Test
    public void mockitoAdvancedUsage()
    {
        //参数匹配器
        //任何整数参数
        when(mockedList.get(anyInt())).thenReturn("element");
        //特定类型的参数（此处为String）
        when(mockedList.contains(anyString())).thenReturn(true);
        //自定义匹配器,argumentString表示加入mockedList的字符串，长度超过5就返回true，
        // 注意mockedList要是List<String>
        when(mockedList.add(argThat(argumentString -> argumentString.length() > 5))).thenReturn(true);
        //验证
        System.out.println(mockedList.get(10086));//显示element
        System.out.println(mockedList.contains("123"));//显示true
        System.out.println(mockedList.add("10086"));//小于5返回false


        //验证调用顺序
        InOrder inOrder = Mockito.inOrder(mockedList);
        //加入顺序不对，或者是漏加都会导致报错
        mockedList.add("second");
        mockedList.add("first");
        inOrder.verify(mockedList).add("first");
        inOrder.verify(mockedList).add("second");

        //部分Mock(Spy)
        List<String> realList = new ArrayList<>();
        List<String> spyList = spy(realList);//模拟realList

        //调用真实方法
        spyList.add("real");

        //模拟特定方法
        doReturn(100).when(spyList).size();
        System.out.println(spyList.size());//显示100
    }


}
