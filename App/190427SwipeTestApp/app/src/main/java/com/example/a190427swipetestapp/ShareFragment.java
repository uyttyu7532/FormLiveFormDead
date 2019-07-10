package com.example.a190427swipetestapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ShareFragment extends Fragment {
    private String title;
    private int page;

    FloatingActionButton fab;

    TextView time1;
    TextView location1;
    TextView time2;
    TextView location2;
    TextView time3;
    TextView location3;

    ImageView photo1;
    ImageView photo2;
    ImageView photo3;

    ImageView weather1;
    ImageView weather2;
    ImageView weather3;

    // newInstance constructor for creating fragment with arguments
    public static ShareFragment newInstance(int page, String title) {
        ShareFragment fragment = new ShareFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragment.setArguments(args);
        return fragment;
    }


    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("someInt", 0);
        title = getArguments().getString("someTitle");

    }

    Activity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity)
            activity = (Activity) context;
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_share, container, false);

        fab = (FloatingActionButton) view.findViewById(R.id.fab);

        time1 = (TextView) view.findViewById(R.id.time1);
        location1 = (TextView) view.findViewById(R.id.location1);
        time2 = (TextView) view.findViewById(R.id.time2);
        location2 = (TextView) view.findViewById(R.id.location2);
        time3 = (TextView) view.findViewById(R.id.time3);
        location3 = (TextView) view.findViewById(R.id.location3);

        photo1 = (ImageView) view.findViewById(R.id.photo1);
        photo2 = (ImageView) view.findViewById(R.id.photo2);
        photo3 = (ImageView) view.findViewById(R.id.photo3);

        weather1 = (ImageView) view.findViewById(R.id.weather1);
        weather2 = (ImageView) view.findViewById(R.id.weather2);
        weather3 = (ImageView) view.findViewById(R.id.weather3);

        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                sendTakePhotoIntent();

            }
        });


        // 게시물 가져오기!!!!
        ShareRead0AsyncTask asyncTask0 = new ShareRead0AsyncTask();
        asyncTask0.execute();

        ShareRead1AsyncTask asyncTask1 = new ShareRead1AsyncTask();
        asyncTask1.execute();

        ShareRead2AsyncTask asyncTask2 = new ShareRead2AsyncTask();
        asyncTask2.execute();


        return view;

    }// onCreateView 끝
    // 메소드 구현은 여기서부터



    /* 사진 intent 관련 */
    static final int REQUEST_IMAGE_CAPTURE = 1; // requestCode

    private String imageFilePath;
    private Uri photoUri;

    private void sendTakePhotoIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getActivity().getPackageManager())!=null) {

            File photoFile = null;

            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

            }

            if(photoFile != null) {
                photoUri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName(), photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }

        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == MainActivity.RESULT_OK)  {

            File imgFile = new File(imageFilePath);


            if(imgFile.exists()) {
                ShareWriteAsyncTask asyncTask = new ShareWriteAsyncTask();
                asyncTask.execute();
            }

        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREAN).format(new Date());
        String imageFileName = "TEST_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        imageFilePath = image.getAbsolutePath();

        return image;
    }

    class ShareRead0AsyncTask extends AsyncTask<Void, Void, Void> {

        private Socket clientSocket; // 클라이언트 소켓
        private BufferedReader in; //서버로부터 온 데이터를 읽는다.
        private PrintWriter out; // 서버로 데이터를 보낸다.

        private int port = 1818; // 포트
        private String ip = ((MainActivity) activity).ip;

        String[] ShareArray;

        int size = 6022386; // filesize temporary hardcoded
        int bytesRead;
        int current = 0;
        String path1 = Environment.getExternalStorageDirectory().toString() + "/test1.png";
        Bitmap myBitmap1;


        public ShareRead0AsyncTask() {

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

                // 서버로 메시지 보냄
                out.println("SHARE;R;0;");
                // 서버에서 메시지 받음
                // SHARE;R;time;place;weather;
                String shareReadResponse = in.readLine();
                System.out.println(shareReadResponse);
                ShareArray = shareReadResponse.split(";");

                // 공유사진 1번째
                byte[] myByteArray = new byte[size];
                InputStream is = clientSocket.getInputStream();
                FileOutputStream fos = new FileOutputStream(path1);
                BufferedOutputStream bos = new BufferedOutputStream(fos);

                do {
                    bytesRead = is.read(myByteArray, current, (myByteArray.length - current));
                    if (bytesRead >= 0)
                        current += bytesRead;
                } while (bytesRead > -1);

                bos.write(myByteArray, 0, current);
                bos.flush();

                myBitmap1 = BitmapFactory.decodeByteArray(myByteArray, 0, myByteArray.length);

               System.out.println("사진1 전송완료");


            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            time1.setText(ShareArray[2]);
            location1.setText(ShareArray[3]);
            switch (ShareArray[4]) {
                case "맑음":
                    weather1.setImageResource(R.drawable.sun);
                    break;
                case "구름많음":
                    weather1.setImageResource(R.drawable.cloudy);
                    break;
                case "흐림":
                    weather1.setImageResource(R.drawable.cloud);
                    break;
                case "비":
                    weather1.setImageResource(R.drawable.rainy);
                    break;
                case "눈":
                    weather1.setImageResource(R.drawable.snowy);
                    break;
            }

            photo1.setImageBitmap(myBitmap1);

        }
    }

    class ShareRead1AsyncTask extends AsyncTask<Void, Void, Void> {

        private Socket clientSocket; // 클라이언트 소켓
        private BufferedReader in; //서버로부터 온 데이터를 읽는다.
        private PrintWriter out; // 서버로 데이터를 보낸다.

        private int port = 1818; // 포트
        private String ip = ((MainActivity) activity).ip;

        String[] ShareArray;

        int size = 6022386; // filesize temporary hardcoded
        int bytesRead;
        int current = 0;
        String path2 = Environment.getExternalStorageDirectory().toString() + "/test2.png";
        Bitmap myBitmap2;

        public ShareRead1AsyncTask() {

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

                out.println("SHARE;R;1;");
                String shareReadResponse = in.readLine();
                System.out.println(shareReadResponse);
                ShareArray = shareReadResponse.split(";");


                // 공유사진 2번째
                byte[] myByteArray = new byte[size];
                InputStream is = clientSocket.getInputStream();
                FileOutputStream fos = new FileOutputStream(path2);
                BufferedOutputStream bos = new BufferedOutputStream(fos);

                do {
                    bytesRead = is.read(myByteArray, current, (myByteArray.length - current));
                    if (bytesRead >= 0)
                        current += bytesRead;
                } while (bytesRead > -1);

                bos.write(myByteArray, 0, current);
                bos.flush();

                myBitmap2 = BitmapFactory.decodeByteArray(myByteArray, 0, myByteArray.length);

                System.out.println("사진2 전송완료");


            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            time2.setText(ShareArray[2]);
            location2.setText(ShareArray[3]);
            switch (ShareArray[4]) {
                case "맑음":
                    weather2.setImageResource(R.drawable.sun);
                    break;
                case "구름많음":
                    weather2.setImageResource(R.drawable.cloudy);
                    break;
                case "흐림":
                    weather2.setImageResource(R.drawable.cloud);
                    break;
                case "비":
                    weather2.setImageResource(R.drawable.rainy);
                    break;
                case "눈":
                    weather2.setImageResource(R.drawable.snowy);
                    break;
            }

            photo2.setImageBitmap(myBitmap2);

        }
    }

    class ShareRead2AsyncTask extends AsyncTask<Void, Void, Void> {

        private Socket clientSocket; // 클라이언트 소켓
        private BufferedReader in; //서버로부터 온 데이터를 읽는다.
        private PrintWriter out; // 서버로 데이터를 보낸다.

        private int port = 1818; // 포트
        private String ip = ((MainActivity) activity).ip;

        String[] ShareArray;

        int size = 6022386; // filesize temporary hardcoded
        int bytesRead;
        int current = 0;
        String path3 = Environment.getExternalStorageDirectory().toString() + "/test3.png";
        Bitmap myBitmap3;

        public ShareRead2AsyncTask() {

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

                out.println("SHARE;R;2;");
                String shareReadResponse = in.readLine();
                System.out.println(shareReadResponse);
                ShareArray = shareReadResponse.split(";");

                // 공유사진 3번째
                byte[] myByteArray = new byte[size];
                InputStream is = clientSocket.getInputStream();
                FileOutputStream fos = new FileOutputStream(path3);
                BufferedOutputStream bos = new BufferedOutputStream(fos);

                do {
                    bytesRead = is.read(myByteArray, current, (myByteArray.length - current));
                    if (bytesRead >= 0)
                        current += bytesRead;
                } while (bytesRead > -1);

                bos.write(myByteArray, 0, current);
                bos.flush();

                myBitmap3 = BitmapFactory.decodeByteArray(myByteArray, 0, myByteArray.length);

                System.out.println("사진3 전송완료");


            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            time3.setText(ShareArray[2]);
            location3.setText(ShareArray[3]);
            switch (ShareArray[4]) {
                case "맑음":
                    weather3.setImageResource(R.drawable.sun);
                    break;
                case "구름많음":
                    weather3.setImageResource(R.drawable.cloudy);
                    break;
                case "흐림":
                    weather3.setImageResource(R.drawable.cloud);
                    break;
                case "비":
                    weather3.setImageResource(R.drawable.rainy);
                    break;
                case "눈":
                    weather3.setImageResource(R.drawable.snowy);
                    break;
            }

            photo3.setImageBitmap(myBitmap3);

        }
    }

    class ShareWriteAsyncTask extends AsyncTask<Void, Void, Void>{

        private Socket clientSocket; // 클라이언트 소켓
        private BufferedReader in; //서버로부터 온 데이터를 읽는다.
        private PrintWriter out; // 서버로 데이터를 보낸다.

        private int port = 1818; // 포트
        //private String ip = "192.168.22.114"; //190612 TEST
        private String ip = ((MainActivity) activity).ip;

        // 현재시간을 msec으로 구한다.
        long now = System.currentTimeMillis();
        // 현재시간을 date변수에 저장
        Date date = new Date(now);
        // 시간 포맷
        SimpleDateFormat sdfNow = new SimpleDateFormat("a h시 m분", Locale.KOREAN);
        // nowDate 변수(String)에 값을 저장
        String formatDate = sdfNow.format(date);
        String location = ((MainActivity) activity).locationShare;

        String[] ShareArray;

        int bytesRead;
        int current = 0;

        @Override
        protected Void doInBackground(Void... voids) {


            try {
                clientSocket = new Socket(ip, port);
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "EUC_KR"));

                out.println("SHARE;W;" + formatDate + ";" + location + ";");

                // handshaking
                in.readLine();


//                byte[] myByteArray = new byte[size];
//                InputStream is = clientSocket.getInputStream();
//                FileOutputStream fos = new FileOutputStream(path1);
//                BufferedOutputStream bos = new BufferedOutputStream(fos);
//
//                do {
//                    bytesRead = is.read(myByteArray, current, (myByteArray.length - current));
//                    if (bytesRead >= 0)
//                        current += bytesRead;
//                } while (bytesRead > -1);
//
//                bos.write(myByteArray, 0, current);
//                bos.flush();

                File myFile = new File (imageFilePath);
                byte [] mybytearray  = new byte [(int)myFile.length()];
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
                bis.read(mybytearray,0,mybytearray.length);

//                do {
//                    bytesRead = bis.read(mybytearray, current, (mybytearray.length - current));
//                    if (bytesRead >= 0)
//                        current += bytesRead;
//                } while (bytesRead > -1);

                OutputStream os = clientSocket.getOutputStream();
                System.out.println("Sending...");
                os.write(mybytearray,0,mybytearray.length);
                os.flush();

                String shareReadResponse = in.readLine();
                System.out.println(shareReadResponse);
                ShareArray = shareReadResponse.split(";");

                ShareRead0AsyncTask read0AsyncTask = new ShareRead0AsyncTask();
                read0AsyncTask.execute();

                ShareRead1AsyncTask read1AsyncTask = new ShareRead1AsyncTask();
                read1AsyncTask.execute();

                ShareRead2AsyncTask read2AsyncTask = new ShareRead2AsyncTask();
                read2AsyncTask.execute();

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

        }
    }
}