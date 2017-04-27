/*This version is the GUI of my previous version.
Plus I added the error when the user with the same login have already been authorized.
Some improvements still can be made.
For example:
- The color of messages. It is difficult to find your own.

As last time I also attach the file where the DB is created and the db itself.

@author Zvereva Ekaterina
version dated 20.04.2017
*/
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

class SimpleServer implements IConstants {

    private ArrayList <ClientHandler> activeClients = new ArrayList<>();
    int client_count = 0;
    ServerSocket server;
    Socket socket;

    public static void main(String[] args) {
        new SimpleServer();
    }

    SimpleServer() {
        System.out.println(SERVER_START);
        new Thread(new CommandHandler()).start();
        try {
            server = new ServerSocket(SERVER_PORT);
            while (true) {
                socket = server.accept();
                client_count++;
                System.out.println("#" + client_count + CLIENT_JOINED);
                ClientHandler client = new ClientHandler(socket, this);
                activeClients.add(client);
                new Thread(client).start();
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        System.out.println(SERVER_STOP);
    }

    /**
     * checkAuthentication: check login and password
     */
    private boolean checkAuthentication(String login, String passwd) {
        Connection connect;
        boolean result = false;
        try {
            // connect db
            Class.forName(DRIVER_NAME);
            connect = DriverManager.getConnection(SQLITE_DB);
            // looking for login && passwd in db
            Statement stmt = connect.createStatement();
            ResultSet rs = stmt.executeQuery(SQL_SELECT.replace("?", login));
            while (rs.next())
                result = rs.getString(PASSWD_COL).equals(passwd);
            // close all
            rs.close();
            stmt.close();
            connect.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return result;
    }
    private String findTheReasonOfFailedAuthorization (String login, String password) {
        Connection connect;
        String reason = "unknown reason";
        boolean result=false;
        try {
            // connect db
            Class.forName(DRIVER_NAME);
            connect = DriverManager.getConnection(SQLITE_DB);
            // looking for login && passwd in db
            Statement stmt = connect.createStatement();
            ResultSet rs = stmt.executeQuery(SQL_SELECT.replace("?", login));
            if (rs.isClosed()) {
                return LOGIN_NOT_FOUND;
            }
            while (rs.next())
                result = rs.getString(PASSWD_COL).equals(password);
            if (!result) {
                return WRONG_PASSWD;
            }
            // close all
            rs.close();
            stmt.close();
            connect.close();
        } catch (Exception ex) {
            ex.printStackTrace();

        }
        return reason;
    }
    public void sendToAll (String message) {
        for (ClientHandler client: activeClients) {
            if (!(client.name.equalsIgnoreCase("")))
            client.sendMessage(message);
        }
    }
    public boolean findEqualNames (String name) {
        for (ClientHandler client: activeClients) {
            if (client.name.equals(name))
                return true;
        }
        return false;
    }


    /**
     * CommandHandler: processing of commands from server console
     */
    class CommandHandler implements Runnable {
        Scanner scanner = new Scanner(System.in);

        @Override
        public void run() {
            String command;
            do
                command = scanner.nextLine();
            while (!command.equals(EXIT_COMMAND));
            try {
                server.close();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    /**
     * ClientHandler: service requests of clients
     */
    class ClientHandler implements Runnable {
        SimpleServer server;
        BufferedReader reader;
        PrintWriter writer;
        Socket socket;
        String name;

        ClientHandler(Socket clientSocket, SimpleServer server) {
            try {
                socket = clientSocket;
                this.server=server;
                reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream());
                name = "";
            } catch(Exception ex) {
                System.out.println(ex.getMessage());
            }
        }

        @Override
        public void run() {
            String message;
            try {
                do {
                    message = reader.readLine();
                    if (message != null) {
                        System.out.println(name + ": " + message);
                        if (message.startsWith(AUTH_SIGN)) {
                            String[] wds = message.split(" ");
                            if (checkAuthentication(wds[1], wds[2])) {
                                if (!findEqualNames(wds[1])){
                                    name = wds[1];
                                    sendMessage(SUCCESS_AUTH);
                                    server.sendToAll( name +" joined the chat");
                                    server.sendToAll("\0");
                                }
                                else {
                                    sendMessage(LOGIN_IN_USE);
                                }

                            } else {
                                System.out.println(name + ": " + AUTH_FAIL);
                                writer.println(findTheReasonOfFailedAuthorization(wds [1], wds [2]));
                                writer.flush();
                            }
                        } else if (!message.equalsIgnoreCase(EXIT_COMMAND)) {
                            server.sendToAll(name+ ": " + message);
                            server.sendToAll("\0");
                        }

                    }
                } while (!message.equalsIgnoreCase(EXIT_COMMAND));
                server.sendToAll(name + " exit the chat");
                activeClients.remove(this);
                socket.close();
                System.out.println(name + CLIENT_DISCONNECTED);
            } catch(Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
        public void sendMessage (String message) {
            writer.println(message);
            writer.flush();
        }
    }
}