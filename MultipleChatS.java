import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class MultipleChatS extends Frame {
    TextArea display;
    Label info;
    String clientData = "";
    String serverData = "";
    List<ServerThread> list;

    public ServerThread SThread;

    public MultipleChatS(){
        super("서버");
        info = new Label();
        add(info, BorderLayout.CENTER);
        display = new TextArea("", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY);
        display.setEditable(false);
        add(display, BorderLayout.SOUTH);
        addWindowListener(new WinListener());
        setSize(350, 250);
        setVisible(true);
    }

    public void runServer(){
        ServerSocket server;
        Socket sock;
        ServerThread SThread;
        try {
            list = new ArrayList<ServerThread>();
            server = new ServerSocket(5000, 100);
            try {
                while(true){
                    sock = server.accept();
                    SThread = new ServerThread(this, sock, display, info, serverData);
                    SThread.start();
                    info.setText(sock.getInetAddress().getHostName() +"서버는 클라이언트와 연결됨 \r\n");
                }
            } catch (IOException e){
                server.close();
                e.printStackTrace();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        MultipleChatS s = new MultipleChatS();
        s.runServer();
    }

    class WinListener extends WindowAdapter {
        public void windowClosing(WindowEvent e){
            System.exit(0);
        }
    } 
}

class ServerThread extends Thread {
    Socket sock;
    InputStream is;
    InputStreamReader isr;
    BufferedReader input;
    OutputStream os;
    OutputStreamWriter osw;
    BufferedWriter output;
    TextArea display;
    Label info;
    TextField text;
    String serverData = "";
    MultipleChatS cs;
    String clientName = "";
    // userList
    static ArrayList<String> userList = new ArrayList<String>();

    // etc
    String helpCommand = "귓속말 -- /r [아이디명] \n" +
                         "현재 접속자 확인 -- /checkUser\r\n";
    String welcomeString;

    public ServerThread(MultipleChatS c, Socket s, TextArea ta, Label l, String data){
        sock = s;
        display = ta;
        info = l;
        serverData = data;
        cs = c;
        try {
            is = sock.getInputStream();
            isr = new InputStreamReader(is);
            input = new BufferedReader(isr);
            os = sock.getOutputStream();
            osw = new OutputStreamWriter(os);
            output = new BufferedWriter(osw);
        } catch (Exception e) {
            //TODO: handle exception
            e.printStackTrace();
        }
    }
    // 전체 채팅
    public void broadCast(String ServiceData){
        int cnt = cs.list.size();
        try {
            for(int i=0; i<cnt; i++){
                ServerThread SThread = (ServerThread) cs.list.get(i);
                SThread.output.write(clientName + ServiceData + "\r\n");
                SThread.output.flush();
            }
        } catch (IOException e) {
            //TODO: handle exception
            e.printStackTrace();
        }
    }
    // 로그인 확인
    public void loginCheck(String ServiceClientName){
        // use Set
        try {
            Set<String> set = new HashSet<String>(userList);
            if ( set.contains(ServiceClientName) ) {
                // 실패
                output.write("#id-false\r\n");
                output.flush();
            } else if (clientName.equals("")){
                // 성공
                // clientName 지정
                clientName = ServiceClientName;
                userList.add(ServiceClientName);
                output.write("#id-true\r\n");
                welcomeString = "현재 접속자 리스트 " + userList + "\n" + "채팅 명령어 [ /help ] 입력\r\n";
                output.write(welcomeString);
                output.flush();
                System.out.println(userList);
                // .. 님이 입장하셨습니다.
                display.append("\r\n\r\n" + this.clientName + "님이 입장하셨습니다." + "\r\n\r\n");
                broadCast("님이 입장하셨습니다..\r\n\r\n");
            }
        } catch (IOException e) {
            //TODO: handle exception
            e.printStackTrace();
        }
    }
    // 귓속말
    public void whisper(String to, String sendData){
        try {
            String from = this.clientName;
            int cnt = cs.list.size();
            for(int i=0; i<cnt; i++){
                ServerThread SThread = (ServerThread) cs.list.get(i);
                if( !(SThread.clientName.equals(to) || SThread.clientName.equals(from)) ) continue;
                SThread.output.write("whisper@" + from + " : " + sendData + "\r\n");
                SThread.output.flush();
            }
        } catch (IOException e) {
            //TODO: handle exception
            e.printStackTrace();
        }
    }
    // 연결 끊기
    public void exitConnect(){
        try {
            this.output.write("exitConnect");
            this.output.flush();
            cs.list.remove(this);
            userList.remove(this.clientName);
            this.sock.close();
        } catch (IOException e) {
            //TODO: handle exception
            e.printStackTrace();
        }
    }

    public void run(){
        cs.list.add(this);
        try {
            while(true){
                String SThreadClientData = this.input.readLine();
                String ServiceClientName;
                String ServiceName;
                String ServiceData = "";
                String Services[] = SThreadClientData.split("@");
                ServiceName = Services[0].trim();
                ServiceClientName = Services[1].trim();
                for(int i=2; i<Services.length; i++){
                    ServiceData += Services[i];
                }
                display.append(SThreadClientData + "\r\n");
                if ( ServiceName.equals("id") ){
                    // id 확인
                    loginCheck(ServiceClientName);
                }
                // -------------------- Command Service --------------------------
                if ( ServiceData.equals("/help") ){
                    output.write(helpCommand);
                    output.flush();
                } 
                if ( ServiceData.equals("/checkUser") ){
                    welcomeString = "현재 접속자 리스트\r\n" + userList + "\r\n";
                    output.write(welcomeString);
                    output.flush();
                }
                // 귓속말
                if ( ServiceName.equals("private")){
                    System.out.println("귓속말 동작중.." + SThreadClientData);
                    whisper(ServiceClientName, ServiceData); 
                }
                // 전체 채팅
                if ( ServiceName.equals("allChat")){
                    broadCast(" : " + ServiceData);
                }
                // 방 떠남
                if ( ServiceName.equals("exitConnect")){
                    display.append(clientName + "님이 방을 떠났습니다..\r\n");
                    broadCast("님이 방을 떠났습니다..");
                    exitConnect();
                    break;
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }catch(ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
        }
    }
}