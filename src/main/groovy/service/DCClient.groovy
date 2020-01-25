package service

import tool.DBHelper

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

public class DCClient {

    private JFrame frame;
    private JTextField textField_1;
    private JPasswordField passwordField;

    static def args
    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        this.args = args
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
                String password = new String(passwordField.getPassword())
                String sql = """select 云桌面命令 from va2 where vano='${id}' and 
vapassword = '${password}'"""
//                println(sql)
                passwordField.setText("")

                try{
                    String cmd = new DBHelper(args[0], args[1], args[2]).query(sql)[0][0]
                    Runtime.getRuntime().exec(cmd)
                }catch(Exception ee) {
                    def msg = ""
                    switch (ee.getClass().toString()) {
                        case "class java.lang.NullPointerException":
                            msg = "密码错误"
                            break
                        case "class org.postgresql.util.PSQLException":
                            msg = "数据库连接错误"
                            break
                    }
                    JOptionPane.showMessageDialog(null, msg+ee.toString(),"提示" ,  JOptionPane.INFORMATION_MESSAGE)
                }
            }
        })


    }
}
