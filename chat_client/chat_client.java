import java.net.*;
import java.io.*;
import java.util.*;
 
public class chat_client{
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
                   
                  InputThread it = new InputThread(sock, br);
                  it.start();
                   
                  /******************************************************************
                        3. 키보드로부터 한 줄씩 입력받아 서버에 전송(/quit를 입력받기 전까지)
                  ******************************************************************/                  
                  String line = null;

                   
                  while((line = keyboard.readLine()) != null){
                        if (line.equals("/file")) {
                              // inputThread 일시정지
                              it.suspend();
                              file_send(pw);
                              // inputThread 재시작
                              it.resume();
                        } else {
                              pw.println(line);
                              pw.flush();
                              if(line.equals("/quit")){
                                    endflag = true;
                                    break;
                              }
                           
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

      public static void file_send(PrintWriter pw) {

            String reciever = null;
            String file_path = null;
            String file_name = null;
            Scanner sc = new Scanner(System.in);
            
            // cmd창 청소
            clearScreen();

            // 수신자와 파일에 정보 입력
            System.out.println("--------------file transfer---------------");
            System.out.print("Enter reciever ID : ");
            reciever = sc.nextLine();
            System.out.print("Enter file path : ");
            file_path = sc.nextLine();
            file_name = file_path.substring(file_path.lastIndexOf("/")+1);

            // 서버로 메세지 전송
            int port = 9999;
            
            
            
            try {

                  pw.println("/file " + reciever + " " + file_name + " " + InetAddress.getLocalHost().getHostAddress() + " " + Integer.toString(port));
                  System.out.println("/file " + reciever + " " + file_name + " " + InetAddress.getLocalHost().getHostAddress() + " " + Integer.toString(port));
                  System.out.println("----------------Waiting for reciver----------------");

                  // sever socket open
                  ServerSocket server = new ServerSocket(port);
                  Socket reciever_soc = server.accept();
                  
                  System.out.println("--------------Connected-------------------");
                  // open file stream
                  FileInputStream fin = new FileInputStream(new File(file_path));
                  DataOutputStream dout = new DataOutputStream(reciever_soc.getOutputStream());

                  byte[] buffer = new byte[1024];        //바이트단위로 임시저장하는 버퍼를 생성합니다.
                  int len;                               //전송할 데이터의 길이를 측정하는 변수입니다.
                  int data = 0;                            //전송횟수, 용량을 측정하는 변수입니다.
                  
                  while((len = fin.read(buffer))>0){     //FileInputStream을 통해 파일에서 입력받은 데이터를 버퍼에 임시저장하고 그 길이를 측정합니다.
                        data++;                        //데이터의 양을 측정합니다.
                  }

                  int datas = data;                      //아래 for문을 통해 data가 0이되기때문에 임시저장한다.
                        
                  fin.close();
                  fin = new FileInputStream(file_path);   //FileInputStream이 만료되었으니 새롭게 개통합니다.
                  dout.writeInt(data);                   //데이터 전송횟수를 서버에 전송하고,
                  dout.writeUTF(file_name);               //파일의 이름을 서버에 전송합니다.
            
                  len = 0;
            
                  for(;data>0;data--){                   //데이터를 읽어올 횟수만큼 FileInputStream에서 파일의 내용을 읽어옵니다.
                        len = fin.read(buffer);        //FileInputStream을 통해 파일에서 입력받은 데이터를 버퍼에 임시저장하고 그 길이를 측정합니다.
                        dout.write(buffer,0,len);       //서버에게 파일의 정보(1kbyte만큼보내고, 그 길이를 보냅니다.
                  }

            } catch ( Exception e ) {
                  System.out.print(e);
            }

            System.out.println("-----------Success file trasfer-----------");

      }

      public static void clearScreen() {  
            System.out.print("\033[H\033[2J");
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
                        if(line.indexOf("/file") == 0) {
                              file_recive(line);
                        } else {
                              System.out.println(line);
                        }
                           
                           
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

      public void file_recive(String line) {
            
            int to_start = line.indexOf(" ") + 1;
            int to_end = line.indexOf(" ", to_start);
            int ip_start = line.indexOf(" ", to_end) + 1;
            int ip_end = line.indexOf(" ", ip_start);
            

            String to = line.substring(to_start, to_end);
            String file_name = line.substring(to_end, ip_start - 1);
            String ip = line.substring(ip_start, ip_end);
            int port = Integer.parseInt(line.substring(ip_end + 1));


            System.out.println("------------File trasfer started-----------");
            try{
                  Socket file_socket = new Socket(ip,port); //127.0.0.1은 루프백 아이피로 자신의 아이피를 반환해주고,
                  System.out.println("--------------File stream connected-------------");        //port은 서버접속 포트입니다.

                  FileOutputStream out = null;              //클라이언트로 부터 바이트 단위로 입력을 받는 InputStream을 얻어와 개통합니다.
                  DataInputStream din = new DataInputStream(file_socket.getInputStream());  //InputStream을 이용해 데이터 단위로 입력을 받는 DataInputStream을 개통합니다.
                  
                  
                  int data = din.readInt();           //Int형 데이터를 전송받습니다.
                  String filename = din.readUTF();            //String형 데이터를 전송받아 filename(파일의 이름으로 쓰일)에 저장합니다.
                  
                  File file = new File(System.getProperty("user.home") + "/" + file_name);    //입력받은 File의 이름으로 복사하여 생성합니다.
                  out = new FileOutputStream(file);           //생성한 파일을 클라이언트로부터 전송받아 완성시키는 FileOutputStream을 개통합니다.
            
                  int datas = data;                            //전송횟수, 용량을 측정하는 변수입니다.
                  byte[] buffer = new byte[1024];        //바이트단위로 임시저장하는 버퍼를 생성합니다.
                  int len;                               //전송할 데이터의 길이를 측정하는 변수입니다.
                  
                  for(;data>0;data--){                   //전송받은 data의 횟수만큼 전송받아서 FileOutputStream을 이용하여 File을 완성시킵니다.
                        len = din.read(buffer);
                        out.write(buffer,0,len);
                  }
                  
                  System.out.println("약: "+datas+" kbps");
                  out.flush();
                  out.close();       

            }catch(Exception e){

                  System.out.print(e);
            }

            System.out.println(file_name + "을 받았습니다.");
      }
}