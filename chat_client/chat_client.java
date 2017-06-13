import java.net.*;
import java.io.*;
import java.util.*;
 
public class ChatClient{
       public static void main(String[] args){
            
             Scanner sc = new Scanner(System.in);
             String ip,id,password;
            
             Socket sock = null;
             BufferedReader br = null;
             PrintWriter pw = null;
             boolean endflag = false;

             System.out.println("--------------Chat_Client-------------");
             System.out.print("Enter Sever IP : ");
             ip = sc.next();
            
             try{
                    /******************************************************************
                     입력받은 ip로 10001번 포트에 접속
                   
                     1. 서버에 접속하기 위해 Socket 생성하고,
                        Socket으로부터 InputStream과 OutputStream을  얻어와서
                            각각 Buffered와 PrintWriter 형태로 변환시킴
                     ******************************************************************/
                  
      
                    sock = new Socket(ip, 10001);
                    OutputStream out = sock.getOutputStream();
                    InputStream in = sock.getInputStream();
                    pw = new PrintWriter(new OutputStreamWriter(out));
                    br = new BufferedReader(new InputStreamReader(in));
                    

             


                    /******************************************************************
                     2. 키보드로부터 입력받기 위한 BufferedReader를 생성한 후,
                     서버로부터 전달된 문자열을 모니터에 출력하는 InputThread 객체를 생성
                     ******************************************************************/                  
                    BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
                   
                    System.out.print("Enter your ID : ");
                    id = sc.next();
                    // 사용자의 id를 서버로 전송한다
                    pw.println(id);
                    pw.flush();
                   
                    InputThread it = new InputThread(sock, br);
                    it.start();
                   
                    /******************************************************************
                     3. 키보드로부터 한 줄씩 입력받아 서버에 전송(/quit를 입력받기 전까지)
                     ******************************************************************/                  
                    String line = null;
                   
                   FileInputStream fins;
                   DataOutputStream dous = new DataOutputStream(out);
                   String file_path = null;
                   
                    while((line = keyboard.readLine()) != null){
                        if (line.indexOf("/file") == 0)
                         {
                           
                           int start = line.indexOf(" ") +1; //start는 멤버 ID부터 시작하는 offset
                           
                           int end = line.indexOf(" ", start); //end는 file이름부터 시작하는 offset
                             
                           if(end!=-1)
                                file_path = line.substring(end+1);  //띄어쓰기 바로다음부터니까 file이름
                           
                           //  String file_name = file_path.substring(file_path.lastIndexOf("\\"));
                             
                              fins = new FileInputStream(new File(file_path));
                                
                              byte buffer[] = new byte[1024];
                              int len;
                              int data = 0;
                              
                              
                              pw.println("/file "+ line.substring(start, end) + " " + file_path);
                              pw.flush();
                              
                              while((len=fins.read(buffer))>0)
                                   ++data;
                                
                                fins.close();
                                fins = new FileInputStream(file_path);
                                dous.writeInt(data);
                                dous.writeUTF(file_path);
                                
                                len = 0;
                                
                                for(; data>0; --data){
                                   len = fins.read(buffer);
                                   out.write(buffer, 0, len);
                                }
                                
                              fins.close();
                             
                             
                         }
                        else if (line.indexOf("/yes") == 0)
                        {
                              DataInputStream d_ins = new DataInputStream(in);  
                           
                        
                           
                               int data = d_ins.readInt();
                               String filename = d_ins.readUTF();
                               File file = new File(System.getProperty("user.home") + "/" + filename);
                               out = new FileOutputStream(file);
                               
                               byte[] buffer = new byte[1024];
                               int len;
                               
                               for(; data>0; --data){
                                  len = in.read(buffer);
                                  out.write(buffer, 0, len);
                               }
                               out.close();
                           
                        }
                        else{
                           pw.println(line);
                           pw.flush();
                           if(line.equals("/quit")){
                                 endflag = true;
                                 break;}
                           
                              }
                    }
                    System.out.println("클라이언트의 접속을 종료합니다.");
                   
             }catch(Exception ex){
                    if(!endflag)
                           System.out.println(ex);
             }finally{
                    try{
                           if(pw != null)
                                 pw.close();
                    }catch(Exception ex){}
                   
                    try{
                           if(sock != null)
                                 sock.close();
                    }catch(Exception ex){}
                   
             }
       }
}
      
/******************************************************************
4. 서버로부터 전달 받은 문자열을 모니터에 출력하는 InputThread 객체를 생성하여
BuffereadReader와 Socket 객체를 인자로 전달 받음
******************************************************************/
class InputThread extends Thread{
     

       private Socket sock = null;
       private BufferedReader br = null;
       public InputThread(Socket sock, BufferedReader br){
             this.sock = sock;
             this.br = br;
       }
            
       /******************************************************************
       5. 서버로부터 문자열을 읽어 들여 모니터에 출력함
       ******************************************************************/
       public void run(){
             try{
               
                    String line = null;
                    while((line = br.readLine())!=null){
                           System.out.println(line);
                    }
             }catch(Exception e){
             }finally{
                    try{
                           if(br!=null){
                                 br.close();
                           }
                    }catch(Exception ex){}
                    try{
                           if(sock!=null){
                                  sock.close();
                           }
                    }catch(Exception ex){}
             }
       }
}