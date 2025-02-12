package com.example.android_study_demo_project.storage.ROOM;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

//DB引擎
public class DBEngine {
    private StudentDao studentDao;
    public DBEngine(Context context)
    {
        StudentDatabase studentDatabase = StudentDatabase.getInstance(context);
        studentDao = studentDatabase.getStudentDao();
    }

    //dao的增删改查
    //增
    public void insertStudents(Student ... students)
    {
        new InsertAsyncTask(studentDao).execute(students);
    }

    //删（有条件）
    public void deleteStudents(Student ... students)
    {
        new DeleteAsyncTask(studentDao).execute(students);
    }
    //删（全部删除）
    public void deleteAllStudents(Student ... students)
    {
        new DeleteAllAsyncTask(studentDao).execute();
    }

    //改
    public void updateStudents(Student ... students)
    {
        new UpdateAsyncTask(studentDao).execute(students);
    }

    //查（有条件）
    public void queryStudents(String ... studentsName)
    {
        new QueryAsyncTask(studentDao).execute(studentsName);
    }
    //查（查询全部）
    public void queryAllStudents(Student ... students)
    {
        new QueryAllAsyncTask(studentDao).execute();
    }

    //异步查询
    private static class InsertAsyncTask extends android.os.AsyncTask<Student,Void,Void>
    {
        private StudentDao dao;
        public InsertAsyncTask(StudentDao studentDao) {
            dao = studentDao;
        }

        @Override
        protected Void doInBackground(Student... students) {
            dao.insertStudents(students);
            return null;
        }
    }

    //异步删除（有条件）
    private static class DeleteAsyncTask extends android.os.AsyncTask<Student,Void,Void>
    {
        private StudentDao dao;
        public DeleteAsyncTask(StudentDao studentDao) {
            dao = studentDao;
        }

        @Override
        protected Void doInBackground(Student... students) {
            dao.deleteStudents(students);
            return null;
        }
    }

    //异步删除（全部删除）
    private static class DeleteAllAsyncTask extends AsyncTask<Void,Void,Void>
    {
        private StudentDao dao;
        public DeleteAllAsyncTask(StudentDao studentDao) {
            dao = studentDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            dao.deleteAllStudents();
            return null;
        }
    }

    //异步更新
    private static class UpdateAsyncTask extends android.os.AsyncTask<Student,Void,Void>
    {
        private StudentDao dao;
        public UpdateAsyncTask(StudentDao studentDao) {
            dao = studentDao;
        }

        @Override
        protected Void doInBackground(Student... students) {
            dao.updateStudents(students);
            return null;
        }
    }

    //异步查找（有条件）
    private static class QueryAsyncTask extends android.os.AsyncTask<String,Void,Void>
    {
        private StudentDao dao;
        public QueryAsyncTask(StudentDao studentDao) {
            dao = studentDao;
        }

        @Override
        protected Void doInBackground(String... names) {
            List<Student> studentResult = new ArrayList<>();
            for (String name:names) {
                studentResult.addAll(dao.getStudents(name));
            }
            //遍历所有的查询结果
            for (Student student:studentResult) {
                Log.i("ROOMQuery:",student.toString());
            }
            return null;
        }
    }
    //异步查找（查找全部）
    private static class QueryAllAsyncTask extends android.os.AsyncTask<Void,Void,Void>
    {
        private StudentDao dao;
        public QueryAllAsyncTask(StudentDao studentDao) {
            dao = studentDao;
        }

        @Override
        protected Void doInBackground(Void ... voids) {
            List<Student> allStudent =  dao.getALLStudents();
            //遍历所有的查询结果
            for (Student student:allStudent) {
                Log.i("ROOMQuery:",student.toString());
            }
            return null;
        }
    }
}
