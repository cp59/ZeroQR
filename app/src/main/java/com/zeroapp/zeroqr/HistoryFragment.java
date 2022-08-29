package com.zeroapp.zeroqr;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryFragment extends Fragment {
    private ListView historyListView;
    private List<Map<String, String>> listArray;
    private static final String DataBaseName = "HistoryDataBase";
    private static final int DataBaseVersion = 1;
    private static final String DataBaseTable = "History";
    private static SQLiteDatabase db;
    private HistorySQLDataBaseHelper historySQLDataBaseHelper;
    private List contentArrayList,idArrayList;
    private TextView noHistoryTextView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        historyListView = view.findViewById(R.id.history_listview);
        noHistoryTextView = view.findViewById(R.id.no_history_textview);
        loadHistory();
    }
    public void clearHistory(){
        db.execSQL("DROP TABLE History");
        String SqlTable = "CREATE TABLE IF NOT EXISTS History (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "source text not null," +
                "type text not null," +
                "content text not null," +
                "parsedContent text not null," +
                "time text not null" +
                ")";
        db.execSQL(SqlTable);
        Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.clear_history_successfully), Snackbar.LENGTH_LONG);
        View snackbarLayout = snackbar.getView();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(0, 10, 0, 0);
        snackbarLayout.setLayoutParams(lp);
        snackbar.show();
        loadHistory();
    }
    public void loadHistory(){
        listArray = new ArrayList<>();
        contentArrayList = new ArrayList<>();
        idArrayList = new ArrayList<>();
        historySQLDataBaseHelper = new HistorySQLDataBaseHelper(getActivity(),DataBaseName,null,DataBaseVersion,DataBaseTable);
        db = historySQLDataBaseHelper.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + DataBaseTable,null);
        c.moveToFirst();
        for(int i=0;i<c.getCount();i++){
            contentArrayList.add(c.getString(3));
            idArrayList.add(c.getInt(0));
            HashMap<String, String> listItem = new HashMap<>();
            listItem.put("from", c.getString(1));
            listItem.put("type", c.getString(2));
            listItem.put("content", c.getString(4));
            listItem.put("time", c.getString(5));
            listArray.add(listItem);
            c.moveToNext();
        }
        if (listArray.isEmpty()){
            historyListView.setVisibility(View.INVISIBLE);
            noHistoryTextView.setVisibility(View.VISIBLE);
        } else {
            Collections.reverse(listArray);
            Collections.reverse(contentArrayList);
            Collections.reverse(idArrayList);
            SimpleAdapter simpleAdapter = new SimpleAdapter(getActivity(), listArray,
                    R.layout.history_result_list_item,
                    new String[]{"from","type", "content", "time"},
                    new int[]{R.id.text1, R.id.text2, R.id.text3, R.id.text4});
            historyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getActivity(), ResultActivity.class);
                    intent.putExtra("CONTENT", (String) contentArrayList.get(position));
                    intent.putExtra("saveToHistory",false);
                    startActivity(intent);
                }
            });
            historyListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                   @Override
                   public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                       HashMap<String, String> itemData = (HashMap<String, String>) listArray.get(i);
                       String[] listItems = {};
                       listItems = new String[]{getString(R.string.delete)};
                       AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                       builder.setItems(listItems, new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialogInterface, int which) {
                               if (which == 0) {
                                   db.execSQL("DELETE FROM " + DataBaseTable + " WHERE _id = " + idArrayList.get(i));
                                   loadHistory();
                               }
                           }
                       });
                       AlertDialog dialog = builder.create();
                       dialog.show();
                       return true;
                   }
            });
            historyListView.setAdapter(simpleAdapter);
        }
    }
}