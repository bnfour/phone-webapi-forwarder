package net.bnfour.phone2web2tg_forwarder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class ListEntryAdapter extends RecyclerView.Adapter<ListEntryAdapter.ViewHolder> {


    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public Button deleteButton;

        public View layout;

        public ViewHolder(View view) {
            super(view);
            layout = view;

            textView = view.findViewById(R.id.senderTextView);
            deleteButton = view.findViewById(R.id.deleteButton);
        }
    }

    private ArrayList<String> _entries;
    // context is necessary for alert dialogs
    private Context _context;

    public ListEntryAdapter(ArrayList<String> entries, Context context) {
        _entries = entries;
        _context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.listitemlayout, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return  holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.textView.setText(_entries.get(position));
        // this is :(
        holder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(_context);
                builder.setTitle(R.string.edit_entry);
                builder.setMessage(R.string.format_hint);

                final EditText input = new EditText(_context);
                input.setText(_entries.get(position));

                builder.setView(input);
                // ok button updates the list
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        _entries.set(position, input.getEditableText().toString());
                        notifyDataSetChanged();
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
        // making delete button work
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _entries.remove(position);
                notifyItemRemoved(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return _entries.size();
    }
}
