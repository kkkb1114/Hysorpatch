package kkkb1114.sampleproject.hysorpatch;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DataActivity extends AppCompatActivity {

    RecyclerView rv;
    DataAdapter dataAdapter;
    Context context;
    ArrayList<String> ad = new ArrayList<>();
    ArrayList<String> af = new ArrayList<>();
    SQLiteDatabase sqlDB;
    Cursor cursor;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        rv = (RecyclerView) findViewById(R.id.rv_list);
        rv.setVisibility(View.VISIBLE);

        sqlDB = TestService.dbHelper.getReadableDatabase();
        cursor = sqlDB.rawQuery("SELECT * FROM DATA; ", null);
        while (cursor.moveToNext()) {
            ad.add(cursor.getString(0));
            af.add("battery : "+cursor.getString(1)+" temperature : " + cursor.getString(2) +" humidity : "+cursor.getString(3));
        }

        dataAdapter = new DataAdapter(ad, af, context);
        rv.setLayoutManager(new LinearLayoutManager(context));
        rv.setAdapter(dataAdapter);
        rv.invalidate();
    }
}
