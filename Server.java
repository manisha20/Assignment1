import java.io.*;
import java.net.*;

public class Server {

    public static void main(String[] args) throws IOException {
        int filesize=60228789; // file size temporary hardcoded
        
        
        
        
       
        
        
        

        long start = System.currentTimeMillis();
        int bytesRead;
        int current = 0;

        // create socket
        ServerSocket servsock = new ServerSocket(8887);
        while (true) {
          System.out.println("Waiting...");

          Socket sock = servsock.accept();
          System.out.println("Accepted connection : " + sock);

          int ret=0;
          int offset=0;
       // receive file
            byte [] mybytearray  = new byte [filesize];
            InputStream is = sock.getInputStream();

            while ((ret = is.read(mybytearray, offset, filesize -offset)) > 0)
            {
                offset+=ret;
                // just in case the file is bigger that the buffer size
               if (offset >= filesize) break;
            }
            
            

            FileOutputStream fos = new FileOutputStream("C:\\Users\\manisha\\Desktop\\check.csv"); 
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            bytesRead = is.read(mybytearray,0,mybytearray.length);
            current = bytesRead;

            
            do {
               bytesRead = is.read(mybytearray, current, (mybytearray.length-current));
               if(bytesRead >= 0) current += bytesRead;
            } while(bytesRead > -1);

            bos.write(mybytearray, 0 ,offset );// current
            bos.flush();
            long end = System.currentTimeMillis();
            System.out.println(end-start);
            bos.close();

            //RESPONSE FROM THE SERVER
            PrintWriter out = new PrintWriter(sock.getOutputStream(), true); 
            out.println(99); //REPLY DE NUMBER 99

            out.close();


          sock.close();
          }
    }   
}
