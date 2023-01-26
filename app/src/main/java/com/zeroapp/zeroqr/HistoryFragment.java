package com.zeroapp.zeroqr;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryFragment extends Fragment {
    private ListView historyListView;
    private static final String DataBaseName = "HistoryDataBase";
    private static final int DataBaseVersion = 1;
    private static final String DataBaseTable = "History";
    private static SQLiteDatabase db;
    private List contentArrayList,idArrayList;
    private TextView noHistoryTextView;

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
        MaterialToolbar topAppBar = view.findViewById(R.id.topAppBar);
        topAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_clear_history){
                new MaterialAlertDialogBuilder(getContext())
                        .setTitle(getString(R.string.clear_history_dialog_title))
                        .setPositiveButton(getString(android.R.string.yes), (dialogInterface, i) -> clearHistory())
                        .setNegativeButton(getString(android.R.string.cancel),null)
                        .show();

                return true;
            }
            return false;
        });
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
        List<Map<String, String>> listArray = new ArrayList<>();
        contentArrayList = new ArrayList<>();
        idArrayList = new ArrayList<>();
        HistorySQLDataBaseHelper historySQLDataBaseHelper = new HistorySQLDataBaseHelper(getActivity());
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
        c.close();
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
            historyListView.setOnItemClickListener((parent, view, position, id) -> {
                Intent intent = new Intent(getActivity(), ResultActivity.class);
                intent.putExtra("CONTENT", (String) contentArrayList.get(position));
                intent.putExtra("saveToHistory",false);
                startActivity(intent);
            });
            historyListView.setOnItemLongClickListener((adapterView, view, i, l) -> {
                String[] listItems;
                listItems = new String[]{getString(R.string.delete)};
                new MaterialAlertDialogBuilder(getContext())
                         .setItems(listItems, (dialogInterface, which) -> {
                             if (which == 0) {
                                 db.execSQL("DELETE FROM " + DataBaseTable + " WHERE _id = " + idArrayList.get(i));
                                 loadHistory();
                             }
                         })
                        .show();
                return true;
            });
            historyListView.setAdapter(simpleAdapter);
        }
    }
}