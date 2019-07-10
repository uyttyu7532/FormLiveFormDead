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


public class HomeFragment extends Fragment {

    private String title;
    private int page;
    double latitude;
    double longitude;

    TextView nowTemperature;
    ImageView nowWeatherImage;
    TextView rainfall;
    TextView fineDust;
    TextView top;
    TextView bottom;
    TextView outerClothing;
    TextView umbrella;
    TextView maskText;

    public static HomeFragment newInstance(int page, String title) {
        HomeFragment fragment = new HomeFragment();
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
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        nowTemperature = (TextView) view.findViewById(R.id.nowTemperature1);
        nowWeatherImage = (ImageView) view.findViewById(R.id.nowWeatherImage1);
        rainfall = (TextView) view.findViewById(R.id.rainfall);
        fineDust = (TextView) view.findViewById(R.id.fineDust1);
        top = (TextView) view.findViewById(R.id.top);
        bottom = (TextView) view.findViewById(R.id.bottom);
        outerClothing = (TextView) view.findViewById(R.id.outerClothing);

        latitude = ((MainActivity) activity).latitude;
        longitude = ((MainActivity) activity).longitude;

        maskText = (TextView) view.findViewById(R.id.maskText);
        umbrella = (TextView) view.findViewById(R.id.umbrella);

        HomeAsyncTask asyncTask = new HomeAsyncTask();
        asyncTask.execute();

        return view;

    }

    class HomeAsyncTask extends AsyncTask<Void, Void, Void> {

        private Socket clientSocket; // 클라이언트 소켓
        private BufferedReader in; //서버로부터 온 데이터를 읽는다.
        private PrintWriter out; // 서버로 데이터를 보낸다.

        private int port = 1818; // 포트
        private String ip = ((MainActivity) activity).ip;

        String[] HomeArray;

        public HomeAsyncTask() {

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

                out.println("HOME;" + latitude + ";" + longitude);
                System.out.println("서버에 메시지 보냄 이제 읽을거임");
                String homeResponse = in.readLine();
                System.out.println("폼생폼사 homeResponse!!!!!!!!!!!" + homeResponse);

                HomeArray = homeResponse.split(";");

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

            HomeArray[1] = HomeArray[1] + "℃";

            // nowTemperature 현재 기온
            nowTemperature.setText(HomeArray[1]);

            // nowWeatherImage 현재 날씨 그림 변경 switch
            switch (HomeArray[2]) {
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

            // rainfall 강수확률
            rainfall.setText(HomeArray[3]);
            int rf = Integer.parseInt(HomeArray[3]);
            String rfText;
            if (rf <=20) {
                rfText = "우산 필요 없음";
                umbrella.setText(rfText);
            }
            else if(rf <= 50) {
                rfText = "강수확률 낮음";
                umbrella.setText(rfText);
            }
            else if(rf <= 70) {
                rfText = "우산 소지 추천";
                umbrella.setText(rfText);
            } else {
                rfText ="우산 필수!!";
                umbrella.setText(rfText);
            }


            // fineDust 미세먼지
            //fineDust.setText(HomeArray[4]);
            int pm10 = Integer.parseInt(HomeArray[4]);
            // WHO 기준 좋음, 보통, 나쁨, 매우 나쁨
            String pm10text;
            // 미세먼지 마스크 추천
            String mask;
            if(pm10 <= 30){
                pm10text = "좋음";
                fineDust.setText(pm10text);
                mask = "필요없음!";
                maskText.setText(mask);
            }
            else if(pm10 <= 50){
                pm10text = "보통";
                fineDust.setText(pm10text);
                mask = "마스크 추천";
                maskText.setText(mask);
            }
            else if(pm10 <=100){
                pm10text = "나쁨";
                fineDust.setText(pm10text);
                mask = "KF80 이상 권장";
                maskText.setText(mask);
            }
            else {
                pm10text = "매우 나쁨";
                fineDust.setText(pm10text);
                mask = "KF90 이상 권장";
                maskText.setText(mask);
            }

           // top 현재 온도에 맞는 상의 추천
            top.setText(HomeArray[5]);

            // bottom 현재 온도에 맞는 하의 추천
            bottom.setText(HomeArray[6]);

            // outerClothing 현재 온도에 맞는 겉옷 추천
            outerClothing.setText(HomeArray[7]);




        }
    }


}

