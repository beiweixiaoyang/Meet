package com.example.meet.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.meet.R;
import com.example.meet.adapter.CommonAdapter;
import com.example.meet.adapter.CommonViewHolder;
import com.example.meet.base.BaseBackActivity;
import com.example.meet.bmob.BmobManager;
import com.example.meet.bmob.MeetUser;
import com.example.meet.event.EventManager;
import com.example.meet.manager.DialogManager;
import com.example.meet.utils.FileUtil;
import com.example.meet.utils.LogUtils;
import com.example.meet.view.DialogView;
import com.example.meet.view.LoadingView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.aigestudio.datepicker.cons.DPMode;
import cn.aigestudio.datepicker.views.DatePicker;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FetchUserInfoListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class PersonActivity extends BaseBackActivity implements View.OnClickListener {


    //基本信息
    private CircleImageView iv_user_photo;
    private EditText et_nickname;
    private TextView tv_user_sex;
    private LinearLayout ll_user_sex;
    private TextView tv_user_age;
    private LinearLayout ll_user_age;
    private EditText et_user_desc;
    private TextView tv_user_birthday;
    private LinearLayout ll_user_birthday;
    private TextView tv_user_constellation;
    private LinearLayout ll_user_constellation;
    private TextView tv_user_hobby;
    private LinearLayout ll_user_hobby;
    private TextView tv_user_status;
    private LinearLayout ll_user_status;
    private RelativeLayout ll_photo;

    //头像选择框
    private DialogView mPhotoDialog;
    private TextView tv_camera;
    private TextView tv_ablum;
    private TextView tv_photo_cancel;

    //性别选择框
    private DialogView mSexDialog;
    private TextView tv_boy;
    private TextView tv_girl;
    private TextView tv_sex_cancel;

    //年龄选择框
    private DialogView mAgeDialog;
    private RecyclerView mAgeView;
    private TextView tv_age_cancel;
    private CommonAdapter<Integer> mAgeAdapter;
    private List<Integer> mAgeList = new ArrayList<>();

    //生日选择框
    private DialogView mBirthdayDialog;
    private DatePicker mDatePicker;

    //星座选择框
    private DialogView mConstellationDialog;
    private RecyclerView mConstellationnView;
    private TextView tv_constellation_cancel;
    private CommonAdapter<String> mConstellationAdapter;
    private List<String> mConstellationList = new ArrayList<>();

    //状态选择框
    private DialogView mStatusDialog;
    private RecyclerView mStatusView;
    private TextView tv_status_cancel;
    private CommonAdapter<String> mStatusAdapter;
    private List<String> mStatusList = new ArrayList<>();

    //爱好选择框
    private DialogView mHobbyDialog;
    private RecyclerView mHobbyView;
    private TextView tv_hobby_cancel;
    private CommonAdapter<String> mHobbyAdapter;
    private List<String> mHobbyList = new ArrayList<>();

    //头像文件
    private File uploadPhotoFile;

    //加载View
    private LoadingView mLodingView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);
        initView();
        initPhotoDialog();
        initSexDialog();
        initAgeDialog();
        initBirthdayDialog();
        initConstellationDialog();
        initHobbyDialog();
        initStatusDialog();
        loadPersonInfo();
    }

    private void initView() {
        mLodingView = new LoadingView(this);

        iv_user_photo = (CircleImageView) findViewById(R.id.iv_user_photo);

        et_nickname = (EditText) findViewById(R.id.et_nickname);

        tv_user_sex = (TextView) findViewById(R.id.tv_user_sex);
        ll_user_sex = (LinearLayout) findViewById(R.id.ll_user_sex);

        tv_user_age = (TextView) findViewById(R.id.tv_user_age);
        ll_user_age = (LinearLayout) findViewById(R.id.ll_user_age);

        et_user_desc = (EditText) findViewById(R.id.et_user_desc);

        tv_user_birthday = (TextView) findViewById(R.id.tv_user_birthday);
        ll_user_birthday = (LinearLayout) findViewById(R.id.ll_user_birthday);

        tv_user_constellation = (TextView) findViewById(R.id.tv_user_constellation);
        ll_user_constellation = (LinearLayout) findViewById(R.id.ll_user_constellation);

        tv_user_hobby = (TextView) findViewById(R.id.tv_user_hobby);
        ll_user_hobby = (LinearLayout) findViewById(R.id.ll_user_hobby);

        tv_user_status = (TextView) findViewById(R.id.tv_user_status);
        ll_user_status = (LinearLayout) findViewById(R.id.ll_user_status);

        ll_photo = (RelativeLayout) findViewById(R.id.ll_photo);

        iv_user_photo.setOnClickListener(this);
        ll_user_sex.setOnClickListener(this);
        ll_user_age.setOnClickListener(this);
        ll_user_birthday.setOnClickListener(this);
        ll_user_constellation.setOnClickListener(this);
        ll_user_hobby.setOnClickListener(this);
        ll_user_status.setOnClickListener(this);
        ll_photo.setOnClickListener(this);
    }

    /**
     * 加载个人信息
     */
    private void loadPersonInfo() {
        LogUtils.i("loadPersonInfo");
        BmobManager.getInstance().queryByObjectId(BmobManager.getInstance().getCurrentUser().getObjectId()
                , new FindListener<MeetUser>() {
                    @Override
                    public void done(List<MeetUser> list, BmobException e) {
                        if(e == null){
                            MeetUser meetUser = list.get(0);

                            Glide.with(PersonActivity.this)
                                    .load(meetUser.getPhoto())
                                    .into(iv_user_photo);
                            et_nickname.setText(meetUser.getNickName());
                            tv_user_sex.setText(meetUser.isSex() ? "男" : "女");
                            tv_user_age.setText(meetUser.getAge() + "岁");
                            et_user_desc.setText(meetUser.getDesc());

                            tv_user_birthday.setText(meetUser.getBirthday());
                            tv_user_constellation.setText(meetUser.getConstellation());
                            tv_user_hobby.setText(meetUser.getHobby());
                            tv_user_status.setText(meetUser.getStatus());
                        }
                    }
                });
    }

    /**
     * 初始化修改头像提示框
     */
    private void initPhotoDialog() {
        mPhotoDialog = DialogManager.getInstance().initDialogView(this, R.layout.dialog_select_photo);
        tv_camera = (TextView) mPhotoDialog.findViewById(R.id.tv_camera);
        tv_ablum = (TextView) mPhotoDialog.findViewById(R.id.tv_album);
        tv_photo_cancel = (TextView) mPhotoDialog.findViewById(R.id.tv_cancel);
        tv_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogManager.getInstance().hideDialog(mPhotoDialog);
                FileUtil.getInstance().toCamera(PersonActivity.this);
            }
        });
        tv_ablum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogManager.getInstance().hideDialog(mPhotoDialog);
                FileUtil.getInstance().toAlbum(PersonActivity.this);
            }
        });
        tv_photo_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogManager.getInstance().hideDialog(mPhotoDialog);
            }
        });
    }

    /**
     * 初始化性别修改提示框
     */
    private void initSexDialog() {
        mSexDialog = DialogManager.getInstance().initDialogView(this, R.layout.dialog_select_sex, Gravity.BOTTOM);
        tv_boy = (TextView) mSexDialog.findViewById(R.id.tv_boy);
        tv_girl = (TextView) mSexDialog.findViewById(R.id.tv_girl);
        tv_sex_cancel = (TextView) mSexDialog.findViewById(R.id.tv_cancel);
        tv_boy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogManager.getInstance().hideDialog(mSexDialog);
                tv_user_sex.setText("男");
            }
        });
        tv_girl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogManager.getInstance().hideDialog(mSexDialog);
                tv_user_sex.setText("女");
            }
        });
        tv_sex_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogManager.getInstance().hideDialog(mSexDialog);
            }
        });
    }

    /**
     * 初始化年龄选择提示框
     */
    private void initAgeDialog() {

        for (int i = 0; i < 100; i++) {
            mAgeList.add(i);
        }

        mAgeDialog = DialogManager.getInstance().initDialogView(this, R.layout.dialog_select_age, Gravity.BOTTOM);
        mAgeView = (RecyclerView) mAgeDialog.findViewById(R.id.mAgeView);
        tv_age_cancel = (TextView) mAgeDialog.findViewById(R.id.tv_cancel);

        mAgeView.setLayoutManager(new LinearLayoutManager(this));
        mAgeView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mAgeAdapter = new CommonAdapter<>(mAgeList, new CommonAdapter.OnBindDataListener<Integer>() {
            @Override
            public void onBindViewHolder(final Integer model, CommonViewHolder hodler, int type, int position) {
                hodler.setText(R.id.tv_age_text, model + "");

                hodler.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DialogManager.getInstance().hideDialog(mAgeDialog);
                        tv_user_age.setText(model + "");
                    }
                });
            }

            @Override
            public int getLayoutId(int viewType) {
                return R.layout.layout_me_age_item;
            }
        });
        mAgeView.setAdapter(mAgeAdapter);

        tv_age_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogManager.getInstance().hideDialog(mAgeDialog);
            }
        });

    }

    /**
     * 初始化生日选择提示框
     */
    private void initBirthdayDialog() {
        mBirthdayDialog = DialogManager.getInstance().initDialogView(this, R.layout.dialog_select_birthday, Gravity.BOTTOM);
        mDatePicker = mBirthdayDialog.findViewById(R.id.mDatePicker);
        //设置默认时间
        mDatePicker.setDate(1995, 5);
        //设置选择模式：单选
        mDatePicker.setMode(DPMode.SINGLE);
        mDatePicker.setOnDatePickedListener(new DatePicker.OnDatePickedListener() {

            @Override
            public void onDatePicked(String date) {
                DialogManager.getInstance().hideDialog(mBirthdayDialog);
                tv_user_birthday.setText(date);
            }
        });
    }

    /**
     * 初始化星座选择提示框
     */
    private void initConstellationDialog() {
        String[] cArray = getResources().getStringArray(R.array.ConstellatioArray);
        for (int i = 0; i < cArray.length; i++) {
            mConstellationList.add(cArray[i]);
        }

        mConstellationDialog = DialogManager.getInstance().initDialogView(this, R.layout.dialog_select_constellation, Gravity.BOTTOM);
        mConstellationnView = mConstellationDialog.findViewById(R.id.mConstellationnView);
        tv_constellation_cancel = mConstellationDialog.findViewById(R.id.tv_cancel);

        mConstellationnView.setLayoutManager(new GridLayoutManager(this, 4));
        mConstellationnView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL));
        mConstellationnView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mConstellationAdapter = new CommonAdapter<>(mConstellationList, new CommonAdapter.OnBindDataListener<String>() {
            @Override
            public void onBindViewHolder(final String model, CommonViewHolder hodler, int type, int position) {
                hodler.setText(R.id.tv_age_text, model);

                hodler.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DialogManager.getInstance().hideDialog(mConstellationDialog);
                        tv_user_constellation.setText(model);
                    }
                });
            }

            @Override
            public int getLayoutId(int viewType) {
                return R.layout.layout_me_age_item;
            }
        });
        mConstellationnView.setAdapter(mConstellationAdapter);

        tv_constellation_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogManager.getInstance().hideDialog(mConstellationDialog);
            }
        });
    }

    /**
     * 初始化爱好提示框
     */
    private void initHobbyDialog() {
        String[] hArray = getResources().getStringArray(R.array.HobbyArray);
        for (int i = 0; i < hArray.length; i++) {
            mHobbyList.add(hArray[i]);
        }

        mHobbyDialog = DialogManager.getInstance().initDialogView(this, R.layout.dialog_select_constellation, Gravity.BOTTOM);
        mHobbyView = mHobbyDialog.findViewById(R.id.mConstellationnView);
        tv_hobby_cancel = mHobbyDialog.findViewById(R.id.tv_cancel);

        mHobbyView.setLayoutManager(new GridLayoutManager(this, 4));
        mHobbyView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL));
        mHobbyView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mHobbyAdapter = new CommonAdapter<>(mHobbyList, new CommonAdapter.OnBindDataListener<String>() {
            @Override
            public void onBindViewHolder(final String model, CommonViewHolder hodler, int type, int position) {
                hodler.setText(R.id.tv_age_text, model);

                hodler.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DialogManager.getInstance().hideDialog(mHobbyDialog);
                        tv_user_hobby.setText(model);
                    }
                });
            }

            @Override
            public int getLayoutId(int viewType) {
                return R.layout.layout_me_age_item;
            }
        });
        mHobbyView.setAdapter(mHobbyAdapter);

        tv_hobby_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogManager.getInstance().hideDialog(mHobbyDialog);
            }
        });
    }

    private void initStatusDialog() {
        String[] sArray = getResources().getStringArray(R.array.StatusArray);
        for (int i = 0; i < sArray.length; i++) {
            mStatusList.add(sArray[i]);
        }

        mStatusDialog = DialogManager.getInstance().initDialogView(this, R.layout.dialog_select_constellation, Gravity.BOTTOM);
        mStatusView = mStatusDialog.findViewById(R.id.mConstellationnView);
        tv_status_cancel = mStatusDialog.findViewById(R.id.tv_cancel);

        mStatusView.setLayoutManager(new LinearLayoutManager(this));
        mStatusView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        mStatusAdapter = new CommonAdapter<>(mStatusList, new CommonAdapter.OnBindDataListener<String>() {

            @Override
            public void onBindViewHolder(final String model, CommonViewHolder hodler, int type, int position) {
                hodler.setText(R.id.tv_age_text, model);

                hodler.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DialogManager.getInstance().hideDialog(mStatusDialog);
                        tv_user_status.setText(model);
                    }
                });
            }

            @Override
            public int getLayoutId(int type) {
                return R.layout.layout_me_age_item;
            }
        });
        mStatusView.setAdapter(mStatusAdapter);

        tv_status_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogManager.getInstance().hideDialog(mStatusDialog);
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_user_photo:
            case R.id.ll_photo:
                DialogManager.getInstance().showDialog(mPhotoDialog);
                break;
            case R.id.ll_user_sex:
                DialogManager.getInstance().showDialog(mSexDialog);
                break;
            case R.id.ll_user_age:
                DialogManager.getInstance().showDialog(mAgeDialog);
                break;
            case R.id.ll_user_birthday:
                DialogManager.getInstance().showDialog(mBirthdayDialog);
                break;
            case R.id.ll_user_constellation:
                DialogManager.getInstance().showDialog(mConstellationDialog);
                break;
            case R.id.ll_user_hobby:
                DialogManager.getInstance().showDialog(mHobbyDialog);
                break;
            case R.id.ll_user_status:
                DialogManager.getInstance().showDialog(mStatusDialog);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.me_info_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_save) {
            final MeetUser meetUser = BmobManager.getInstance().getCurrentUser();
            mLodingView.show("正在保存");
            if (uploadPhotoFile != null) {
                final BmobFile file = new BmobFile(uploadPhotoFile);
                file.uploadblock(new UploadFileListener() {
                    @Override
                    public void done(BmobException e) {
                        if (e == null) {
                            mLodingView.hide();
                            meetUser.setPhoto(file.getFileUrl());
                            updateUser(meetUser);
                        }
                    }
                });
            } else {
                updateUser(meetUser);
            }
        }
        return true;
    }

    private void updateUser(MeetUser meetUser) {
        String nickName = et_nickname.getText().toString().trim();
        if (TextUtils.isEmpty(nickName)) {
            Toast.makeText(this, "昵称不能为空", Toast.LENGTH_SHORT).show();
            mLodingView.hide();
            return;
        }

        String desc = et_user_desc.getText().toString().trim();
        String sex = tv_user_sex.getText().toString();
        String age = tv_user_age.getText().toString();
        String birthday = tv_user_birthday.getText().toString();
        String constellation = tv_user_constellation.getText().toString();
        String hobby = tv_user_hobby.getText().toString();
        String status = tv_user_status.getText().toString();

        meetUser.setNickName(nickName);
        meetUser.setDesc(desc);
        meetUser.setSex(sex.equals("男") ? true : false);
        meetUser.setAge(Integer.parseInt(age));
        meetUser.setBirthday(birthday);
        meetUser.setConstellation(constellation);
        meetUser.setHobby(hobby);
        meetUser.setStatus(status);
        meetUser.update(new UpdateListener() {
            @Override
            public void done(BmobException e) {
                mLodingView.hide();
                if (e == null) {
                    //同步缓存
                    BmobManager.getInstance().fetchUserInfo(new FetchUserInfoListener<BmobUser>() {
                        @Override
                        public void done(BmobUser bmobUser, BmobException e) {
                            if (e == null) {
                                EventManager.post(EventManager.EVENT_REFRE_ME_INFO);
                                finish();
                            } else {
                                Toast.makeText(PersonActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(PersonActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == FileUtil.CAMERA_REQUEST_CODE) {
                try {
                    FileUtil.getInstance().startPhotoZoom(this, FileUtil.getInstance().getTempFile());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == FileUtil.ALBUM_REQUEST_CODE) {
                Uri uri = data.getData();
                if (uri != null) {
                    String path = FileUtil.getInstance().getRealPathFromUri(this, uri);
                    if (!TextUtils.isEmpty(path)) {
                        uploadPhotoFile = new File(path);
                        try {
                            FileUtil.getInstance().startPhotoZoom(this, uploadPhotoFile);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else if (requestCode == FileUtil.CAMERA_CROP_RESULT) {
                uploadPhotoFile = new File(FileUtil.getInstance().getCropPath());
                LogUtils.i("uploadPhotoFile:" + uploadPhotoFile.getPath());
            }
            if (uploadPhotoFile != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(uploadPhotoFile.getPath());
                iv_user_photo.setImageBitmap(bitmap);
            }
        }
    }
}