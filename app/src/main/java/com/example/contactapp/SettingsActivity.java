package com.example.contactapp;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
//import androidx.core.view.setPadding;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.example.contactapp.databinding.ActivitySettingsBinding;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import android.view.View;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;

    private GroupAdapter groupAdapter;
    private List<String> groupList = new ArrayList<>();
    private String group;
    private boolean isListLayout = true;
    private boolean isDarkTheme = true;
    private  GroupAdapter.OnGroupActionListener action;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 加载配置信息
        loadSettings();

        // 初始化view
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("设置");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.radioLightTheme.setChecked(!isDarkTheme);
        binding.radioDarkTheme.setChecked(isDarkTheme);
        binding.radioListLayout.setChecked(isListLayout);
        binding.radioCardLayout.setChecked(!isListLayout);

        // 更改group
        groupAdapter = new GroupAdapter(groupList, true, group, (group,action) -> {
            switch (action) {
                case "edit":
                    showEditGroupDialog(group);
                    break;
                case "delete":
                    deleteGroup(group);
                    break;
            }
//            loadSettings();
        });
        binding.recyclerViewGroups.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewGroups.setAdapter(groupAdapter);

        binding.btnAddGroup.setOnClickListener(v -> showAddGroupDialog());

        // 更改显示模式
        binding.radioGroupLayout.setOnCheckedChangeListener((group, checkedId) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (checkedId == R.id.radio_list_layout) {
                editor.putBoolean("isListLayout", true);
            } else if (checkedId == R.id.radio_card_layout) {
                editor.putBoolean("isListLayout", false);
            }
            editor.apply();
        });

        // 更改主题
        binding.radioGroupTheme.setOnCheckedChangeListener((group, checkedId) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            boolean useDarkTheme = checkedId == R.id.radio_dark_theme;
            editor.putBoolean("isDarkTheme", useDarkTheme);
            editor.apply();
            recreate();
        });
    }

    private void loadSettings() {
        sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        isDarkTheme = sharedPreferences.getBoolean("isDarkTheme", false);
        loadTheme(isDarkTheme);
        isListLayout = sharedPreferences.getBoolean("isListLayout", true);

        Set<String> defaultGroupSet = new HashSet<>();
        defaultGroupSet.add("全部");
        Set<String> groupSet = sharedPreferences.getStringSet("groupList", Collections.unmodifiableSet(defaultGroupSet));

        groupList = new ArrayList<>(groupSet);
        Collections.sort(groupList);
    }

    private void loadTheme(boolean isDarkTheme) {
        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void saveGroups() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> groupSet = new HashSet<>(groupList);
        editor.putStringSet("groupList", Collections.unmodifiableSet(groupSet));
        editor.apply();
        setResult(RESULT_OK);
    }

    private void showAddGroupDialog() {
        EditText input = new EditText(this);
        input.setHint("输入分组名称");
        input.setPadding(100, 0, 100, 0);
        input.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(input);
        dialog.setOnDismissListener(dialog1 -> {
            String groupName = input.getText().toString().trim();
            int loc=0;
            for (String group:groupList){
                if(groupName.compareTo(group)<0)
                    break;
                loc++;
            }
            if (!groupName.isEmpty() && !groupList.contains(groupName)) {
                groupList.add(groupList.size(), groupName);
                groupAdapter.notifyItemInserted(groupList.size() - 1);
                saveGroups();
            } else {
                Toast.makeText(this, "不允许的分组名", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    private void showEditGroupDialog(String group) {
        EditText input = new EditText(this);
        input.setHint("输入分组名称");
        input.setPadding(100, 0, 100, 0);
        input.setText(group);
        input.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(input);
        dialog.setOnDismissListener(dialog1 -> {
            String newGroupName = input.getText().toString().trim();

            int position = groupList.indexOf(group);
            if (!newGroupName.isEmpty() && !groupList.contains(newGroupName) && position != -1) {
                groupList.set(position, newGroupName);
                groupAdapter.notifyItemChanged(position);
                saveGroups();

            } else {
                Toast.makeText(this, "不允许的分组名", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    private void deleteGroup(String group) {
        int position = groupList.indexOf(group);
        if (position != -1) {
            groupList.remove(position);
            groupAdapter.notifyItemRemoved(position);
            saveGroups();
            Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
        }
    }
}
