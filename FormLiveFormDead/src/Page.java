import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

class Page{
	File image;
	String time;
	String place;
	String weather;
	
	Page(){
		image=new File("C:\\flfdServerDB\\default.png");
		time=":";
		place="위치 정보가 없습니다.";
		weather="날씨 정보가 없습니다.";
	}
	
	Page(String imageName, String t, String p, String w){
		String imagePath="C:\\flfdServerDB\\"+imageName+".png";
		image=new File(imagePath);
		time=t;
		place=p;
		weather=w;
	}
	
	Page(String imageName, String imagePath, String t, String p, String w){
		String iPath=imagePath+imageName+".png";
		image=new File(iPath);
		time=t;
		place=p;
		weather=w;
	}
	
	Page(String imageName, String imagePath, String time, String minutes, String p, String w){
		String iPath=imagePath+imageName+".png";
		image=new File(iPath);
		time=time+":"+minutes;
		place=p;
		weather=w;
	}
	
	File getImage(){	return image;	} //객체 image를 리턴한다
	void setImage(File src) {
		try(BufferedInputStream in = new BufferedInputStream(new FileInputStream(src)) ;
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(this.image))) {
			int data;
			while(true) {
				data = in.read();
				if(data == -1)
					break;
				out.write(data);
			}
			in.close();
			out.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}//객체 image에 값을 입력한다. 
	
	String getTime() {	return time;	}//변수 time를 리턴한다.
	String getPlace() {	return place;	}//변수 place를 리턴한다. 
	String getWeather() {	return weather;	}//변수 weather를 리턴한다. 
	void setTime(String t) {	this.time=t;	}//변수 time에 값을 입력한다.
	void setPlace(String p) {	this.place=p;	}//변수 place에 값을 입력한다. 
	void setWeather(String w) {	this.weather=w;	}//변수 weather에 값을 입력한다.
}