/*This version is the GUI of my previous version.
Plus I added the error when the user with the same login have already been authorized.
Some improvements still can be made.
For example:
- The color of messages. It is difficult to find your own.

As last time I also attach the file where the DB is created and the db itself.

@author Zvereva Ekaterina
version dated 20.04.2017
*/
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;


class SimpleClient extends JFrame implements ActionListener, IConstants {

    final String TITLE_OF_PROGRAM = "Chat client";
    final String TITLE_BTN_ENTER = "Send";
    final int START_LOCATION = 200;
    final int WINDOW_WIDTH = 350;
    final int WINDOW_HEIGHT = 450;

    JTextArea dialogue; // area for dialog
    JTextField command;// field for entering commands
    JTextField login;
    JPasswordField password;
    JTextArea reasonOfFailedAuthorization;
    JPanel commonPanel;

    Socket socket;
    PrintWriter writer;
    BufferedReader reader;
    String message;
    volatile boolean authorized = false;

    public static void main(String[] args) {
        new SimpleClient();
    }

    /**
     * Constructor:
     * Creating a window and all the necessary elements on it
     */

    SimpleClient() {
        setTitle(TITLE_OF_PROGRAM);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBounds(START_LOCATION, START_LOCATION, WINDOW_WIDTH, WINDOW_HEIGHT);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                try {
                    writer.println(EXIT_COMMAND);
                    writer.flush();
                    socket.close();
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            }
        });
        commonPanel = new JPanel(new CardLayout());
        //Panel for authorization
        JPanel authorization = new JPanel();
        JLabel tool = new JLabel("Please, enter your login and password"); // info
        tool.setPreferredSize(new Dimension(250,50));
        reasonOfFailedAuthorization = new JTextArea(); // message about fail
        reasonOfFailedAuthorization.setPreferredSize(new Dimension(200,50));
        reasonOfFailedAuthorization.setEditable(false);
        reasonOfFailedAuthorization.setBackground(null);
        reasonOfFailedAuthorization.setFont(new Font("Arial", Font.BOLD, 16));
        login = new JTextField(LOGIN_PROMPT);
        login.setPreferredSize(new Dimension(200,50));
        //Listener for removing text from login field
        login.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                login.setText("");
                reasonOfFailedAuthorization.setText("");
            }
        });
        //Listener for key "Enter"
        login.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                password.grabFocus();
                password.setText("");
            }
        });
        password = new JPasswordField(PASSWD_PROMPT);
        password.setPreferredSize(new Dimension(200,50));
        //Listener for removing text from password field
        password.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                password.setText("");
                reasonOfFailedAuthorization.setText("");
            }
        });
        //Listener for key "Enter"
        password.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    writer.println(getLoginAndPassword()); // send authentication data
                    writer.flush();
                    Thread.sleep(500);
                    if (authorized) {
                        ((CardLayout) commonPanel.getLayout()).show(commonPanel, "chat");
                    }
                }
                catch  (Exception ex) {
                }
            }
        });
        //Enter the chat is possible either ways typing key "Enter" or clicking mouse on button
        JButton enterTheChat = new JButton("Enter");
        enterTheChat.setPreferredSize(new Dimension(200,50));
        enterTheChat.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    super.mouseClicked(e);
                    writer.println(getLoginAndPassword()); // send authentication data
                    writer.flush();
                    Thread.sleep(500);
                    if (authorized) {
                        ((CardLayout) commonPanel.getLayout()).show(commonPanel, "chat");
                    }
                }
                catch  (Exception ex) {
                }
            }
        });
        //add all elements to authorization panel
        authorization.add(tool);
        authorization.add (reasonOfFailedAuthorization);
        authorization.add(login);
        authorization.add (password);
        authorization.add(enterTheChat);
        commonPanel.add(authorization);
        //Panel for chat
        JPanel chat = new JPanel(new BorderLayout());
        dialogue = new JTextArea();
        dialogue.setLineWrap(true);
        dialogue.setEditable(false);
        JScrollPane scrollBar = new JScrollPane(dialogue);
        // panel for command field and button
        JPanel bp = new JPanel();
        bp.setLayout(new BoxLayout(bp, BoxLayout.X_AXIS));
        command = new JTextField();
        command.addActionListener(this);
        JButton send = new JButton(TITLE_BTN_ENTER);
        send.addActionListener(this);
        //add elements to bottom panel
        bp.add(command);
        bp.add(send);
        //add elements to chat panel
        chat.add(BorderLayout.CENTER, scrollBar);
        chat.add(BorderLayout.SOUTH, bp);
        commonPanel.add(chat, "chat");
        getContentPane().add(commonPanel);
        setVisible(true);
        // connect to server
        connect();
    }

    void connect() {
        try {
            socket = new Socket(SERVER_ADDR, SERVER_PORT);
            writer = new PrintWriter(socket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            new Thread(new ServerListener()).start();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
    //getLoginAndPassword: get login and password

    String getLoginAndPassword() {
        if ((login.getText().trim().length() > 0)) {
            return AUTH_SIGN +" "+ login.getText() + " " +  password.getText();
        }
        return AUTH_SIGN +"unknown";
    }

    // ServerListener: get messages from Server

    class ServerListener implements Runnable {
        @Override
        public void run() {
            try {
                while ((message = reader.readLine()) != null) {
                    if (!message.equals("\0")&& !message.equals(SUCCESS_AUTH)&&!message.equals(WRONG_PASSWD)
                            &&!message.equals(LOGIN_NOT_FOUND)&&!message.equals(LOGIN_IN_USE))
                        dialogue.append(message + "\n");
                    if ((message.equals(WRONG_PASSWD))||message.equals(LOGIN_NOT_FOUND))
                        authorized=false;
                        reasonOfFailedAuthorization.append(message);
                    if (message.equals(SUCCESS_AUTH)) {
                        authorized=true;
                    }
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
    //Listener for command panel and send button
    @Override
    public void actionPerformed(ActionEvent event) {
        if (command.getText().trim().length() > 0) {
            writer.println(command.getText());
            writer.flush();
            command.setText("");
        }
        command.requestFocusInWindow();
    }
}


