package service

import tool.DBHelper
import tool.DBHelper2

import javax.swing.Box
import javax.swing.JComponent
import javax.swing.JOptionPane
import javax.swing.KeyStroke;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import javax.swing.JButton;
import java.awt.FlowLayout;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JPasswordField;
import java.awt.Font
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent;

class DCClient {

    private JFrame frame;
    private JTextField textField_1;
    private JPasswordField passwordField;
    static String[] args
    /**
     * Launch the application.
     */
    public static void main(String[] args) {

        args =  ['192.168.0.149','postgres','ruianVA123'] as String[] //dev
        DCClient.args=args

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    DCClient window = new DCClient();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public DCClient() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 546, 426);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        JButton btnOk = new JButton("ok");
        btnOk.registerKeyboardAction(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                btnOk.doClick();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        btnOk.setBounds(206, 269, 51, 29);
        frame.getContentPane().add(btnOk);

        JLabel lblNewLabel_1 = new JLabel("id");
        lblNewLabel_1.setBounds(82, 110, 81, 21);
        frame.getContentPane().add(lblNewLabel_1);

        textField_1 = new JTextField();
        textField_1.setFont(new Font("Tahoma", Font.PLAIN, 25));
        textField_1.setColumns(10);
        textField_1.setBounds(192, 107, 154, 27);
        frame.getContentPane().add(textField_1);

        JLabel lblPassword = new JLabel("password");
        lblPassword.setBounds(82, 174, 81, 21);
        frame.getContentPane().add(lblPassword);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Tahoma", Font.PLAIN, 25));
        passwordField.setBounds(192, 167, 154, 27);
        frame.getContentPane().add(passwordField);
        btnOk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                println(textField_1.getText())
                String id = textField_1.getText()
                String os = System.getProperty("os.name")

                String password = new String(passwordField.getPassword())
                String sql = """select 云桌面命令,云桌面命令2 from va2 where vano='${id}' and 
vapassword = '${password}'"""
                println(sql)
                passwordField.setText("")

                try{
                    println(DCClient)
                    String cmd = new DBHelper2(DCClient.args[0], DCClient.args[1], DCClient.args[2]).query(sql)[0][0]
                    //若是linux
                    if( os!= null && os.toLowerCase().startsWith("windows")){
                        println("windows:"+cmd)
                        def ip = (cmd =~  /\/u:(.*?)\s/)[0][1]
                        def id1 = (cmd =~  /\/p:(.*?)\s/)[0][1]
                        def passwd1 = (cmd =~  /\/v:(.*?)\s/)[0][1]
                        println("${ip} ${id1} ${passwd1}")
                        //导出rdp配置文件

                    }else{
                        println("linux:"+cmd)
                    }
                    //若是windows
                    Runtime.getRuntime().exec(cmd)
                }catch(Exception ee) {
                    def msg = ""
                    switch (ee.getClass().toString()) {
                        case "class java.lang.NullPointerException":
                            msg = "密码错误"
                            break
                        case "class org.postgresql.util.PSQLException":
                            msg = "网络故障，请查看网线是否未连接"
                            break
                    }
                    JOptionPane.showMessageDialog(null, msg+ee.toString(),"提示" ,  JOptionPane.INFORMATION_MESSAGE)
                }
            }
        })


    }
}
