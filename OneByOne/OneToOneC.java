import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;

public class OneToOneC extends Frame implements ActionListener {
    TextArea display;
    TextField text;
    Label lword;
    BufferedWriter output;
    BufferedReader input;
    Socket client;
    String clientData = "";
    String serverData = "";

    public OneToOneC(){
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

        setSize(300, 200);
        setVisible(true);
    }

    public void runClient(){
        try {
            client = new Socket(InetAddress.getLocalHost(), 5000); 
            input = new BufferedReader(new InputStreamReader(client.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            while(true){
                String serverData = input.readLine();
                if( serverData.equals("quit") ){
                    display.append("\n서버와의 접속이 중단되었습니다.");
                    output.flush();
                    break;
                } else {
                    display.append("\n서버 메시지 : " + serverData);
                    output.flush();
                }
            }
        } catch (IOException e) {
            //TODO: handle exception
            e.printStackTrace(); 
        }
    }

    public void actionPerformed(ActionEvent ae){
        clientData = text.getText();
        try {
            display.append("\n 클라이언트 : " + clientData);
            output.write(clientData + "\r\n");
            output.flush();
            text.setText("");
            if ( clientData.equals("quit") ){
                client.close();
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        OneToOneC c = new OneToOneC();
        c.runClient();
    }    
    class WinListener extends WindowAdapter {
        public void WindowClosing(WindowEvent e){
            System.exit(0);
        }
    }
}