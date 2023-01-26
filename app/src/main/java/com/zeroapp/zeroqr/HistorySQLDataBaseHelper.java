package com.zeroapp.zeroqr;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;


public class HistorySQLDataBaseHelper extends SQLiteOpenHelper {

    private static final String DataBaseName = "HistoryDataBase";
    private static final int DataBaseVersion = 1;

    public HistorySQLDataBaseHelper(@Nullable Context context) {
        super(context, DataBaseName, null, DataBaseVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String SqlTable = "CREATE TABLE IF NOT EXISTS History (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "source text not null," +
                "type text not null," +
                "content text not null," +
                "parsedContent text not null," +
                "time text not null" +
                ")";
        sqLiteDatabase.execSQL(SqlTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        final String SQL = "DROP TABLE History";
        sqLiteDatabase.execSQL(SQL);
    }
}