package net.bnfour.phone2web2tg_forwarder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Arrays;

public class ListEditActivity extends Activity {

    private RecyclerView.Adapter _adapter;

    private ArrayList<String> _entries;
    private SharedPreferences _preferences;

    private final static String key = "filter_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listeditlayout);

        RecyclerView _recyclerView = findViewById(R.id.recycleview);
        _recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager _layoutManager = new LinearLayoutManager(this);
        _recyclerView.setLayoutManager(_layoutManager);

        _preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String[] entriesAsArray = _preferences.getString(key, "").split(";");
        // this is awkward implementation of returning an empty list on empty string
        // "" split by ";" is still one "", but we want an empty list in this case
        if (entriesAsArray.length > 1 || !entriesAsArray[0].equals("")) {
            _entries = new ArrayList<>(Arrays.asList(entriesAsArray));
        } else {
            _entries = new ArrayList<>();
        }

        _adapter = new ListEntryAdapter(_entries, ListEditActivity.this);
        _recyclerView.setAdapter(_adapter);

        Button addButton = findViewById(R.id.AddButton);
        // copy-paste programming is bad
        // this was taken from adapter class and slightly modified
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ListEditActivity.this);
                builder.setTitle(R.string.edit_entry);
                builder.setMessage(R.string.format_hint);

                final EditText input = new EditText(ListEditActivity.this);

                builder.setView(input);
                // ok button updates the list
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        _entries.add(input.getEditableText().toString());
                        _adapter.notifyDataSetChanged();
                        dialogInterface.dismiss();
                    }
                });
                // cancel button does nothing
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

                builder.show();
            }
        });
        // also a copy-paste, this is getting out of hand
        Button clearButton = findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ListEditActivity.this);
                builder.setTitle(R.string.confirm_title);
                builder.setMessage(R.string.confirm_text);

                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        _entries.clear();
                        _adapter.notifyDataSetChanged();
                        dialogInterface.dismiss();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

                builder.show();
            }
        });
    }


    @Override
    protected void onStop() {
        super.onStop();
        // also writes updated list to shared preferences
        _preferences.edit().putString(key, TextUtils.join(";", _entries)).commit();
    }
}
