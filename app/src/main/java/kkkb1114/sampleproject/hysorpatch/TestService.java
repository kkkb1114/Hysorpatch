package kkkb1114.sampleproject.hysorpatch;

import static android.content.Intent.getIntent;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class TestService extends Service {

    private static final int NOTIFICATION_ID = 1;
    final String CHANNEL_ID = "hysorpatch_notification_channel";
    public static String saveStorage = ""; //저장된 파일 경로
    public static String saveData = ""; //저장된 파일 내용
    // 권한 체크
    private PermissionManager permissionManager;
    // 블루투스
    private ScanCallback mScanCallback;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mScanner;
    //private ArrayList<String> scanBleDeviceAddressList; // 블루투스 기기 목록 중복 체크를 위한 ArrayList
    private ArrayList<BluetoothDevice> scanBleDeviceList;
    private ArrayList<ScanFilter> scanFilters;
    private ScanSettings mSettings;
    Context context;
    Handler handler = new Handler();

    // 다른 로직
    private static final String TAG = "HysorPatchSearching";
    private String mDeviceName;
    private BluetoothDevice mDevice;
    private HashMap<String, String> mRegisteredDevice = new HashMap<>();
    private int mRssi;

    private String Maddress;
    String type;

    public static DBHelper dbHelper;
    SQLiteDatabase sqlDB;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override

    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e("TestService", "onStartCommand");
        Maddress = intent.getStringExtra("key");
        initView(Maddress);
        type = intent.getStringExtra("type");

        MeasurBodyTempreture(type);
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        Log.e("TestService", "onCreate");
        createNotificationChannel();

        dbHelper = DBHelper.getInstance(context, "data.db", null, 1);
        sqlDB = TestService.dbHelper.getWritableDatabase();


        // 이동하려는 액티비티를 작성해준다.
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        // 노티에 전달 값을 담는다.
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent;

        // SDK 31 이상이면 PendingIntent.FLAG_IMMUTABLE (바뀐 정책상 무조건 FLAG_IMMUTABLE 사용해야함.)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)

                .setContentTitle("서비스 앱")
                .setContentText("서비스 앱 동작 중")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(NOTIFICATION_ID, notification);


        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
        String nowTime = simpleDateFormat.format(date);
    }


    public void initView(String Maddress) {
        try {
            dbHelper = DBHelper.getInstance(context, "data.db", null, 1);
            scanFilters = new ArrayList<>();
            ScanFilter scanFilter = new ScanFilter.Builder()
                    .setDeviceAddress(Maddress)
                    .build();
            scanFilters.add(scanFilter);
            scanBleDeviceList = new ArrayList<>();
            permissionManager = new PermissionManager();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void MeasurBodyTempreture(String s) {

        // 체온측정 스레드 시작.
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    setBluetoothBLE(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    /**
     * 오레오 이상 버전부턴 startForegroundService()로 서비스 실행시 Notificaiton Channel과 Notification를 만들어 Notification를 등록해야한다.
     **/
    public void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "hysorpatch_notification",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("노티채널");

            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    /**
     * 블루투스 세팅
     **/
    public void setBluetoothBLE(String s) {
        // bluetoothManager, bluetoothAdapter 세팅
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        // 블루투스가 지원되지 않는 경우 bluetoothAdapter가 null이 뜬다.
        if (mBluetoothAdapter == null) {

        } else {
            // 블루투스가 꺼져있는지 확인
            if (mBluetoothAdapter.isEnabled()) {
                // 스캐너 생성
                mScanner = mBluetoothAdapter.getBluetoothLeScanner();
                // 스캔 설정
                mSettings = new ScanSettings.Builder()
                        .setScanMode(2)
                        .build();
                // 스캔 콜백 메소드
                setBLEScanCallback(s);
                // 스캔 시작
                setBleScanTime();
            }
        }
    }


    /**
     * 스캔 시작
     **/
    public void setBleScanTime() {
        checkBlePermission();
        //bluetoothLeScanner.startScan(scanFilters, setting, scanCallback);
        if (mScanner != null && mScanCallback != null) {
            mScanner.startScan(scanFilters, mSettings, mScanCallback);
        }
    }

    /**
     * 블루투스 스캔 콜백
     **/
    public void setBLEScanCallback(String s) {
        String FD = s;
        Log.d("Type",s);
        this.mScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                checkBlePermission();

                processResultFilter(result);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                checkBlePermission();

                for (ScanResult result : results) {
                    processResultFilter(result);
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);

            }

            /** 블루투스 스캔 결과 필터 **/
            public void processResultFilter(ScanResult result) {
                String trim;
                ScanRecord scanRecord = result.getScanRecord();
                TestService.this.mDeviceName = scanRecord.getDeviceName();
                TestService.this.mRssi = result.getRssi(); // 기기와의 신호 세기
                TestService.this.mDevice = result.getDevice();
                Log.i(TestService.TAG, "deviceName = " + TestService.this.mDeviceName);

                if (TestService.this.mDeviceName != null) {
                    Log.e("11111111111", (TestService.this.mDeviceName));
                    if (TestService.this.mDeviceName.contains(FD)) {
                        TestService.this.mDevice.getAddress().split(":");
                        if (!TestService.this.mDeviceName.contains(":")) {
                            trim = TestService.this.mDeviceName.trim();
                        } else {
                            trim = TestService.this.mDeviceName.split(":")[0];
                        }
                        Log.d(TestService.TAG, "deviceName = " + TestService.this.mDeviceName + ", address = " + TestService.this.mDevice.getAddress() + ", containsKey = " + TestService.this.mRegisteredDevice.containsKey(TestService.this.mDevice.getAddress()) + ", comingDeviceName = " + trim);
                        if (TestService.this.mRegisteredDevice.containsKey(TestService.this.mDevice.getAddress())) {
                            return;
                        }

                        saveStorage = PreferenceManager.getString(context, "saveStorage");
                        processResultSave(result);
                    }
                }
            }

            /** 블루투스 스캔 결과 저장 로직_1 **/
            private void processResultSave(final ScanResult result) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        checkBlePermission();
                        if (result != null) {
                            // 저장
                            long now = System.currentTimeMillis();
                            Date date = new Date(now);
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
                            String nowTime = simpleDateFormat.format(date);
                            sqlDB.beginTransaction();
                            sqlDB.execSQL("INSERT INTO DATA VALUES ('" + nowTime + "', '" + saveData.split(":")[1] + "', '" + saveData.split(":")[2] + "','" + saveData.split(":")[3] + "');");
                            sqlDB.setTransactionSuccessful();
                            sqlDB.endTransaction();

                            //todo 여기에 저장했음======================================================================================

                            if (result.getDevice().getName() != null) {
                                Log.e("222222222222", (TestService.this.mDeviceName));
                            }
                            if (result.getDevice().getAddress() != null) {
                                Log.e("3333333333", result.getDevice().getAddress());
                            }
                        }
                    }
                });
            }
        };
    }


    /**
     * 블루투스 주변 기기 검색 권한 체크
     **/
    public boolean checkBlePermission() {
        // 권한 체크 (만약 권한이 허용되어있지 않으면 권한 요청)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!(permissionManager.permissionCheck(this, android.Manifest.permission.BLUETOOTH_CONNECT) ||
                    permissionManager.permissionCheck(this, android.Manifest.permission.BLUETOOTH_SCAN))) {

                permissionManager.requestPermission(context, 0, new String[]{
                        android.Manifest.permission.BLUETOOTH_CONNECT,
                        android.Manifest.permission.BLUETOOTH_SCAN
                });
                return false;
            }else {
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

    @Override
    public void onDestroy() {
        super.onDestroy();

    }
}
