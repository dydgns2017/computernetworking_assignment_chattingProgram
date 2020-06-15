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
        super("����");
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
                    info.setText(sock.getInetAddress().getHostName() +"������ Ŭ���̾�Ʈ�� ����� \r\n");
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
    String helpCommand = "�ӼӸ� -- /r [���̵��] \n" +
                         "���� ������ Ȯ�� -- /checkUser\r\n";
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
    // ��ü ä��
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
    // �α��� Ȯ��
    public void loginCheck(String ServiceClientName){
        // use Set
        try {
            Set<String> set = new HashSet<String>(userList);
            if ( set.contains(ServiceClientName) ) {
                // ����
                output.write("#id-false\r\n");
                output.flush();
            } else if (clientName.equals("")){
                // ����
                // clientName ����
                clientName = ServiceClientName;
                userList.add(ServiceClientName);
                output.write("#id-true\r\n");
                welcomeString = "���� ������ ����Ʈ " + userList + "\n" + "ä�� ��ɾ� [ /help ] �Է�\r\n";
                output.write(welcomeString);
                output.flush();
                System.out.println(userList);
                // .. ���� �����ϼ̽��ϴ�.
                display.append("\r\n\r\n" + this.clientName + "���� �����ϼ̽��ϴ�." + "\r\n\r\n");
                broadCast("���� �����ϼ̽��ϴ�..\r\n\r\n");
            }
        } catch (IOException e) {
            //TODO: handle exception
            e.printStackTrace();
        }
    }
    // �ӼӸ�
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
    // ���� ����
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
                    // id Ȯ��
                    loginCheck(ServiceClientName);
                }
                // -------------------- Command Service --------------------------
                if ( ServiceData.equals("/help") ){
                    output.write(helpCommand);
                    output.flush();
                } 
                if ( ServiceData.equals("/checkUser") ){
                    welcomeString = "���� ������ ����Ʈ\r\n" + userList + "\r\n";
                    output.write(welcomeString);
                    output.flush();
                }
                // �ӼӸ�
                if ( ServiceName.equals("private")){
                    System.out.println("�ӼӸ� ������.." + SThreadClientData);
                    whisper(ServiceClientName, ServiceData); 
                }
                // ��ü ä��
                if ( ServiceName.equals("allChat")){
                    broadCast(" : " + ServiceData);
                }
                // �� ����
                if ( ServiceName.equals("exitConnect")){
                    display.append(clientName + "���� ���� �������ϴ�..\r\n");
                    broadCast("���� ���� �������ϴ�..");
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