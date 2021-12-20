import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MyClient extends JFrame {
    private final String SERVER_ADDR = "127.0.0.1";
    private final int SERVER_PORT = 8189;
    private JTextField msgInputField;
    private JTextArea chatArea;
    private JTextField loginField;
    private JTextField passField;
    private JButton btnSendAuth;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Boolean authorized;

    public MyClient() {
        prepareGUI();
        try {
            openConnection();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void setAuthorized(Boolean bol){
        authorized = bol;
    }
    public void openConnection() throws IOException, InterruptedException {
        socket = new Socket(SERVER_ADDR, SERVER_PORT);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        setAuthorized(false);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String strFromServer = in.readUTF();
                        if(strFromServer.startsWith("/authok")) {
                            String[] parts = strFromServer.split("\\s");
                            setTitle(parts[1]);
                            setAuthorized(true);
                            btnSendAuth.setBackground(Color.GREEN);
                            btnSendAuth.setText("Вы в чате");
                            break;
                        }
                        chatArea.append(strFromServer + "\n");
                    }
                    while (true) {
                        String strFromServer = in.readUTF();
                        if (strFromServer.equalsIgnoreCase("/end")) {
                            break;
                        }
                        chatArea.append(strFromServer);
                        chatArea.append("\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public void onAuthClick() {
        try {
            if (!authorized) {
                out.writeUTF("/auth" + " " + loginField.getText() + " " + passField.getText());
                loginField.setText("");
                passField.setText("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sendMessage() {
        if (!msgInputField.getText().trim().isEmpty()) {
            try {
                out.writeUTF(msgInputField.getText());
                msgInputField.setText("");
                msgInputField.grabFocus();
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Ошибка отправки сообщения");
            }
        }
    }
    public void prepareGUI() {
// Параметры окна
        setBounds(600, 300, 500, 500);
        setTitle("Клиент");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
// Текстовое поле для вывода сообщений
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);
// Нижняя панель с полем для ввода сообщений и кнопкой отправки сообщений

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel upPanel = new JPanel();
        upPanel.setLayout(new BoxLayout(upPanel, BoxLayout.X_AXIS));

        JButton btnSendMsg = new JButton("Отправить");
        btnSendAuth = new JButton("Авторизоваться");
        btnSendAuth.setBackground(Color.GRAY);
        bottomPanel.add(btnSendMsg, BorderLayout.EAST);
        msgInputField = new JTextField();
        loginField = new JTextField();
        passField = new JTextField();

        loginField.setText("Login");
        passField.setText("Password");

        upPanel.add(loginField);
        upPanel.add(passField);

        upPanel.add(btnSendAuth);
        bottomPanel.add(msgInputField, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        add(upPanel, BorderLayout.NORTH);

        btnSendMsg.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        btnSendAuth.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAuthClick();
            }
        });
        msgInputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        loginField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
               loginField.setText("");
            }
        });

        passField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                passField.setText("");
            }
        });
// Настраиваем действие на закрытие окна
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    out.writeUTF("/end");
                    closeConnection();
                } catch (IOException exc) {
                    exc.printStackTrace();
                }
            }
        });
        setVisible(true);
    }

}