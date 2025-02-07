package com.example.android_study_demo_project.storage.SQLite;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.android_study_demo_project.R;

import java.util.List;

public class BookResultAdapter extends ArrayAdapter<BookModel> {

    private int resourceId;
    public BookResultAdapter(@NonNull Context context, int resource, @NonNull List<BookModel> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        BookModel bookModel = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
        TextView bookId = (TextView)view.findViewById(R.id.tv_bookId);
        TextView bookName = (TextView)view.findViewById(R.id.tv_bookName);
        TextView bookAuthor = (TextView)view.findViewById(R.id.tv_bookAuthor);
        TextView bookPages = (TextView)view.findViewById(R.id.tv_bookPages);
        TextView bookPrice = (TextView)view.findViewById(R.id.tv_bookPrice);

        bookId.setText(bookModel.getId());
        bookName.setText(bookModel.getName());
        bookAuthor.setText(bookModel.getAuthor());
        //最好用String.format，不然容易报错
        bookPages.setText(String.format("%d",bookModel.getPages()));
        bookPrice.setText(String.format("%.2f",bookModel.getPrice()));

        return view;
    }
}
