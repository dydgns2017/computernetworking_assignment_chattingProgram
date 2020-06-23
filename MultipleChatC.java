import java.io.*; 
import java.net.*;
import java.awt.*;
import java.awt.event.*;

public class MultipleChatC extends Frame implements ActionListener {
    TextArea display;
    TextField text;
    Label lword;
    BufferedWriter output;
    BufferedReader input;
    Socket client;
    String clientData = "";
    String serverData = "";
    // 클라이언트 이름
    String clientName = "";
    Frame clientNameFrame;
    // string input
    TextField clfT1;
    Label clfL2;

    public static void main(String[] args) {
        MultipleChatC c = new MultipleChatC();
        c.runClient();
    }

    public MultipleChatC() {
        // 채팅창 UI
        super("클라이언트");
        display = new TextArea("", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY);
        display.setEditable(false);
        add(display, BorderLayout.CENTER);
        Panel pword = new Panel(new BorderLayout());
        lword = new Label("대화말");
        text = new TextField(30);
        text.addActionListener(this);
        pword.add(lword, BorderLayout.WEST);
        pword.add(text, BorderLayout.EAST);
        add(pword, BorderLayout.SOUTH);
        addWindowListener(new WinListener());
        setSize(300, 300);
        setVisible(false);

        // 클라이언트 이름 정하기
        clientNameFrame = new Frame();
        // 컴포넌트
        Panel clfPanel = new Panel();
        Panel clfPanel2 = new Panel();
        Panel clfPanel3 = new Panel();
        Label clfL1 = new Label("아이디 입력 : ");
        clfL2 = new Label("사용할 아이디를 입력해주세요.");
        clfT1 = new TextField("", 10);
        // 커서 포커싱
        clfT1.requestFocus();
        Button b = new Button("확인");
        b.addActionListener(this);
        clfT1.addActionListener(this);

        clfPanel.add(clfL1);
        clfPanel.add(clfT1);
        clfPanel2.add(b);
        clfPanel3.add(clfL2);
        clientNameFrame.add(clfPanel, BorderLayout.NORTH);
        clientNameFrame.add(clfPanel3, BorderLayout.CENTER);
        clientNameFrame.add(clfPanel2, BorderLayout.SOUTH);
        
        clientNameFrame.setTitle("아이디 지정");
        clientNameFrame.setSize(300, 150);
        clientNameFrame.setVisible(true);
        clientNameFrame.addWindowListener(new WinListener());
    }

    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getActionCommand() == "확인" || e.getSource() == clfT1 ){
                clientName = clfT1.getText();
                output.write("id@"+clientName + "@\r\n");
                output.flush();
                System.out.println("id@"+clientName+"@");
                return;
            }
            clientData = text.getText();
            // 귓속말
            if (clientData.substring(0, 2).equals("/r")){
                String[] temp_data = clientData.split(" ");
                System.out.println("동작");
                int exceptLength = temp_data[0].length() + temp_data[1].length() + 2;
                output.write("private@" + temp_data[1] + "@" + clientData.substring(exceptLength) + "\r\n");
                output.flush();
                text.setText("");
                return;
            }
            // display.append("\r\n" + clientName + " : " + clientData); 
            output.write("allChat@" + clientName + "@" + clientData + "\r\n");
            output.flush();
            text.setText("");
        } catch (IOException ae) {
            //TODO: handle exception
            ae.printStackTrace();
        }    
    }

    public void runClient(){
        try {
            client = new Socket("192.168.0.4", 5000);
            input = new BufferedReader(new InputStreamReader(client.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            while(true){
                String serverData = input.readLine();
                // id false
                System.out.println(serverData);
                if( serverData.equals("#id-false")){
                    // code id false
                    clfL2.setText("다른 아이디를 선택해주세요.");
                    clfT1.setText("");
                    continue;
                } else if ( serverData.equals("#id-true")) {
                    // id usefully
                    clientNameFrame.setVisible(false);
                    this.setVisible(true);
                    continue;
                }
                if( serverData.equals("exitConnect")){
                    System.exit(0);
                }
                display.append("\r\n" + serverData);
            }
        } catch (Exception e) {
            //TODO: handle exception
            e.printStackTrace();
        }
    }

    class WinListener extends WindowAdapter {
        public void windowClosing(WindowEvent e){
            try {
                output.write("exitConnect@.@.\r\n");
                output.flush();
            } catch (IOException ioe) {
                //TODO: handle exception
                ioe.printStackTrace();
            }
        }
    }
}