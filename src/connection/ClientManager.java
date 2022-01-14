package connection;
public class ClientManager {
    //Tìm ngẫu nhiên theo thứ tự
    public static ServerTCP findRandomMatch(ServerTCP target) {
        for(ServerTCP client: Server.dsClient)
        {
            if(client != target && !target.rejectNames.contains(client.getName()))
            {
                if (client.isFindingMatch()) {
                    return client;
                }
            }
        }
        return null;
    }
    public static ServerTCP findClient(String nickname)
    {
        for(ServerTCP client: Server.dsClient)
        {
            if(client.getName().equals(nickname))
            {
                return client;
            }
        }
        return null;
    }
}
