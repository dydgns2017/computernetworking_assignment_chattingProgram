import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class chatServer {
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
        chatServer.runServer();
    }

    static Socket connection;
    static BufferedReader input;
    static BufferedWriter output;
    static String return_ClientData;
    static String server_OutputData;
    public static void runServer() {
        ServerSocket server;
        try {
            server = new ServerSocket(20170, 100);
            System.out.println("wating for client");
            connection = server.accept();  // wating for Client
            System.out.println("ok");
            InputStream is = connection.getInputStream(); // wait client data ( store )
            InputStreamReader isr = new InputStreamReader(is); // and then store data
            input = new BufferedReader(isr); // read
            OutputStream os = connection.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            output = new BufferedWriter(osw);
            while(true){
                return_ClientData = input.readLine();
                output.write(server_OutputData);
                if (return_ClientData.equals("quit")){
                    output.flush();
                    break;
                } else if( !return_ClientData.equals("") && !server_OutputData.equals("") ) {
                    output.flush();
                    if ( !return_ClientData.equals("") ){
                        // get data from client
                        MainGUI.chatBox.append(input.readLine());
                    }
                    return_ClientData = "";
                    server_OutputData = "";
                }
            }
            connection.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}