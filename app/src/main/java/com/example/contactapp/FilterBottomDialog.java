package com.example.contactapp;

import android.content.Context;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.contactapp.databinding.DialogGroupFilterBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

public class FilterBottomDialog extends BottomSheetDialog {
    private final List<String> groupList;
    private final String currentGroup;
    private final GroupSelectedListener onGroupSelected;
    private DialogGroupFilterBinding binding;

    public FilterBottomDialog(Context context, List<String> groupList, String currentGroup, GroupSelectedListener onGroupSelected) {
        super(context);
        this.groupList = groupList;
        this.currentGroup = currentGroup;
        this.onGroupSelected = onGroupSelected;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogGroupFilterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.recyclerViewGroups.setLayoutManager(new LinearLayoutManager(getContext()));
        GroupAdapter groupAdapter = new GroupAdapter(groupList, false, currentGroup, (group, action) -> {
            if ("select".equals(action)) {
                onGroupSelected.onGroupSelected(group);
                dismiss();
            }
        });
        binding.recyclerViewGroups.setAdapter(groupAdapter);
    }

    public interface GroupSelectedListener {
        void onGroupSelected(String group);
    }
}
