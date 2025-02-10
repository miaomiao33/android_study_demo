package com.example.android_study_demo_project.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android_study_demo_project.R;
import com.example.android_study_demo_project.storage.SQLite.BookModel;
import com.example.android_study_demo_project.storage.SQLite.BookResultAdapter;
import com.example.android_study_demo_project.storage.SQLite.MyDataBaseHelper;

import java.util.ArrayList;
import java.util.List;

public class SQLiteUsageActivity extends AppCompatActivity {

    MyDataBaseHelper myDataBaseHelper;
    EditText nameEditText;
    Button createDataBaseButton;
    Button updateDataBaseButton;
    Button searchByNameButton;
    Button insertDataButton;
    Button updateDataButton;
    Button deleteDataByNameButton;
    private List<BookModel> bookModelList = new ArrayList<>();
    BookResultAdapter bookResultAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sqlite_usage);

        //初始化数据库
        initDataBase();

        nameEditText = (EditText) findViewById(R.id.et_bookName);
        createDataBaseButton = (Button) findViewById(R.id.bt_createDataBase);
        updateDataBaseButton = (Button) findViewById(R.id.bt_updateDataBase);
        searchByNameButton = (Button) findViewById(R.id.bt_searchByName);
        insertDataButton = (Button) findViewById(R.id.bt_insertData);
        updateDataButton = (Button) findViewById(R.id.bt_updateData);
        deleteDataByNameButton = (Button) findViewById(R.id.bt_deleteDataByName);

        createDataBaseButton.setOnClickListener(new SQLiteUsageClick());
        updateDataBaseButton.setOnClickListener(new SQLiteUsageClick());
        searchByNameButton.setOnClickListener(new SQLiteUsageClick());
        insertDataButton.setOnClickListener(new SQLiteUsageClick());
        updateDataButton.setOnClickListener(new SQLiteUsageClick());
        deleteDataByNameButton.setOnClickListener(new SQLiteUsageClick());

        bookResultAdapter = new BookResultAdapter(SQLiteUsageActivity.this,
                R.layout.activity_sqlite_usage_item,bookModelList);
        ListView listView = (ListView)findViewById(R.id.lv_book_result);
        listView.setAdapter(bookResultAdapter);
    }

    //初始化数据库
    private void initDataBase()
    {
//        myDataBaseHelper = new MyDataBaseHelper(SQLiteUsageActivity.this,
//                "BookStore.dp", null, 1);
//        myDataBaseHelper.getWritableDatabase();

        //单例模式写法
        myDataBaseHelper = (MyDataBaseHelper) MyDataBaseHelper.getInstance(SQLiteUsageActivity.this);
        myDataBaseHelper.getWritableDatabase();
    }

    private class SQLiteUsageClick implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.bt_createDataBase:
                    createDataBase();
                    break;
                case R.id.bt_updateDataBase:
                    updateDataBase();
                    break;
                case R.id.bt_searchByName:
                    searchByName(nameEditText.getText().toString());
                    break;
                case R.id.bt_insertData:
                    insertData(nameEditText.getText().toString());
                    break;
                case R.id.bt_updateData:
                    updateData(nameEditText.getText().toString());
                    break;
                case R.id.bt_deleteDataByName:
                    deleteDataByName(nameEditText.getText().toString());
                    break;
            }
        }
    }

    //创建数据库
    private void createDataBase()
    {
        myDataBaseHelper = new MyDataBaseHelper(SQLiteUsageActivity.this,
                "BookStore.dp", null, 1);
        myDataBaseHelper.getWritableDatabase();
        myDataBaseHelper.close();
    }
    //更新数据库版本
    private void updateDataBase()
    {
        myDataBaseHelper = new MyDataBaseHelper(SQLiteUsageActivity.this,
                "BookStore.dp", null, 2);
        myDataBaseHelper.getWritableDatabase();
        myDataBaseHelper.close();
        Toast.makeText(SQLiteUsageActivity.this,"更新数据库成功",Toast.LENGTH_SHORT).show();
    }

    //根据名称查询数据
    private void searchByName(String name)
    {
        SQLiteDatabase db = myDataBaseHelper.getReadableDatabase();
        if(db.isOpen())
        {
            //name为空就是查询所有，没有就是根据name查询
            String selection = (name==null ||name.isEmpty()) ? null:"name=?";
            String[] selectionArgs = (name==null ||name.isEmpty()) ? null:new String[]{name};
            Cursor cursor = db.query("Book",null,
                    selection,selectionArgs,null,null,null);
            //循环打印出来
            bookModelList.clear();
            if(cursor.moveToFirst())
            {
                do{
                    BookModel bookModel = new BookModel();
                    bookModel.setId(cursor.getString(cursor.getColumnIndex("id")));
                    bookModel.setAuthor(cursor.getString(cursor.getColumnIndex("author")));//作者
                    bookModel.setPrice(cursor.getFloat(cursor.getColumnIndex("price")));//价格
                    bookModel.setPages(cursor.getInt(cursor.getColumnIndex("pages")));//页数
                    bookModel.setName(cursor.getString(cursor.getColumnIndex("name")));//书名
                    bookModelList.add(bookModel);
                }while (cursor.moveToNext());
                bookResultAdapter.notifyDataSetChanged();//刷新ListView
            }else
            {
                Toast.makeText(SQLiteUsageActivity.this,"未查询到对应名称书籍",Toast.LENGTH_SHORT).show();
            }
            cursor.close();
            myDataBaseHelper.close();
        }
    }

    //插入数据
    private void insertData(String name)
    {
        if(name.isEmpty())
        {
            Toast.makeText(SQLiteUsageActivity.this,"请输入书名",Toast.LENGTH_SHORT).show();
        }else
        {
            SQLiteDatabase db = myDataBaseHelper.getWritableDatabase();
            if(db.isOpen())
            {
                ContentValues values = new ContentValues();
                //组装插入数据
                values.put("name",name);
                values.put("author","Dan Brown");
                values.put("price",16.962);
                values.put("pages",454);
                //插入
                db.insert("Book",null,values);
                myDataBaseHelper.close();
                Toast.makeText(SQLiteUsageActivity.this,"插入成功",Toast.LENGTH_SHORT).show();
                searchByName(null);//刷新ListView
            }
        }
    }
    //更新数据
    private void updateData(String name)
    {
        if(name.isEmpty())
        {
            Toast.makeText(SQLiteUsageActivity.this,"请输入书名",Toast.LENGTH_SHORT).show();
        }else
        {
            SQLiteDatabase db = myDataBaseHelper.getWritableDatabase();
            if(db.isOpen())
            {
                ContentValues values = new ContentValues();
                //组装插入数据
                values.put("price",10.99);
                db.update("Book",values,"name=?",new String[]{name});
                db.close();
                Toast.makeText(SQLiteUsageActivity.this,"更新成功",Toast.LENGTH_SHORT).show();
                searchByName(null);//刷新ListView
            }
        }
    }
    //删除数据
    private void deleteDataByName(String name)
    {
        if(name.isEmpty())
        {
            Toast.makeText(SQLiteUsageActivity.this,"请输入书名",Toast.LENGTH_SHORT).show();
        }else
        {
            SQLiteDatabase db = myDataBaseHelper.getWritableDatabase();
            if(db.isOpen())
            {
                db.delete("Book","name=?",new String[]{name});
                db.close();
                Toast.makeText(SQLiteUsageActivity.this,"删除成功",Toast.LENGTH_SHORT).show();
                searchByName(null);//刷新ListView
            }
        }
    }
}
