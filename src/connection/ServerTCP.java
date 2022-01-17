package connection;
import extension.FormatDate;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
public class ServerTCP implements Runnable{
    private BufferedWriter out;
    private BufferedReader in;
    private Socket s = null;
    private String name;
    ServerTCP friendMatch = null;
    String accepted_match = "_";
    boolean finding_match = false;
    ArrayList<String> rejectNames = new ArrayList<>(); //Lưu tên nickname người dùng từ chối chat
    ArrayList<String> historyChat = new ArrayList<>();
    private boolean running = false;
    
    private ServerTCP () //null
    {
        
    }
    
    public ServerTCP (String name, Socket s)
    {
        try {
            this.name = name;
            this.s = s;
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            running = true;
        } catch (IOException ex){
            System.err.println(ex);
        }
    }
    
    @Override
    public void run() {
        try{
            while(running)
            {
                String line = in.readLine();
                String[] parseLine = line.split(";");
                if(parseLine.length == 0 || parseLine.equals(new String("")) || parseLine[0].equals(""))
                {
                    out.write("Null String Found!!!");
                    out.newLine();
                    out.flush();
                }
                try {
                    switch(parseLine[0])
                    {
                        case "login" -> {
                            login(line);
                        }
                        case "find_match" -> {
                            findMatch();
                        }
                        case "accepted_match" -> {
                            acceptMatch(line);
                        }
                        case "chat_item" -> {
                            sendChatItem(line);
                        }
                        case "logout" -> {
                            logout(line);
                        }
                        case "viewchathistory" -> {
                            sendChatHistory();
                        }
                    }
                } catch(IndexOutOfBoundsException ex) {
                    out.write("Sai cú pháp");
                    out.newLine();
                    out.flush();
                }
                
            }
        } catch(IOException ex)
        {
            try {
                if(friendMatch.isEmpty()) {
                } else {
                    friendMatch.sendData("client_exited;"+name);
                    friendMatch.unpair();
                }
            }
            catch(IOException ex1)
            {
                System.err.println(ex1);
            }
            finally {
                if(!name.isBlank()) {
                    System.out.println(name+" đã thoát");
                    historyChat.clear();
                }
                Server.dsClient.remove(this);
                closeSocket();
            }
        }
    }
    
    /*  Login   */
    private void login(String line) throws IOException
    {
        String[] parseLine = line.split(";");
        if(parseLine[1].equals(""))
        {
            out.write("Vui lòng nhập nickname để tham gia");
            out.newLine();
            out.flush();
            return;
        }
        boolean isSignedUp = false;
        for(ServerTCP client: Server.dsClient)
        {
            if(parseLine[1].equals(client.getName()))
            {
                isSignedUp = true;
            }
        }
        if(isSignedUp)
        {
            sendData("login;"+parseLine[1]+";fail");
        } else {
            sendData("login;"+parseLine[1]+";success");
            name = parseLine[1];
            Server.dsClient.add(this);
            System.out.println(name+" đã tham gia");
        }
    }
    
    /*  Find match  */
    private void findMatch() throws IOException
    {
        // Q/A: https://www.spigotmc.org/threads/getting-random-player-from-arraylist.143977/ 
        // Sequence random
        ServerTCP randomClient = ClientManager.findRandomMatch(this);
            
        // if the random client was not in reject list and they are still finding pair
        if(randomClient != null)
        {   
            // toggle off the status of finding pair of two
            this.setFindingMatch(false);
            randomClient.setFindingMatch(false);
                
            // pair two clients
            randomClient.friendMatch = this;
            friendMatch = randomClient;
                
            // notify both of two
            this.sendData("finding;false");
            randomClient.sendData("finding;false");
            
            sendData("match_found;"+randomClient.getName());
            randomClient.sendData("match_found;"+name);
            
        }
        else {
            // notify to this client and continue to finding
            sendData("finding;true");
            finding_match = true;
        }
    }
    
    private void acceptMatch(String line) throws IOException
    {
        String[] parseLine = line.split(";"); //accepted_match;[option] (yes or no)
        accepted_match = parseLine[1];
        if(!friendMatch.accepted_match.equals("_"))
        {
            if(accepted_match.equals("yes") && friendMatch.accepted_match.equals("yes"))
            {
                sendData("result_match;"+friendMatch.getName()+";success");
                friendMatch.sendData("result_match;"+name+";success");

                //reset option
                friendMatch.accepted_match = "_";
                accepted_match = "_";
            }
            else
            {
                if(accepted_match.equals("yes") && friendMatch.accepted_match.equals("no"))
                {
                    //Notify
                    sendData("result_match;"+name+";fail;Người dùng "+friendMatch.getName()+" đã từ chối ghép đôi với bạn");
                    friendMatch.sendData("result_match;"+friendMatch.getName()+";fail;Bạn đã từ chối ghép đôi với người dùng "+name);
                }
                else if (accepted_match.equals("no") && friendMatch.accepted_match.equals("yes"))
                {
                    //Notify
                    friendMatch.sendData("result_match;"+name+";fail;Người dùng "+name+" đã từ chối ghép đôi với bạn");
                    sendData("result_match;"+friendMatch.getName()+";fail;Bạn đã từ chối ghép đôi với người dùng "+friendMatch.getName());
                }
                else {
                    //Notify
                    friendMatch.sendData("result_match;"+name+";fail;Cả hai bạn đã từ chối ghép đôi");
                    sendData("result_match;"+friendMatch.getName()+";fail;Cả hai bạn đã từ chối ghép đôi");
                }
                                    
                //Move two clients nicknames to the both reject list
                rejectNames.add(friendMatch.getName());
                friendMatch.rejectNames.add(name);
                                    
                //Reset option
                friendMatch.accepted_match = "_";
                accepted_match = "_";
                                    
                //Unpair two clients
                this.unpair();
                friendMatch.unpair();
            }
        }
    }
    
    private void sendChatItem (String line) throws IOException
    {
        String[] parseLine = line.split(";");
        String chatContent = parseLine[1];
        if(friendMatch.isEmpty())
        {
            sendData("chat_item_status;"+chatContent+"fail;Không tìm thấy người nhận");
            return;
        }
        String curTime = FormatDate.getCurrent(FormatDate.STANDARD_TIME);
        sendData("chat_item_status;"+chatContent+";success;"+curTime);
        friendMatch.sendData("received_chat;"+name+";"+curTime+";"+chatContent);
        historyChat.add("["+curTime+"] You sent to "+friendMatch.name+": "+chatContent);
    }
    
    private void logout(String line)
    {
        try {
            sendData(line+";"+name+";success");
            running = false;
        } catch (IOException ex)
        {
            System.err.println(ex);
        }
    }
    public boolean isFindingMatch()
    {
        return finding_match;
    }
    
    public void setFindingMatch(boolean finding)
    {
        this.finding_match = finding;
    }
    
    private void sendData(String message) throws IOException
    {
        out.write(message);
        out.newLine();
        out.flush();
    }

    public boolean isEmpty() {
        return (this.equals(new ServerTCP()) || this == null);
    }
    
    public void closeSocket() {
        try{
            in.close();
            out.close();
            s.close();
        } catch(IOException ex)
        {
            System.err.println("Lỗi đóng socket");
        }
    }
    
    /* Unpair friend */
    public void unpair()
    {
        friendMatch = new ServerTCP();
    }
    
    public String getName(){
        return name;
    }

    private void sendChatHistory() throws IOException{
        if(!historyChat.isEmpty()){
            String chatHistory = "";
            for(int i=0;i<historyChat.size();i++)
            {
                chatHistory += historyChat.get(i);
                if(i < historyChat.size()-1)
                {
                    chatHistory += ",";
                }
            }
            sendData("result_chat_history"+";success;"+chatHistory);            
        }
        else{
            sendData("result_chat_history"+";not_found_history");
        }
    }
 
}
