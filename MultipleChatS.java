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
    // 실패 성공 여부

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

    public void run(){
        cs.list.add(this);
        try {
            while(true){
                int cnt = cs.list.size();
                String SThreadClientData = this.input.readLine();
                display.append(SThreadClientData + "\r\n");
                if ( SThreadClientData.split(":")[1].trim().equals("/help") ){
                    output.write(helpCommand);
                    output.flush();
                } else if ( SThreadClientData.split(":")[1].trim().equals("/checkUser") ){
                    welcomeString = "현재 접속자 리스트\r\n" + userList + "\r\n";
                    output.write(welcomeString);
                    output.flush();
                }
                if ( SThreadClientData.split(":")[0].trim().equals("id") ){
                    // use Set
                    Set<String> set = new HashSet<String>(userList);
                    if ( set.contains(SThreadClientData.split(":")[1].trim()) ) {
                        // 실패
                        output.write("#id-false\r\n");
                        output.flush();
                    } else if (clientName.equals("")){
                        // 성공
                        // clientName 지정
                        clientName = SThreadClientData.split(":")[1].trim();
                        userList.add(SThreadClientData.split(":")[1].trim());
                        output.write("#id-true\r\n");
                        welcomeString = "현재 접속자 리스트 " + userList + "\n" + "채팅 명령어 [ /help ] 입력\r\n";
                        output.write(welcomeString);
                        output.flush();
                        System.out.println(userList);
                        // .. 님이 입장하셨습니다.
                        display.append("\r\n\r\n" + this.clientName + "님이 입장하셨습니다." + "\r\n\r\n");
                        for(int i=0; i<cnt; i++){
                            ServerThread SThread = (ServerThread) cs.list.get(i);
                            SThread.output.write("\r\n\r\n" + this.clientName + "님이 입장하셨습니다." + "\r\n\r\n");
                            SThread.output.flush();
                        }
                    }
                }
                // 전체 채팅
                if ( SThreadClientData.split("_")[0].trim().equals("[allChat]")){
                    for(int i=0; i<cnt; i++){
                        ServerThread SThread = (ServerThread) cs.list.get(i);
                        SThread.output.write(SThreadClientData.substring(10) + "\r\n");
                        SThread.output.flush();
                    }
                }       
                // 귓속말
            }
        } catch(IOException e){
            e.printStackTrace();
        }
        try {
            cs.list.remove(this);
            userList.remove(this.clientName);
            this.sock.close();
        } catch (Exception e) {
            //TODO: handle exception
            e.printStackTrace();
        }
    }
}