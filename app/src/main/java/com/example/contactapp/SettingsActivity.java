package com.example.contactapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.contactapp.databinding.ActivitySettingsBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {

    private static final int REQUEST_EXTERNAL_STORAGE_PERMISSION = 1;
    private ActivitySettingsBinding binding;

    private GroupAdapter groupAdapter;
    private List<String> groupList = new ArrayList<>();
    private String group;
    private boolean isListLayout = true;
    private boolean isDarkTheme = true;
    private GroupAdapter.OnGroupActionListener action;
    private SharedPreferences sharedPreferences;
    private ActivityResultLauncher<String[]> pickCsvFileLauncher;
    private ActivityResultLauncher<String> createCsvFileLauncher;
    private ContactDatabaseHelper dbHelper;
    private List<Contact> contactList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 加载配置信息
        loadSettings();

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
        // 初始化导入导出功能的 ActivityResultLauncher
        pickCsvFileLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri != null) {
                ContactImporter.importContactsFromUri(this, uri);
            }
        });

        createCsvFileLauncher = registerForActivityResult(new ActivityResultContracts.CreateDocument(), uri -> {
            if (uri != null) {
                ContactExporter.exportContactsToUri(this, uri);
            }
        });

        // 设置导入按钮点击事件
        binding.btnImportContacts.setOnClickListener(view -> {
            if (checkAndRequestPermissions()) {
                pickCsvFileLauncher.launch(new String[]{"*/*"}); // 允许所有文件类型选择
            } else {

                Toast.makeText(this, "没有存储权限，无法导入文件", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnExportContacts.setOnClickListener(view -> {
            if (checkAndRequestPermissions()) {
                createCsvFileLauncher.launch("contacts.csv");
            } else {
                Toast.makeText(this, "没有存储权限，无法导出文件", Toast.LENGTH_SHORT).show();
            }
        });

        // 更改group
        groupAdapter = new GroupAdapter(groupList, true, group, (group, action) -> {
            switch (action) {
                case "edit":
                    showEditGroupDialog(group);
                    break;
                case "delete":
                    deleteGroup(group);
                    break;
            }
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
            loadSettings();

        });
        super.onCreate(savedInstanceState);
    }

    private boolean checkAndRequestPermissions() {
        boolean hasPermission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            hasPermission = Environment.isExternalStorageManager();
            Log.d("SettingsActivity", "Checking permission for Android R+: " + hasPermission);
        } else {
            hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            Log.d("SettingsActivity", "Checking standard storage permissions: " + hasPermission);
        }
        if(!hasPermission)showExplanationDialog();
        return hasPermission;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限被授予，可以进行文件操作
            } else {
                showExplanationDialog();
//                Log.d("Settings","len "+grantResults.length+" "+PackageManager.PERMISSION_GRANTED+" "+grantResults[0]);
                // 权限被拒绝，需要提示用户或者进行其他操作
            }
        }
    }



    private void showExplanationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("存储权限被拒绝")
                .setMessage("您已拒绝存储权限。请在设置中启用它，以允许应用导入和导出联系人。")
                .setPositiveButton("打开设置", (dialogInterface, i) -> {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton("取消", (dialogInterface, i) -> dialogInterface.dismiss())
                .create()
                .show();
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
//        input.setPadding(100, 0, 100, 0);
        input.setHeight(200);
        input.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(input);
        dialog.setOnDismissListener(dialog1 -> {
            String groupName = input.getText().toString().trim();

            if (!groupName.isEmpty() && !groupList.contains(groupName)) {
                int insertIndex = Collections.binarySearch(groupList, groupName);
                if (insertIndex < 0) {
                    insertIndex = -insertIndex - 1;
                }
                groupList.add(insertIndex, groupName);
                groupAdapter.notifyItemInserted(insertIndex);
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
                dbHelper = new ContactDatabaseHelper(this);
                contactList = dbHelper.getAllContacts();
                for(Contact contact:contactList){
//                    Log.d("setting", String.valueOf(" "+contact.getGroup()==group));
                    if(Objects.equals(contact.getGroup(), group)){
                        Log.d("setting"," "+contact.getGroup()+" "+group);
                        contact.setGroup(newGroupName);
                        dbHelper.updateContact(contact);

                    }
                }
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