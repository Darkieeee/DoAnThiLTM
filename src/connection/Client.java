package connection;
import execute.RunClient;
import giaodien.Main;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
public class Client {
    private BufferedWriter out;
    private String nickname = null;
    private BufferedReader in;
    private final String hostname;
    private final int port;
    private Socket s;
    private boolean isConnected = false;
    private Thread listener = null;
    public Client()
    {
        hostname = "localhost";
        port = 5000;
    }
    public void runConfig() //Ket noi den server
    {
        try 
        {
            s = new Socket(hostname, port);
            out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            isConnected = true;
            listener = new Thread(() -> {
                try {
                    this.getMessage();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            listener.start();
        } catch(IOException ex){
            isConnected = false;
        }
    }
    private void sendMessage(String message){
        try {
            out.write(message);
            out.newLine();
            out.flush();
        } catch (IOException ex)
        {
            System.err.println("Không thể kết nối đến server");
        }
    }
    public void getMessage() throws InterruptedException
    {
        try{
            while(isConnected)
            {
                String line = in.readLine();
                System.out.println("> "+line);
                String[] parseLine = line.split(";");
                switch(parseLine[0])
                {
                    case "login" -> {
                        getLoginMessage(line);
                    }
                    case "finding" -> {
                        getFindingMessage(line);
                    }
                    case "match_found" -> {
                        getMatchFoundMessage(line);
                    }
                    case "result_match" -> {
                        getResultMatchMessage(line);
                    }
                    case "client_exited" -> {
                        getClientExitedMessage(line);
                    }
                    case "chat_item_status" -> {
                        getChatItemStatus(line);
                    }
                    case "received_chat" -> {
                        getReceivedChat(line);
                    }
                    case "logout" -> {
                        receiveLogout(line);
                    }
                }
            }
            in.close();
            out.close();
            s.close();
        } catch(IOException ex)
        {
            isConnected = false;
        }
    }
    
    public boolean isConnected()
    {
        return isConnected;
    }
    
    public  void findMatch() {
        sendMessage("find_match");
    }
    
    public void rejectMatch() {
        sendMessage("accepted_match;no");
        RunClient.main.setState(Main.State.DECLINED_MATCH);
    }
    
    public void acceptMatch() {
        sendMessage("accepted_match;yes");
        RunClient.main.setState(Main.State.ACCEPTED_MATCH);
    }
    
    public void login(String nickname) {
        if(!isConnected())
        {
            javax.swing.JOptionPane.showMessageDialog(RunClient.loginForm,
                                                      "Không thể kết nối đến server",
                                                      "Chương trình",
                                                      javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        sendMessage("login;"+nickname);
    }
    
    private void getLoginMessage (String line)
    {
        String[] splittedLine = line.split(";");
        int processLoginIndex = 2;
        if(splittedLine[processLoginIndex].equals("success"))
        {
            javax.swing.JOptionPane.showMessageDialog(RunClient.loginForm,
                                                      "Đăng nhập thành công",
                                                      "Chương trình",
                                                      javax.swing.JOptionPane.INFORMATION_MESSAGE);
            int nicknameIndex = 1;
            nickname = splittedLine[nicknameIndex];
            RunClient.openForm(RunClient.Form.MAIN);
            RunClient.main.setLocationRelativeTo(RunClient.loginForm);
            RunClient.main.setNickname(nickname);
            RunClient.main.setState(Main.State.IN_QUEUE);
            RunClient.client.findMatch();
            RunClient.closeForm(RunClient.Form.LOGIN);
        }
        else {
            javax.swing.JOptionPane.showMessageDialog(RunClient.loginForm,
                                                      "Nickname đã được sử dụng trước đó! Hãy thử một nickname khác xem",
                                                      "Chương trình",
                                                      javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void getFindingMessage (String line)
    {
        String[] splittedLine = line.split(";");
        int processFindingIndex = 1;
        //Còn tiếp tục tìm kiếm
        if(splittedLine[processFindingIndex].equals("true"))
        {
            RunClient.main.setState(Main.State.IN_QUEUE);
        }
        //Tắt chức năng tìm kiếm khi thấy người cần ghép`
        else {
            RunClient.main.setState(Main.State.MATCH_FOUND);
        }
    }
    
    private void getMatchFoundMessage (String line)
    {
        String[] splittedLine = line.split(";");
        RunClient.main.setState(Main.State.MATCH_FOUND);
        int nicknamePairedIndex = 1;
        int confirmed = javax.swing.JOptionPane.showConfirmDialog(RunClient.main,
                                                                  "Bạn có muốn ghép đôi với "+splittedLine[nicknamePairedIndex]+"?",
                                                                  "Chương trình",
                                                                  javax.swing.JOptionPane.YES_NO_OPTION);
        if(confirmed == javax.swing.JOptionPane.NO_OPTION || confirmed == javax.swing.JOptionPane.CLOSED_OPTION)
        {
            RunClient.main.setState(Main.State.DECLINED_MATCH);
            RunClient.client.rejectMatch();
        }
        else {
            RunClient.main.setState(Main.State.ACCEPTED_MATCH);
            RunClient.client.acceptMatch();
        }
    }
    
    private void getResultMatchMessage (String line)
    {
        String[] splittedLine = line.split(";");
        int processResultMatchIndex = 2;
        if(splittedLine[processResultMatchIndex].equals("fail"))
        {
            javax.swing.JOptionPane.showMessageDialog(RunClient.main,
                                                      splittedLine[3],
                                                      "Chương trình",
                                                      javax.swing.JOptionPane.INFORMATION_MESSAGE);
            //Tiếp tục quá trình ghép đôi mới
            RunClient.client.findMatch();
            RunClient.main.setState(Main.State.IN_QUEUE);
        }
        else {
            javax.swing.JOptionPane.showMessageDialog(RunClient.main,
                                                      "Ghép đôi thành công",
                                                      "Chương trình",
                                                      javax.swing.JOptionPane.INFORMATION_MESSAGE);
            RunClient.openForm(RunClient.Form.CHATBOX);
            RunClient.chatBox.setTitle(splittedLine[1]);
            RunClient.chatBox.setLocationRelativeTo(RunClient.main);
            RunClient.closeForm(RunClient.Form.MAIN);
        }
    }
    
    private void getChatItemStatus(String line){
        String[] splittedLine = line.split(";");
        String status = splittedLine[2]; //chat_item_status;chat_content;status[success or fail];curTime(if success)
        String chatContent = splittedLine[1];
        if(status.equals("fail")) {
            javax.swing.JOptionPane.showMessageDialog(RunClient.chatBox,
                                                      chatContent,
                                                      "Chương trình",
                                                      javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
        else {
            String curTime = String.format("[%s]", splittedLine[3]);
            String chatItem = String.format("%s %s: %s",curTime,"You sent",chatContent);
            RunClient.chatBox.addChatEvent(chatItem);
            RunClient.chatBox.clearMessage();
        }
    }
    
    private void getReceivedChat (String line)
    {
        String[] splittedLine = line.split(";");
        //received_chat;client_chat;curTime;chat_content
        String client_chat = splittedLine[1]; 
        String curTime = String.format("[%s]", splittedLine[2]);
        String chatContent = splittedLine[3];
        String chatItem = String.format("%s %s: %s",curTime,client_chat,chatContent);
        RunClient.chatBox.addChatEvent(chatItem);
    }
    
    private void getClientExitedMessage (String line)
    {
        String[] splittedLine = line.split(";");
        int nicknameIndex = 1;
        if(RunClient.getCurrentForm() == 2)
        {
            javax.swing.JOptionPane.showMessageDialog(RunClient.main,
                                                      splittedLine[nicknameIndex]+" đã thoát!!!",
                                                      "Chương trình",
                                                      javax.swing.JOptionPane.INFORMATION_MESSAGE);
            //Tiếp tục quá trình ghép đôi mới
            RunClient.openForm(RunClient.Form.MAIN);
            RunClient.main.setLocationRelativeTo(RunClient.chatBox);
            RunClient.closeForm(RunClient.Form.CHATBOX);
            RunClient.openForm(RunClient.Form.MAIN);
            RunClient.client.findMatch();
            RunClient.main.setState(Main.State.IN_QUEUE);
        }
        else if(RunClient.getCurrentForm() == 3)
        {
            javax.swing.JOptionPane.showMessageDialog(RunClient.chatBox,
                                                      splittedLine[nicknameIndex]+" đã thoát!!!",
                                                      "Chương trình",
                                                      javax.swing.JOptionPane.INFORMATION_MESSAGE);
            //Tiếp tục quá trình ghép đôi mới
            RunClient.openForm(RunClient.Form.MAIN);
            RunClient.main.setLocationRelativeTo(null);
            RunClient.main.setNickname(nickname);
            RunClient.closeForm(RunClient.Form.CHATBOX);
            RunClient.client.findMatch();
            RunClient.main.setState(Main.State.IN_QUEUE);
        }
    }
    
    private void receiveLogout(String line) {
        String[] splittedLine = line.split(";");
        if(splittedLine[2].equals("success")){
            RunClient.closeForm(RunClient.Form.MAIN);
        }
    }
    
    public void logout() {
        sendMessage("logout");
    }
    
    public void viewChatHistory()
    {
        
    }
    
    public void sendChat(String chatContent)
    {
        sendMessage("chat_item;"+chatContent);
    }
    
    public String getNickname(){
        return nickname;
    }
}
