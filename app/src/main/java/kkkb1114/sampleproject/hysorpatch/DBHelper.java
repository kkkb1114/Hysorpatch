package kkkb1114.sampleproject.hysorpatch;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;


public class DBHelper extends SQLiteOpenHelper {

    public static DBHelper mInstance;
    public static SQLiteDatabase writableDatabase;
    public static SQLiteDatabase readableDataBase;

    public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);

        SQLiteDatabase db = getWritableDatabase();


        db.execSQL("CREATE TABLE IF NOT EXISTS DATA (" +
                "DateTime DATETIME PRIMARY KEY , " +
                "Battery TEXT, " +
                "Temperature TEXT , " +
                "Humidity TEXT);");



        db.close();
    }

    public static DBHelper getInstance(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        if (mInstance == null){
            Log.e("Bodytemp_DBHelper_getInstance2222222", "22222222");
            mInstance = new DBHelper(context, name, factory, version);
            writableDatabase = mInstance.getWritableDatabase();
            readableDataBase = mInstance.getReadableDatabase();
        }
        return mInstance;
    }

    /*
     * SQLiteOpenHelper의 onCreate()는 db.getWritableDatabase() 또는 db.getReadableDatabase()가
     * 실행될때 실행되기에 TABLE 생성 코드는 생성자에 넣었다.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

    }



    public void closeDBHelper(){
        Log.e("DBHelper_closeDBHelper", "11111111111");
        /*mInstance.close();
        writableDatabase.close();
        readableDataBase.close();*/
        mInstance = null;
        writableDatabase = null;
        readableDataBase = null;
    }
}