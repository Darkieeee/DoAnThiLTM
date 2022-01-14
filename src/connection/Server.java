package connection;
import giaodien.ChatBox;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class Server {
    private static ServerSocket s;
    public static ArrayList<ServerTCP> dsClient = new ArrayList<>();
    public static ArrayList<ChatBox> dsChatBox = new ArrayList<>();
    private final static int port = 5000;
    public final static ExecutorService ex = Executors.newCachedThreadPool();
    public static boolean isRunning = false;
    public static void main(String... args)
    {
        try{
            s = new ServerSocket(port);
            isRunning = true;
            System.out.println("Server đang kết nối");
            System.out.println("Chờ các client khác tham gia");
            while(isRunning)
            {
                Socket socket = s.accept();
                ServerTCP client = new ServerTCP(null,socket);
                ex.submit(client);
            }
        }
        catch(IOException ex1)
        {
            isRunning = false;
            ex.shutdownNow();
            System.err.println("Port đang được sử dụng");
        }
    }
}