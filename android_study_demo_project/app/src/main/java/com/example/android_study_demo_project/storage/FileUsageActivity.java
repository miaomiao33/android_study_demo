package com.example.android_study_demo_project.storage;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android_study_demo_project.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class FileUsageActivity extends AppCompatActivity {
    Button saveButton;
    Button readButton;
    EditText editText;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_usage);
        saveButton = (Button) findViewById(R.id.bt_file_save);
        readButton = (Button) findViewById(R.id.bt_file_read);
        editText = (EditText) findViewById(R.id.et_saveInfo);

        saveButton.setOnClickListener(new FileUsageClick());
        readButton.setOnClickListener(new FileUsageClick());
    }

    public class FileUsageClick implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            switch (view.getId())
            {
                case R.id.bt_file_save:
                   String textInfo = editText.getText().toString();
                   if(textInfo.isEmpty())
                   {
                       Toast.makeText(FileUsageActivity.this,
                               "请输入信息",Toast.LENGTH_SHORT).show();
                   }else
                   {
                       saveFileData(textInfo);
                   }
                    break;
                case R.id.bt_file_read:
                    String data = readFileData();
                    if(data.isEmpty())
                    {
                        data = "未保存过内容";
                    }else
                    {
                        editText.setText(data);
                        editText.setSelection(data.length());
                    }
                    break;
            }
        }
    }

    //保存文件内容
    void saveFileData(String text)
    {
        FileOutputStream out = null;
        BufferedWriter writer = null;
        try {
            out = openFileOutput("data", Context.MODE_PRIVATE);//私有数据，会覆盖原有数据
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(text);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(writer != null)
            {
                try {
                    writer.flush();
                    writer.close();
                    Toast.makeText(FileUsageActivity.this,
                            "写入成功",Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //读取文件内容
    String readFileData()
    {
        FileInputStream in = null;
        BufferedReader reader = null;
        StringBuilder content = new StringBuilder();
        try {
             in = openFileInput("data");
             reader = new BufferedReader(new InputStreamReader(in));
             String line = "";
             while((line = reader.readLine()) != null)
             {
                 content.append(line);
             }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(reader != null)
            {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return content.toString();
    }
}
