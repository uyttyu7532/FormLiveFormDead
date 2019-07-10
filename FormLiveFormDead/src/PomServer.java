
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
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.sql.SQLException;

public class PomServer implements Runnable {
   static String nowweather;
   static int nowtemp, mintemp, maxtemp, rainfall, yesterdaytemp, PM10, PM25;
   static String latitude;
   static String lonitude;

   private Socket socket;

   BufferedReader bufferedReader = null;
   PrintWriter printWriter = null;

   int bytesRead;
   int current = 0;

   public PomServer(Socket socket) {

      this.socket = socket;

   }

   static class getjson {
      String addr;
      String json;

      public getjson(String addr) throws IOException {
         this.addr = addr;
         // this.json = json;

         try {
            URL Url = new URL(addr); // URL화 한다.
            HttpURLConnection conn = (HttpURLConnection) Url.openConnection();
            conn.setRequestMethod("GET");
            conn.setInstanceFollowRedirects(true);
            HttpURLConnection.setFollowRedirects(true);
            InputStream is = conn.getInputStream();
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
               builder.append(line + "\n");
            }
            final String result = builder.toString();
            this.json = result;
         } catch (Exception e) {
         }
      }
   }

   static void parsingNowTemp() throws IOException {

      String addr_nowtemp = "https://api2.sktelecom.com/weather/current/hourly?version=1&lat=" + latitude + "&lon="
            + lonitude
            + "&coordType=EPSG3857&addressType=A02&callback=result&appKey=7dd5a5d7-64ed-4b34-b944-477d7f4df648";

      getjson getjson_nowtemp = new getjson(addr_nowtemp);

      final String nowTemp_str = (getjson_nowtemp.json.split("\"temperature\":")[1].split("\"")[3]);
      nowtemp = (int) Double.parseDouble(nowTemp_str);
   }

   static void parsingMinMaxTemp() throws IOException {

      String addr_todaytemp = "https://api2.sktelecom.com/weather/summary?version=1&lat=" + latitude + "&lon="
            + lonitude
            + "&coordType=EPSG3857&addressType=A02&callback=result&appKey=7dd5a5d7-64ed-4b34-b944-477d7f4df648";

      getjson getjson_todaytemp = new getjson(addr_todaytemp);

      final String mintemperature_str = (getjson_todaytemp.json.split("\"tmin\":")[1].split("\"")[1]);
      mintemp = (int) Double.parseDouble(mintemperature_str);

      final String maxtemperature_str = (getjson_todaytemp.json.split("\"tmax\":")[1].split("\"")[1]);
      maxtemp = (int) Double.parseDouble(maxtemperature_str);

   }

   static void parsingNowWeather() throws IOException {
      String addr_nowweather = "https://api2.sktelecom.com/weather/current/hourly?version=1&lat=" + latitude + "&lon="
            + lonitude
            + "&coordType=EPSG3857&addressType=A02&callback=result&appKey=7dd5a5d7-64ed-4b34-b944-477d7f4df648";
      getjson getjson_nowweather = new getjson(addr_nowweather);
      nowweather = getjson_nowweather.json.split("\"name\":")[1].split("\"")[1]; // 현재날씨
   }

   static void parsingYesterdayTemp() throws IOException {

      String addr_yesterdaytemp = "https://api2.sktelecom.com/weather/yesterday?version=1&lat=" + latitude + "&lon="
            + lonitude
            + "&coordType=EPSG3857&addressType=A02&callback=result&appKey=7dd5a5d7-64ed-4b34-b944-477d7f4df648";
      getjson getjson_yesterdaytemp = new getjson(addr_yesterdaytemp);
      String yesterdaytemperature_str = getjson_yesterdaytemp.json.split("\"temperature\":\"")[1].split("\"")[0]; // 어제온도
      yesterdaytemp = (int) Double.parseDouble(yesterdaytemperature_str);
   }

   static void parsingRainfall() throws InterruptedException, IOException {

      String addr_rainfall = "https://api2.sktelecom.com/weather/forecast/3days?version=1&lat=" + latitude + "&lon="
            + lonitude
            + "&coordType=EPSG3857&addressType=A02&callback=result&appKey=7dd5a5d7-64ed-4b34-b944-477d7f4df648";
      getjson getjson_rainfall = new getjson(addr_rainfall);

      String rainfall_str = getjson_rainfall.json.split("\"prob4hour\":")[1].split("\"")[1]; // 현재날씨
      rainfall = (int) Double.parseDouble(rainfall_str);

   }

   static void parsingPM() throws IOException {

      String addr_WQ = "https://api.waqi.info/feed/geo:" + latitude + ";" + lonitude
            + "/?token=89844abc19cbf457d4d12d05d86a4dbf0003c225";
      getjson getjson_WQ = new getjson(addr_WQ);

      String PM10_str = getjson_WQ.json.split("\"pm10\":")[1].split("\"v\":")[1].split("\\}")[0]; // pm10
      PM10 = (int) Double.parseDouble(PM10_str);

      String PM25_str = getjson_WQ.json.split("\"pm25\":")[1].split("\"v\":")[1].split("\\}")[0]; // pm10
      PM25 = (int) Double.parseDouble(PM25_str);
   }

   static int getNowTemp() throws IOException {
      parsingNowTemp();
      return nowtemp;
   }

   static int getMinTemp() throws IOException {
      parsingMinMaxTemp();
      return mintemp;
   }

   static int getMaxTemp() throws IOException {
      parsingMinMaxTemp();
      return maxtemp;
   }

   static String getNowWeather() throws IOException {
      parsingNowWeather();
      return nowweather;
   }

   static int getYesterdayTemp() throws IOException {
      parsingYesterdayTemp();
      return yesterdaytemp;
   }

   static int getRainfall() throws InterruptedException, IOException {
      parsingRainfall();
      return rainfall;
   }

   static int getPM10() throws IOException {
      parsingPM();
      return PM10;
   }

   static int getPM25() throws IOException {
      parsingPM();
      return PM25;
   }

   public void parsingInfo(String message) throws IOException, SQLException, InterruptedException {

      String m[] = message.split(";");

      for (int i = 0; i < m.length; i++) {
         System.out.println(m[i]);
      }

      switch (m[0]) {
      case "HOME":
         parsingHome(m);
         break;
      case "SECOND":
         parsingSecond(m);
         break;
      case "SHARE":
         if (m[1].equals("R")) {
            switch(m[2]) {
            case "0":
               parsingShareRead0();
               break;
            case "1":
               parsingShareRead1();
               break;
            case "2":
               parsingShareRead2();
               break;
            }
         } else if (m[1].equals("W")) {
            parsingShareWrite(m);
         }
      }

   }

   public void run() {

      try {
         
         bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

         // 클라이언트로부터 온 메시지를 읽는다.
         String clientMessage = bufferedReader.readLine();
         System.out.println("clientMessage" + clientMessage);
         

         try {
            parsingInfo(clientMessage);
         } catch (SQLException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         

         // 클라이언트로부터 온 메시지를 해석하기 위해 clientMessage를 매개변수로 parsingInfo를 호출한다.
         // try {
         // //printWriter.println(parsingInfo(clientMessage));
         //
         // } catch (SQLException e) {
         // // TODO Auto-generated catch block
         // e.printStackTrace();
         // }

      } catch (IOException e) {

         e.printStackTrace();

      }

      finally {
         
         

         if (bufferedReader != null) {

            try {

               bufferedReader.close();

            } catch (IOException e) {

               e.printStackTrace();

            }

         }

         if (printWriter != null) {

            printWriter.close();

         }

         if (socket != null) {

            try {

               socket.close();

            } catch (IOException e) {

               e.printStackTrace();

            }

         }

      }

   }

   void parsingHome(String[] message) throws IOException, SQLException, InterruptedException {// 메세지 받아서 위도, 경도 산출 후 날씨
                                                                        // 함수 호출, 온도값(int) 반환

      DBMatchTable dbm = new DBMatchTable();
      printWriter = new PrintWriter(socket.getOutputStream(), true);

      String clothProtocol;// 홈 화면 로드 시 반환값이 저장된 변수. 상의;하의;외투; 형태로 저장되어 있음
      String response;

      latitude = message[1];
      lonitude = message[2];

      clothProtocol = dbm.getClothes(getNowTemp(), true);
      // clothProtocol = matchClothesHomeScreen(getNowTemp());

      // HOME;현재온도;현재날씨;강수확률;미세먼지농도;상의;하의;외투;
      response = "HOME;" + getNowTemp() + ";" + getNowWeather() + ";" + getRainfall() + ";" + getPM10() + ";"
            + clothProtocol;

      System.out.println(response);
      

      printWriter.println(response);

   }

   void parsingSecond(String[] message) throws IOException, SQLException, InterruptedException {

      DBMatchTable dbm = new DBMatchTable();
      printWriter = new PrintWriter(socket.getOutputStream(), true);

      String response;

      String maxTempCloth; // 최고기온에 맞는 옷차림
      String minTempCloth; // 최저기온에 맞는 옷차림

      latitude = message[1];
      lonitude = message[2];

      maxTempCloth = dbm.getClothes(getMaxTemp(), false);
      minTempCloth = dbm.getClothes(getMinTemp(), false);

      // maxTempCloth = matchClothesSecondScreen(getMaxTemp());
      // minTempCloth = matchClothesSecondScreen(getMinTemp());

      // SECOND;현재기온;현재날씨;최고기온;최고기온상의;최고기온외투;최저기온;최저기온상의;최저기온외투;미세먼지;초미세먼지;
      response = ("SECOND;" + getNowTemp() + ";" + getNowWeather() + ";" + getMaxTemp() + ";" + maxTempCloth
            + getMinTemp() + ";" + minTempCloth + getPM10() + ";" + getPM25());
      System.out.println(response);

      printWriter.println(response);

   }

//   void parsingShareRead() throws IOException, SQLException {
//
//      printWriter = new PrintWriter(socket.getOutputStream(), true);
//
//      // 변수 설명
//      // Page uPage = new Page(); // upload()용 Page 객체; 사용자로부터 받은 정보를 해당 객체에 입력할 것
//      Page[] returnPage; // 공유화면 업로드 및 단순로딩 시, 클라이언트에게 보낼 현재 페이지가 저장된 객체배열
//      DBShare dbs = new DBShare();
//      returnPage = dbs.loadPage();
//      
//      String shareResponse;
//
//      shareResponse = "SHARE;R;" + returnPage[0].time + ";" + returnPage[0].place + ";" + returnPage[0].weather + ";"
//            + returnPage[1].time + ";" + returnPage[1].place + ";" + returnPage[1].weather + ";"
//            + returnPage[2].time + ";" + returnPage[2].place + ";" + returnPage[2].weather;
//            
//      System.out.println(shareResponse);
//      printWriter.println(shareResponse);
//      
//            
////      for(int i=0;i<3;i++) {
//         // sendfile
//         // File myFile = new File (returnPage[0].image);
////         File myFile = returnPage[i].image;
//         File myFile = returnPage[0].image;
//         byte[] mybytearray = new byte[(int) myFile.length()];
//         FileInputStream fis = new FileInputStream(myFile);
//         BufferedInputStream bis = new BufferedInputStream(fis);
//         bis.read(mybytearray, 0, mybytearray.length);
//         OutputStream os = socket.getOutputStream();
//         System.out.println("Sending...");
//         os.write(mybytearray, 0, mybytearray.length);
//         os.flush();
//         System.out.println("보내긴 했음");
//         
//         bis.close();
//         
////      }
//
//      
//   }
   void parsingShareRead0() throws IOException, SQLException {

      printWriter = new PrintWriter(socket.getOutputStream(), true);

      // 변수 설명
      // Page uPage = new Page(); // upload()용 Page 객체; 사용자로부터 받은 정보를 해당 객체에 입력할 것
      Page[] returnPage; // 공유화면 업로드 및 단순로딩 시, 클라이언트에게 보낼 현재 페이지가 저장된 객체배열
      DBShare dbs = new DBShare();
      returnPage = dbs.loadPage();
      
      String shareResponse;

      shareResponse = "SHARE;R;" + returnPage[0].time + ";" + returnPage[0].place + ";" + returnPage[0].weather;
            
      System.out.println(shareResponse);
      printWriter.println(shareResponse);
      
            
//      for(int i=0;i<3;i++) {
         // sendfile
         // File myFile = new File (returnPage[0].image);
//         File myFile = returnPage[i].image;
         File myFile = returnPage[0].image;
         byte[] mybytearray = new byte[(int) myFile.length()];
         FileInputStream fis = new FileInputStream(myFile);
         BufferedInputStream bis = new BufferedInputStream(fis);
         bis.read(mybytearray, 0, mybytearray.length);
         OutputStream os = socket.getOutputStream();
         System.out.println("Sending...");
         os.write(mybytearray, 0, mybytearray.length);
         os.flush();
         System.out.println("보내긴 했음");
         
         bis.close();
         
//      }

      
   }
   void parsingShareRead1() throws IOException, SQLException {

      printWriter = new PrintWriter(socket.getOutputStream(), true);

      // 변수 설명
      // Page uPage = new Page(); // upload()용 Page 객체; 사용자로부터 받은 정보를 해당 객체에 입력할 것
      Page[] returnPage; // 공유화면 업로드 및 단순로딩 시, 클라이언트에게 보낼 현재 페이지가 저장된 객체배열
      DBShare dbs = new DBShare();
      returnPage = dbs.loadPage();
      
      String shareResponse;

      shareResponse = "SHARE;R;" + returnPage[1].time + ";" + returnPage[1].place + ";" + returnPage[1].weather;
            
      System.out.println(shareResponse);
      printWriter.println(shareResponse);
      
            
//      for(int i=0;i<3;i++) {
         // sendfile
         // File myFile = new File (returnPage[0].image);
//         File myFile = returnPage[i].image;
         File myFile = returnPage[1].image;
         byte[] mybytearray = new byte[(int) myFile.length()];
         FileInputStream fis = new FileInputStream(myFile);
         BufferedInputStream bis = new BufferedInputStream(fis);
         bis.read(mybytearray, 0, mybytearray.length);
         OutputStream os = socket.getOutputStream();
         System.out.println("Sending...");
         os.write(mybytearray, 0, mybytearray.length);
         os.flush();
         System.out.println("보내긴 했음");
         
         bis.close();
         
//      }

      
   }
   void parsingShareRead2() throws IOException, SQLException {

      printWriter = new PrintWriter(socket.getOutputStream(), true);

      // 변수 설명
      // Page uPage = new Page(); // upload()용 Page 객체; 사용자로부터 받은 정보를 해당 객체에 입력할 것
      Page[] returnPage; // 공유화면 업로드 및 단순로딩 시, 클라이언트에게 보낼 현재 페이지가 저장된 객체배열
      DBShare dbs = new DBShare();
      returnPage = dbs.loadPage();
      
      String shareResponse;

      shareResponse = "SHARE;R;" + returnPage[2].time + ";" + returnPage[2].place + ";" + returnPage[2].weather;
            
      System.out.println(shareResponse);
      printWriter.println(shareResponse);
      
            
//      for(int i=0;i<3;i++) {
         // sendfile
         // File myFile = new File (returnPage[0].image);
//         File myFile = returnPage[i].image;
         File myFile = returnPage[2].image;
         byte[] mybytearray = new byte[(int) myFile.length()];
         FileInputStream fis = new FileInputStream(myFile);
         BufferedInputStream bis = new BufferedInputStream(fis);
         bis.read(mybytearray, 0, mybytearray.length);
         OutputStream os = socket.getOutputStream();
         System.out.println("Sending...");
         os.write(mybytearray, 0, mybytearray.length);
         os.flush();
         System.out.println("보내긴 했음");
         
         bis.close();
         
//      }

      
   }

   void parsingShareWrite(String[] m) throws IOException, SQLException {

      printWriter = new PrintWriter(socket.getOutputStream(), true);
      
      //handshaking
      printWriter.println("give me image file");
      //이미지 파일 받아오기
      File image=new File("C:\\flfdServerDB\\reader\\test.png");
      
      byte[] mybytearray1 = new byte[6022386];
      InputStream is = socket.getInputStream();
      FileOutputStream fos = new FileOutputStream(image);
      BufferedOutputStream bos = new BufferedOutputStream(fos);

      do {
         bytesRead = is.read(mybytearray1, current, (mybytearray1.length - current));
         if (bytesRead >= 0)
            current += bytesRead;
      } while (bytesRead > -1);

      bos.write(mybytearray1, 0, current);
      bos.flush();
      bos.close();
      
      //페이지 객체 정의
      Page uPage = new Page("test","C:\\flfdServerDB\\reader\\",m[2],m[3],getNowWeather()); // upload()용 Page 객체; 사용자로부터 받은 정보를 해당 객체에 입력할 것
      Page[] returnPage; // 공유화면 업로드 및 단순로딩 시, 클라이언트에게 보낼 현재 페이지가 저장된 객체배열

      DBShare dbs = new DBShare();
      
      //업로드
      dbs.upload(uPage);
returnPage = dbs.loadPage();
      
      String shareResponse;

      shareResponse = "SHARE;R;" + returnPage[2].time + ";" + returnPage[2].place + ";" + returnPage[2].weather;
            
      System.out.println(shareResponse);
      printWriter.println(shareResponse);
      
         File myFile = returnPage[2].image;
         byte[] mybytearray = new byte[(int) myFile.length()];
         FileInputStream fis = new FileInputStream(myFile);
         BufferedInputStream bis = new BufferedInputStream(fis);
         bis.read(mybytearray, 0, mybytearray.length);
         OutputStream os = socket.getOutputStream();
         System.out.println("Sending...");
         os.write(mybytearray, 0, mybytearray.length);
         os.flush();
         System.out.println("보내긴 했음");
         
         bis.close();

      System.out.println("case 업로드");

   }

}// serverthread
