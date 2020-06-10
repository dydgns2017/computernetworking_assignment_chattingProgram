import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class chatClient {
    public static void main(String[] args){
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                try {
                    UIManager.setLookAndFeel(UIManager
                            .getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                MainGUI mainGUI = new MainGUI();
                mainGUI.preDisplay();
            }
        });
        chatClient.runClient();
    }

    static Socket connection;
    static BufferedReader input;
    static BufferedWriter output;
    static String outputData;
    public static void runClient() {
        try {
            Socket client = new Socket(InetAddress.getLocalHost(), 20170);
            input = new BufferedReader(new InputStreamReader(client.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            while(true){
                String serverdata = input.readLine();
                if(serverdata.equals("quit")){
                    MainGUI.chatBox.append(serverdata);
                    System.out.println(serverdata);
                    output.flush();
                    break;
                }
                else {
                    MainGUI.chatBox.append(serverdata);
                    System.out.println(serverdata);
                    output.flush();
                }
            }
            client.close();
        } catch (IOException e) {
            //TODO: handle exception
            System.err.println(e);
        }
    }
}