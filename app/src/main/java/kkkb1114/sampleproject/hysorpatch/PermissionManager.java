package kkkb1114.sampleproject.hysorpatch;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;

public class PermissionManager {

    public static final int PERMISSION_REQUEST_LOCATION_CODE = 100;
    public static final int PERMISSION_REQUEST_CODE_LOCATION_S = 101;
    public static final int PERMISSION_REQUEST_CODE_POST_NOTIFICATIONS = 102;

    /** 권한 확인 **/
    // 기기 SDK 확인을 여기서 안하기 때문에 사용하기전에 SDK 확인 필요하다.
    public boolean permissionCheck(Context context, String strPermission){
        // 권한이 허용되어있다면 true를 반환, 거부되어있으면 false 반환
        try {
            return ActivityCompat.checkSelfPermission(context, strPermission) == PackageManager.PERMISSION_GRANTED;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /** 권한 요청 **/
    // 기기 SDK 확인을 여기서 안하기 때문에 사용하기전에 SDK 확인 필요하다.
    public void requestPermission(Context context, int request_permission_type, String[] strPermissions){
        try {
            if (request_permission_type == 0){ // 위치, 블루투스 권한
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){ // SDK 31 이상이면 블루투스 권한 요청
                    Log.e("checkBlePermission", "requestPermission_S");
                    ActivityCompat.requestPermissions(MainActivity.getActivity(), strPermissions, PERMISSION_REQUEST_CODE_LOCATION_S);
                }else { // SDK 31 이하이면 위치 권한 요청
                    ActivityCompat.requestPermissions(MainActivity.getActivity(), strPermissions, PERMISSION_REQUEST_LOCATION_CODE);
                }

            }else if (request_permission_type == 1){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    Log.e("checkBlePermission", "requestPermission_TIRAMISU");
                    ActivityCompat.requestPermissions(MainActivity.getActivity(), strPermissions, PERMISSION_REQUEST_CODE_POST_NOTIFICATIONS);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
