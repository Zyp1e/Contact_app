package com.example.contactapp;

import android.content.Intent;
import android.graphics.Color;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.example.contactapp.databinding.ActivityContactDetailBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ContactDetailActivity extends AppCompatActivity {

    private ActivityContactDetailBinding binding;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private Contact contact;
    private List<String> groupList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 初始化 Spinner
        Set<String> groupSet = getSharedPreferences("settings", MODE_PRIVATE).getStringSet("groupList", null);
        if (groupSet != null) {
            groupList.addAll(groupSet);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, groupList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerGroup.setAdapter(adapter);

        // 获取传递过来的联系人数据
        loadContact();

        // 初始化 Toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 初始化 ActivityResultLauncher
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        binding.imgContactPhoto.setImageURI(selectedImage);
                        if (contact != null) {
                            contact.setPhotoUri(selectedImage.toString());
                        }
                    }
                }
        );

        // 图片点击事件
        binding.imgContactPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });

        // 保存按钮点击事件
        binding.btnSave.setOnClickListener(v -> saveContact());

        // 删除按钮点击事件
        binding.btnDelete.setOnClickListener(v -> deleteContact());
    }

    private void loadContact() {
        contact = getIntent().getParcelableExtra("contact");
        if (contact != null) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(contact.getName());
            }
            binding.etName.setText(contact.getName());
            binding.etNickname.setText(contact.getNickname());
            binding.etPhoneNumber.setText(contact.getPhoneNumber());
            binding.spinnerGroup.setSelection(groupList.indexOf(contact.getGroup()));
            if (contact.getPhotoUri() != null) {
                binding.imgContactPhoto.setImageURI(Uri.parse(contact.getPhotoUri()));
            } else {
                binding.imgContactPhoto.setImageBitmap(contact.createBitmapFromCharacter(
                        contact.getName().charAt(contact.getName().length() - 1),
                        500, Color.WHITE, 10));
            }
        } else {
            // 创建新联系人
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("新建联系人");
            }
            contact = new Contact(System.currentTimeMillis(), "", "", "", "", null);
        }
    }

    private void saveContact() {
        // 保存联系人逻辑（可以保存到数据库或其他持久化存储）
        if (contact != null) {
            contact.setName(binding.etName.getText().toString());
            contact.setNickname(binding.etNickname.getText().toString());
            contact.setPhoneNumber(binding.etPhoneNumber.getText().toString());
            contact.setGroup((String) binding.spinnerGroup.getSelectedItem());
        }
        // 返回并传递更新的数据
        Intent resultIntent = new Intent();
        resultIntent.putExtra("updatedContact", contact);
        setResult(RESULT_OK, resultIntent);
        // 返回主页面
        finish();
    }

    private void deleteContact() {
        // 删除联系人逻辑
        if (contact != null) {
            contact.setName("");
        }
        Intent resultIntent = new Intent();
        resultIntent.putExtra("updatedContact", contact);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
