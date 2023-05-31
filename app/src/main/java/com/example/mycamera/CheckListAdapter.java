package com.example.mycamera;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CheckListAdapter extends RecyclerView.Adapter<CheckListAdapter.ViewHolder> {

    private String[] itemList;
    private SparseBooleanArray checkedItems;

    public CheckListAdapter(String[] itemList) {
        this.itemList = itemList;
        checkedItems = new SparseBooleanArray();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.checklist_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String item = itemList[position];
        holder.textViewItem.setText(item);
        holder.checkBoxItem.setChecked(checkedItems.get(position));

        holder.checkBoxItem.setOnCheckedChangeListener(null); // Remove previous listener to avoid conflicts

        holder.checkBoxItem.setOnClickListener(v -> {
            CheckBox checkBox = (CheckBox) v;
            boolean isChecked = checkBox.isChecked();
            checkedItems.put(position, isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return itemList.length;
    }

    public int[] getSelectedItemsArray() {
        int[] selectedItems = new int[itemList.length];
        for (int i = 0; i < itemList.length; i++) {
            selectedItems[i] = checkedItems.get(i) ? 1 : 0;
        }
        return selectedItems;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBoxItem;
        TextView textViewItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBoxItem = itemView.findViewById(R.id.checkBoxItem);
            textViewItem = itemView.findViewById(R.id.textViewItem);
        }
    }
}
