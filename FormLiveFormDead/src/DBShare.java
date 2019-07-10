import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

class DBShare{
	
	//String IMAGEPATH="";
	
	String driver = "org.mariadb.jdbc.Driver";
	//DB=로컬호스트로 지정. 코드 이전 시 IP주소 바꿀 것
    String dbUrl="jdbc:mariadb://127.0.0.1:3306/formlive_formdead";
	String dbId="fsfd";
	String dbPw="1234";	
	Connection con=null;
	Statement smt=null;
    //PreparedStatement pstmt=null;
    ResultSet rs=null;
	int loadPageFailed;
	int uploadFailedNum;
	
	DBShare(){
		uploadFailedNum=0;
	}
    
	
	Page[] loadPage() throws SQLException{
		
		Page [] pa=new Page[3];
		File i;
		String t;
		String p;
		String w;
		String ip;
	    int success=1;
		
		try {
			//마리아 DB 연결
            Class.forName(driver);
            con = DriverManager.getConnection(dbUrl,dbId,dbPw); //마리아 DB 연결
            if( con != null ) {
                System.out.println("DB 접속 성공");
            }
            smt=con.createStatement();
            
            //쿼리 입력
            String sql="select * from sharepage_all;";
            //결과 로드 및 출력
			ResultSet rs=smt.executeQuery(sql);
			while(rs.next()){
				int n=rs.getInt("number");
				t=rs.getString("time");
				p=rs.getString("place");
				w=rs.getString("weather");
				ip=rs.getString("imagePath")+n+".png";
				
				//System.out.println("Test load::::: "+n+" | "+t+" | "+p+" | "+w+" | "+ip);

				pa[n-1]=new Page(n+"",t,p,w);
				
				//System.out.println("Test load::::: "+pa[n-1].time+" | "+pa[n-1].place+" | "+pa[n-1].weather);
				
			}//while  
			success=0;//오류없음
			this.loadPageFailed=1;//loadPage
        }//try
		catch (ClassNotFoundException e) { 
            System.out.println("드라이버 로드 실패");
        }//catch 
		catch (SQLException e) {
            System.out.println("DB 접속 실패");
            //e.printStackTrace();
        }//catch
		finally {
			//*****loadPage 연결실패 대비함수
			if(success==1) {
				this.loadPageFailed=0;//loadPage실패; 다음번 연결 시 업데이트 요망
				pa=loadPageFailed();
			}
			
		}
		return pa;
	}//loadPage
	
	

	void upload(Page newPage) throws SQLException, IOException{ //upload manager
		
		
		if(uploadFailedNum>0) {
			//2,3 정보 갱신
			Page[] pa=new Page[2];
			String info="";
			try {
				File f=new File("C:\\flfdServerDB\\temporaryDBUpload\\upload.txt");
				Scanner s=new Scanner(f);
				while(s.hasNextLine()) {
					info=s.nextLine();
				}
			}
			catch(FileNotFoundException e) {
			System.out.println("error:파일 없음");	
			}
			String i[]=info.split(";");
			pa[0]=new Page("1","C:\\flfdServerDB\\temporaryDBUpload",i[1],i[2],i[3]);
			pa[1]=new Page("2","C:\\flfdServerDB\\temporaryDBUpload",i[5],i[6],i[7]);
			newUpload(pa[0],true);
			newUpload(pa[1],true);
			uploadFailedNum=0;
			
		}
		
		newUpload(newPage,false);
		//*****로드 실패 시 읽을 텍스트 파일 갱신
		
	}//upload
	
	
void newUpload(Page newPage, boolean update) throws SQLException, IOException{ //upload
		
		String nTime=newPage.time;
		String nPlace=newPage.place;
		String nWeather=newPage.weather;
		StringBuffer sb=new StringBuffer("insert into sharepage_all(number,time,place,weather) values(1,'");
		sb.append(nTime);
		sb.append("','");
		sb.append(nPlace);
		sb.append("','");
		sb.append(nWeather);
		sb.append("');");
		String all=sb.toString();
		int success=1;
		
		try {
			//마리아 DB 연결
            Class.forName(driver);
            con = DriverManager.getConnection(dbUrl,dbId,dbPw); //마리아 DB 연결
            if( con != null ) {
                System.out.println("DB 접속 성공");
            }
            smt=con.createStatement();
            
            //쿼리 입력+//결과 로드 및 출력
            //데이터 2, 1의 넘버를 한칸씩 미룸
            String sql="update sharepage_all set number=number+1;";
            smt.executeUpdate(sql);
            //새로운 정보를 1로 업데이트
            smt.executeUpdate(all);
            //데이터 3(가장 오래된 데이터) 삭제
            sql="delete from sharepage_all where number=4;";
            smt.executeUpdate(sql);
            
            //확인용 출력 코드
            ResultSet rs=smt.executeQuery("select * from sharepage_all;");
			while(rs.next()){
				int n=rs.getInt("number");
				String t=rs.getString("time");
				String p=rs.getString("place");
				String w=rs.getString("weather");
				String ip=rs.getString("imagePath");
				System.out.println("Test load::::: "+n+" | "+t+" | "+p+" | "+w+" | "+ip);
			}//while
			success=0;//오류 없음
        }//try 
		catch (ClassNotFoundException e) { 
            System.out.println("드라이버 로드 실패");
        }//catch 
		catch (SQLException e) {
            System.out.println("DB 접속 실패");
            //e.printStackTrace();    
        }//catch
		finally {
			//*****upload 연결실패 시 임시저장 대비함수
			//*****loadPage 연결실패 대비함수
			if(success==1) {
				uploadFailed(newPage);
			}
			else if(success==0||update==false) { //연결 실패 시 업로드로 인한 데이터 꼬임 방지
				//3번째 파일 삭제
				File f3=new File("C:\\flfdServerDB\\3.png");
				f3.delete();
				//1,2번째 파일 이름 하나씩 미루기
				File f2=new File("C:\\flfdServerDB\\2.png");
				File f1=new File("C:\\flfdServerDB\\1.png");
				setImagePath(f2,"C:\\flfdServerDB\\3.png");
				setImagePath(f1,"C:\\flfdServerDB\\2.png");
				//최근 파일을 1번째 파일로 추가
				File nf=new File("C:\\flfdServerDB\\1.png");
				copyImage(newPage.image,nf);
			}
			else if(success==0||update==true) {
				
				//*************여기 손봐야함
				//3번째 파일 삭제
				File f3=new File("C:\\flfdServerDB\\3.png");
				f3.delete();
				//1,2번째 파일 이름 하나씩 미루기
				File f2=new File("C:\\flfdServerDB\\2.png");
				File f1=new File("C:\\flfdServerDB\\1.png");
				setImagePath(f2,"C:\\flfdServerDB\\3.png");
				setImagePath(f1,"C:\\flfdServerDB\\2.png");
				//최근 파일을 1번째 파일로 추가
				File nf=new File("C:\\flfdServerDB\\1.png");
				copyImage(newPage.image,nf);
			}
			
		}//finally

	}//upload
	
	
	void copyImage(File src, File dest) {
		try(BufferedInputStream in = new BufferedInputStream(new FileInputStream(src)) ;
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dest))) {
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
		
	}
	
	void setImagePath(File f, String newPath) {
		File dest=new File(newPath);
		if(f.exists()) {
			 f.renameTo( dest );
		}
		return;
	}
	
	
	Page[] loadPageFailed() {
		//로딩용 임시저장소에 저장되어있는 파일 3개 리턴 후 계속 연결 시도(스레드 2개)
		//C:\flfdServerDB\temporaryDBLoad
		Page[] pa=new Page[3];
		String t;
		String p;
		String w;
		int n;
		try {
		       // 바이트 단위로 파일읽기
			String filePath = "C:/flfdServerDB/temporaryDBLoad/load.txt"; // 대상 파일
			FileInputStream fileStream = null; // 파일 스트림
		        
		    fileStream = new FileInputStream( filePath );// 파일 스트림 생성
		        //버퍼 선언
		    byte[ ] readBuffer = new byte[fileStream.available()];
		    while (fileStream.read( readBuffer ) != -1){}
		    String s=new String(readBuffer);
		    String[] parse=s.split(";");
		    //시간;위치;날씨;
		    for(int i=0, j=0;i<3;i++,j+=3) {
		    	t=parse[j];
			    p=parse[j+1];
			    w=parse[j+2];
			    n=i+1;
			    pa[i]=new Page("C:\\flfdServerDB\\"+n,t,p,w);
		    }
		    
		    fileStream.close(); //스트림 닫기
		} catch (Exception e) {
			e.getStackTrace();
		}
		
		
		return pa;
	}
	
	
	void uploadFailed(Page newPage) throws IOException {
		//시간;장소;날씨;
		//업로드용 임시저장소에 업로드정보 저장
		//C:\flfdServerDB\temporaryDBUpload
		uploadFailedNum++;
		StringBuffer sb= new StringBuffer(""+uploadFailedNum);
		sb.append(";");
		sb.append(newPage.time);
		sb.append(";");
		sb.append(newPage.place);
		sb.append(";");
		sb.append(newPage.weather);
		sb.append(";");
		
		String txt=sb.toString();
		
		System.out.println(txt);
		
		
		//파일에 스트링 쓰는 걸 수정해야함 안써짐...ㅋ쿠ㅜ
		
        FileWriter writer = null;
        File f = new File("C:/flfdServerDB/temporaryDBUpload/upload.txt");
		try {
            // 기존 파일의 내용에 이어서 쓰려면 true를, 기존 내용을 없애고 새로 쓰려면 false를 지정한다.
			
            writer = new FileWriter(f, true);
            writer.write(txt);
            writer.flush();
            
            System.out.println("DONE");
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(writer != null) writer.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
		
		//이미지옮기기
		File n=new File("C:\\flfdServerDB\\temporaryDBUpload\\"+uploadFailedNum+".png");
		copyImage(newPage.image,n);
		
	}//uploadFailed
	
}//DBShare
