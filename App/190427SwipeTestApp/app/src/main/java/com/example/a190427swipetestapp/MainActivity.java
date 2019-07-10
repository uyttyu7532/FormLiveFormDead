package com.example.a190427swipetestapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import me.relex.circleindicator.CircleIndicator;

public class MainActivity extends AppCompatActivity {

    public String ip = "192.168.22.180"; //190612 TEST

    // 뷰 페이저
    FragmentPagerAdapter adapterViewPager;

    // 190510 추가 (현재 시간 출력)

    // 현재시간을 msec으로 구한다.
    long now = System.currentTimeMillis();
    // 현재시간을 date변수에 저장
    Date date = new Date(now);
    // 시간 포맷
    SimpleDateFormat sdfNow = new SimpleDateFormat("M월 d일 E요일 a h시 m분", Locale.KOREAN);
    // nowDate 변수(String)에 값을 저장
    String formatDate = sdfNow.format(date);

    TextView dateNow;
    // (현재 시간 출력) 끝
    // 시간이 남으면 새로고침 기능 구현하기. 위의 코드는 실행당시의 시간만 출력

    /* GpsInfo 관련 */
    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;

    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;

    private boolean isAccessFineLocation = false;

    private boolean isAccessCoarseLocation = false;

    private boolean isPermission = false;

    private GpsInfo gps;

    TextView locationNow; // 화면에 출력할 현재 위치

    double latitude; // 위도
    double longitude; // 경도

    String locationShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 뷰페이저 있던 곳


        dateNow = (TextView) findViewById(R.id.dateNow);
        dateNow.setText(formatDate); // TextView에 현재 시간 문자열 할당

        locationNow = (TextView) findViewById(R.id.locationNow);



        if (!isPermission) {
            callPermission();
        }

        // GpsInfo 객체 생성
        gps = new GpsInfo(MainActivity.this);

        // GPS 사용유무 가져오기
        if (gps.isGetLocation()) {
            //GPSInfo를 통해 알아낸 위도값과 경도값
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

            //Geocoder
            Geocoder gCoder = new Geocoder(MainActivity.this, Locale.getDefault());

            List<Address> list = null;

            try {
                list = gCoder.getFromLocation(latitude, longitude, 10);

            } catch (IOException e) {
                e.printStackTrace();
            }


            // Geocoder 객체로 위치 주소를 시군구동으로 표기
            if (list != null) {
                if (list.size() == 0) {
                    Toast.makeText(MainActivity.this, "주소정보 없음", Toast.LENGTH_LONG).show();
                }
                else{
                    String cut[] = list.get(0).toString().split(" ");
                    for(int i=0; i<cut.length; i++) {
                        System.out.println("cut[" +i+"] : "+ cut[i]);
                    }
                    String location = cut[1] + " " + cut[2] + " "+ cut[3];
                    locationShare = cut[1] + " " + cut[2];
                    locationNow.setText(location);
                }
            }
        } else {

            // GPS 를 사용할수 없으므로
            gps.showSettingsAlert();

        }

        callPermission();  // 권한 요청을 해야 함



        ViewPager vpPager = (ViewPager) findViewById(R.id.vpPager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);

        CircleIndicator indicator = (CircleIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(vpPager);

    } // onCreate 끝


    // 권한 요청 메소드 (GPS)
    private void callPermission() {

        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(
                MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_ACCESS_FINE_LOCATION);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(
                MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_ACCESS_COARSE_LOCATION);

        } else {
            isPermission = true;
        }
    }

    // PagerAdapter
    public static class MyPagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 3;

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Fragment구현을 위해 필요한 메소드
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return HomeFragment.newInstance(0, "Page # 1");
                case 1:
                    return SecondFragment.newInstance(1, "Page # 2");
                case 2:
                    return ShareFragment.newInstance(2, "Page # 3");
                default:
                    return null;
            }
        }

        // Fragment
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Fragment
        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + position;
        }
    }


    // ShareFragment에서 사진 intent 결과를 받기 위해 필요한 메소드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


}
