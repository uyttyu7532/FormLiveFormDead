import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


//연결실패 대비완료
//테스트 완료
class DBMatchTable{
	
	String driver = "org.mariadb.jdbc.Driver";
	//DB=로컬호스트로 지정. 코드 이전 시 IP주소 바꿀 것
    String dbUrl="jdbc:mariadb://127.0.0.1:3306/formlive_formdead";
	String dbId="fsfd";
	String dbPw="1234";	
	Connection con=null;
	Statement smt=null;
    //PreparedStatement pstmt=null;
    ResultSet rs=null;
    
	
	String getClothes(int temp, boolean page) throws SQLException{
		
		//연결 및 쿼리 시도 실패 시 디폴트값인 Connection Failed가 그대로 리턴됨
		//연결 및 쿼리 시도 성공 시 정상적으로 변경된 의상 정보 리턴
		String returnValue="Connection Failed";
		int success=1;
		
		//온도 
		int categorizedTemp=categorizeTemp(temp);
		
		//연결 및 쿼리 시도
		try {
			//마리아 DB 연결
            Class.forName(driver);
            con = DriverManager.getConnection(dbUrl,dbId,dbPw); //마리아 DB 연결
            if( con != null ) {
                System.out.println("DB 접속 성공");
            }//if 
            smt=con.createStatement();
            
            //쿼리 입력
            String sql="select * from matchtable where temp="+categorizedTemp+";";
            //결과 로드 및 저장
			ResultSet rs=smt.executeQuery(sql);
			while(rs.next()){
				int t =rs.getInt("temp");
				String top=rs.getString("top");
				String bottoms=rs.getString("bottoms");
				String outerClothing=rs.getString("outerClothing");
				//연결 및 결과 로드 성공 시 returnValue값이 정상적으로 변경
				if(page==true) { //상의;하의;외투; 반환
					//프로토콜::서버->앱, 홈 화면 로드(DB)::상의;하의;외투;
					returnValue=top+";"+bottoms+";"+outerClothing+";";
				}
				else { //상의;외투; 반환
					returnValue=top+";"+outerClothing+";";
				}
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
			if(success==1) {
				 //*****MatchTable 연결실패 대비함수
				if(page==true) { //상의;하의;외투; 반환
					//프로토콜::서버->앱, 홈 화면 로드(DB)::상의;하의;외투;
					returnValue=getClothesFailedHome(categorizedTemp);
				}
				else { //상의;외투; 반환
					returnValue=getClothesFailedSecond(categorizedTemp);
				}
	            return returnValue;
			}
		}//finally

		return returnValue;
		
	}//getClothes
	
	int categorizeTemp(int temp) {
		
		if(temp>=28) {
			return 28;
		}
		else if(temp>=23) {
			return 23;
		}
		else if(temp>=20) {
			return 20;
		}
		else if(temp>=17) {
			return 17;
		}
		else if(temp>=12) {
			return 12;
		}
		else if(temp>=9) {
			return 9;
		}
		else if(temp>=5) {
			return 5;
		}
		else {
			return 4;
		}
	}//categorizeTemp
	
	String getClothesFailedHome(int temp) {
		String cloth;
		//프로토콜::서버->앱, 홈 화면 로드(DB)::상의;하의;외투;
		switch (temp){
		case 4:
			cloth="기모 맨투맨, 니트;기모바지;패딩, 두꺼운 코트;";
			break;
		case 5:
			cloth="히트텍, 니트;레깅스;코트, 가죽자켓;";
			break;
		case 9:
			cloth="니트;청바지, 스타킹;자켓, 트렌치코트, 야상;";
			break;
		case 12:
			cloth="맨투맨;청바지, 면바지;자켓, 가디건, 야상;";
			break;
		case 17:
			cloth="얇은 니트, 맨투맨;청바지;가디건;";
			break;
		case 20:
			cloth="긴팔;면바지, 청바지;얇은 가디건;";
			break;
		case 23:
			cloth="반팔, 얇은 셔츠;반바지, 면바지;없음;";
			break;
		case 28:
			cloth="민소매, 반팔;반바지;없음;";
			break;
		default:
			System.out.println("잘못된 온도값");
			cloth="";
			break;		
		}
		
		return cloth;
	}//getClothesFailed
	
	String getClothesFailedSecond(int temp) {
		String cloth;
		//프로토콜::서버->앱, 홈 화면 로드(DB)::상의;하의;외투;
		switch (temp){
		case 4:
			cloth="기모 맨투맨, 니트;패딩, 두꺼운 코트;";
			break;
		case 5:
			cloth="히트텍, 니트;코트, 가죽자켓;";
			break;
		case 9:
			cloth="니트;자켓, 트렌치코트, 야상;";
			break;
		case 12:
			cloth="맨투맨;자켓, 가디건, 야상;";
			break;
		case 17:
			cloth="얇은 니트, 맨투맨;가디건;";
			break;
		case 20:
			cloth="긴팔;얇은 가디건;";
			break;
		case 23:
			cloth="반팔, 얇은 셔츠;없음;";
			break;
		case 28:
			cloth="민소매, 반팔;없음;";
			break;
		default:
			System.out.println("잘못된 온도값");
			cloth="";
			break;		
		}
		
		return cloth;
	}//getClothesFailed
	

}//DBMatchTable