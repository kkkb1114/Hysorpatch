package kkkb1114.sampleproject.hysorpatch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public static MainActivity mainActivity;
    public PermissionManager permissionManager;

    Button serviceBtn;

    EditText et_address;
    String address = " ";
    Context context;
    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        mainActivity = this;
        permissionManager = new PermissionManager();
        initView();
        setListner();
        setPill_dateTextWatcher(et_address);

        checkBlePermission();
    }

    public void initView()
    {
        serviceBtn = (Button) findViewById(R.id.bt_serviceStart);
        et_address = (EditText) findViewById(R.id.et_address);
        spinner = (Spinner)findViewById(R.id.spinner);
        ArrayAdapter sAdapter = ArrayAdapter.createFromResource(this, R.array.my_array, android.R.layout.simple_spinner_dropdown_item);
        sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(sAdapter);
    }

    public void setListner()
    {
        serviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (et_address.getText().toString().length() < 17){
                    Toast.makeText(context, "address를 끝까지 입력해주세요.", Toast.LENGTH_SHORT).show();
                }else {
                    Intent intent = new Intent(context, TestService.class);
                    stopService(intent);

                    startTestService(String.valueOf(et_address.getText()),String.valueOf(spinner.getSelectedItem()));
                }
            }
        });


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView)parent.getChildAt(0)).setTextColor(Color.BLACK);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

    }

    public static Activity getActivity(){
        return mainActivity;
    }

    /** 권한 모두 허용 되어 있으면 서비스 시작 **/
    public boolean startTestService(String s,String s2){
        if (checkBlePermission() && checkStoragePermission()){
            Intent serviceIntent = new Intent(this, TestService.class);
            serviceIntent.putExtra("key",s);
            serviceIntent.putExtra("type",s2);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            }else {
                startService(serviceIntent);
            }
            return true;
        }else {
            checkBlePermission();
            return false;
        }
    }

    /**
     * address 기입
     **/
    public void setPill_dateTextWatcher(EditText et_address) {
        et_address.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                String pill_date_get = et_address.getText().toString();

                if (!pill_date_get.equals(address)) {
                    // 문자 replace
                    pill_date_get = pill_date_get.replaceAll("[^A-F0-9 ]", "");

                    address = dateTimeFormat(pill_date_get);
                    et_address.setText(address);
                    Selection.setSelection(et_address.getText(), address.length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    /**
     * address 문자열 변환
     **/
    public String dateTimeFormat(String address) {
        if (address.length() >= 3 && address.length() <= 4) {
            address = address.substring(0, 2) + ":" + address.substring(2, address.length());
            return address;
        } else if (address.length() >= 5 && address.length() <= 6) {
            address = address.substring(0, 2) + ":" + address.substring(2, 4) + ":" + address.substring(4, address.length());
            return address;
        } else if (address.length() >= 7 && address.length() <= 8) {
            address = address.substring(0, 2) + ":" + address.substring(2, 4) + ":" + address.substring(4, 6) + ":" + address.substring(6, address.length());
            return address;
        } else if (address.length() >= 9 && address.length() <= 10) {
            address = address.substring(0, 2) + ":" + address.substring(2, 4) + ":" + address.substring(4, 6) +
                    ":" + address.substring(6, 8) + ":" + address.substring(8, address.length());
            return address;
        } else if (address.length() >= 11 && address.length() <= 12) {
            address = address.substring(0, 2) + ":" + address.substring(2, 4) + ":" + address.substring(4, 6) +
                    ":" + address.substring(6, 8) + ":" + address.substring(8, 10) + ":" + address.substring(10, address.length());
            return address;
        } else {
            return address;
        }
    }

    /**
     * 블루투스 주변 기기 검색 권한 체크
     **/
    public boolean checkBlePermission() {
        Log.e("checkBlePermission", "시작");
        // 권한 체크 (만약 권한이 허용되어있지 않으면 권한 요청)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.e("checkBlePermission", "S");
            if (!(permissionManager.permissionCheck(this, android.Manifest.permission.BLUETOOTH_CONNECT) ||
                    permissionManager.permissionCheck(this, android.Manifest.permission.BLUETOOTH_SCAN))) {

                Log.e("checkBlePermission", "S_false");
                permissionManager.requestPermission(context, 0, new String[]{
                        android.Manifest.permission.BLUETOOTH_CONNECT,
                        android.Manifest.permission.BLUETOOTH_SCAN
                });
                return false;
            }else {
                Log.e("checkBlePermission", "S_true");
                return true;
            }
        }else {
            if (!(permissionManager.permissionCheck(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ||
                    permissionManager.permissionCheck(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ||
                    permissionManager.permissionCheck(this, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION))) {

                permissionManager.requestPermission(context,0, new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                });
                return false;
            }else {
                return true;
            }
        }
    }

    /**
     * 저장소 권한 체크
     **/
    public boolean checkStoragePermission() {
        Log.e("checkStoragePermission", "시작");
        // 권한 체크 (만약 권한이 허용되어있지 않으면 권한 요청)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.e("checkStoragePermission", "TIRAMISU");
            if (!(permissionManager.permissionCheck(this, android.Manifest.permission.READ_MEDIA_IMAGES) ||
                    permissionManager.permissionCheck(this, android.Manifest.permission.READ_MEDIA_AUDIO) ||
                    permissionManager.permissionCheck(this, android.Manifest.permission.READ_MEDIA_VIDEO))) {

                Log.e("checkStoragePermission", "TIRAMISU_false");
                permissionManager.requestPermission(context,1, new String[]{
                        android.Manifest.permission.READ_MEDIA_IMAGES,
                        android.Manifest.permission.READ_MEDIA_AUDIO,
                        android.Manifest.permission.READ_MEDIA_VIDEO
                });
                return false;
            }else {
                Log.e("checkStoragePermission", "TIRAMISU_true");
                return true;
            }

        }else {
            Log.e("checkStoragePermission", "TIRAMISU_이하");
            if (!(permissionManager.permissionCheck(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    permissionManager.permissionCheck(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE))) {

                Log.e("checkStoragePermission", "TIRAMISU_이하_false");
                permissionManager.requestPermission(context,1, new String[]{
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                });
                return false;
            }else {
                Log.e("checkStoragePermission", "TIRAMISU_이하_true");
                return true;
            }
        }
    }

    /** 권한 요청 결과 처리 메서드 **/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PermissionManager.PERMISSION_REQUEST_LOCATION_CODE:
            case PermissionManager.PERMISSION_REQUEST_CODE_LOCATION_S:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 권한이 허용되었을 때 실행할 코드 (위치 다음은 저장소 권한 요청)
                    checkStoragePermission();
                    Log.e("위치권한", "허용");
                } else {
                    Log.e("위치권한", "거부");
                    // 권한이 거부되었을 때 실행할 코드
                    Toast.makeText(this, "위치 권한이 거부 되어있으면 기능을 이용하실 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
                break;

            case PermissionManager.PERMISSION_REQUEST_STORAGE_CODE:
            case PermissionManager.PERMISSION_REQUEST_STORAGE_TIRAMISU:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 권한이 허용되었을 때 실행할 코드

                    Log.e("저장소권한", "허용");
                } else {
                    Log.e("저장소권한", "거부");
                    // 권한이 거부되었을 때 실행할 코드
                    Toast.makeText(this, "저장소 권한이 거부 되어있으면 기능을 이용하실 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}