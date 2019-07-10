package com.example.a190427swipetestapp;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class SecondFragment extends Fragment {
    private String title;
    private int page;

    TextView nowTemperature;
    ImageView nowWeatherImage;

    TextView maxTemperature;
    TextView maxTop;
    TextView maxOuterClothing;

    TextView minTemperature;
    TextView minTop;
    TextView minOuterClothing;

    TextView fineDust;
    TextView ultraFineDust;

    TextView infoText;

    double latitude;
    double longitude;


    // newInstance constructor for creating fragment with arguments
    public static SecondFragment newInstance(int page, String title) {
        SecondFragment fragment = new SecondFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragment.setArguments(args);
        return fragment;
    }

    Activity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity)
            activity = (Activity) context;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("someInt", 0);
        title = getArguments().getString("someTitle");
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_second, container, false);

        nowTemperature = (TextView) view.findViewById(R.id.nowTemperature2);
        nowWeatherImage = (ImageView) view.findViewById(R.id.nowWeatherImage2);

        maxTemperature = (TextView) view.findViewById(R.id.maxTemperature);
        maxTop = (TextView) view.findViewById(R.id.maxTop);
        maxOuterClothing = (TextView) view.findViewById(R.id.maxOuterClothing);

        minTemperature = (TextView) view.findViewById(R.id.minTemperature);
        minTop = (TextView) view.findViewById(R.id.minTop);
        minOuterClothing = (TextView) view.findViewById(R.id.minOuterClothing);

        fineDust = (TextView) view.findViewById(R.id.fineDust);
        ultraFineDust = (TextView) view.findViewById(R.id.ultraFineDust);

        infoText = (TextView) view.findViewById(R.id.infoText);

        latitude = ((MainActivity) activity).latitude;
        longitude = ((MainActivity) activity).longitude;


        SecondAsyncTask asyncTask = new SecondAsyncTask();
        asyncTask.execute();

        return view;

    }

    class SecondAsyncTask extends AsyncTask<Void, Void, Void> {

        private Socket clientSocket; // 클라이언트 소켓
        private BufferedReader in; //서버로부터 온 데이터를 읽는다.
        private PrintWriter out; // 서버로 데이터를 보낸다.

        private int port = 1818; // 포트
        private String ip = ((MainActivity) activity).ip;

        String[] SecondArray;

        public SecondAsyncTask() {

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                clientSocket = new Socket(ip, port);
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "EUC_KR"));

                out.println("SECOND;" + latitude + ";" + longitude);
                System.out.println("서버에 메시지 보냄 이제 읽을거임");
                String secondResponse = in.readLine();
                System.out.println("폼생폼사 homeResponse!!!!!!!!!!!" + secondResponse);

                SecondArray = secondResponse.split(";");

                if(in!=null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(out!=null) {
                    out.close();
                }
                if(clientSocket!=null) {
                    try {
                        clientSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            int maxTemp = Integer.parseInt(SecondArray[3]);
            int minTemp = Integer.parseInt(SecondArray[6]);
            String info;

            SecondArray[1] = SecondArray[1] + "℃";
            SecondArray[3] = SecondArray[3] + "℃";
            SecondArray[6] = SecondArray[6] + "℃";


            // nowTemperature 현재 기온
            nowTemperature.setText(SecondArray[1]);

            // nowWeatherImage 현재 날씨 그림 변경 switch
            switch (SecondArray[2]) {
                case "맑음":
                    nowWeatherImage.setImageResource(R.drawable.sun);
                    break;
                case "구름많음":
                    nowWeatherImage.setImageResource(R.drawable.cloudy);
                    break;
                case "흐림":
                    nowWeatherImage.setImageResource(R.drawable.cloud);
                    break;
                case "비":
                    nowWeatherImage.setImageResource(R.drawable.rainy);
                    break;
                case "눈":
                    nowWeatherImage.setImageResource(R.drawable.snowy);
                    break;
            }

            // maxTemperature 최고기온
            maxTemperature.setText(SecondArray[3]);
            // maxTop 최고기온 상의
            maxTop.setText(SecondArray[4]);
            // maxOuterClothing 최고기온 외투
            maxOuterClothing.setText(SecondArray[5]);

            // minTemperature 최저기온
            minTemperature.setText(SecondArray[6]);
            // minTop 최저기온 상의
            minTop.setText(SecondArray[7]);
            // minOuterClothing 최저기온 외투
            minOuterClothing.setText(SecondArray[8]);

            // fineDust 미세먼지 농도
            fineDust.setText(SecondArray[9]);
            // ultraFineDust 초미세먼지 농도
            ultraFineDust.setText(SecondArray[10]);

            if((maxTemp-minTemp)>=9) {
                info = "큰 일교차에 주의하세요!";
                infoText.setText(info);
            }
        }
    }
}