
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

 

public class MakeServerThread {

    public static void main(String[] args) {


         ServerSocket serverSocket = null;

 
        try {

            serverSocket = new ServerSocket(1818);

            while(true){
            

                Socket socket = serverSocket.accept();
                

                if(socket != null){
                	
                	
                	Thread thread = new Thread(new PomServer(socket));
                	System.out.println("!!!!!");
                	 
                    thread.start();

                }

            }

        } catch (IOException e1) {

         

            e1.printStackTrace();

        }

    }

}

