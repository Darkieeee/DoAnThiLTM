package execute;

import connection.Client;
import giaodien.*;
public class RunClient {
    // Idea: Caro Socket Online by Hoang Tran
    public static Client client;
    public static Login loginForm;
    public static Main main;
    public static ChatBox chatBox;
    private static int curForm;
    public enum Form {
        LOGIN,
        MAIN,
        CHATBOX
    }
    public RunClient() {
        client = new Client();
        client.runConfig();
        initialize();    
    }
    private void initialize() {
        loginForm = new Login();
        main = new Main();
        chatBox = new ChatBox();
        openForm(Form.LOGIN);
        curForm = 1;
    }
    public static void openForm (Form form)
    {
        if(form == null)
        {
            return;
        }
        switch(form)
        {
            case LOGIN -> {
                loginForm = new Login();
                loginForm.setVisible(true);
                curForm = 1;
            }
            case MAIN -> {
                main = new Main();
                main.setVisible(true);
                curForm = 2;
            }
            case CHATBOX -> {
                chatBox = new ChatBox();
                chatBox.setVisible(true);
                curForm = 3;
            }
        }
    }
    public static void closeForm (Form form)
    {
        if(form == null)
        {
            return;
        }
        switch(form) {
            case LOGIN -> {
                loginForm.dispose();
            }
            case MAIN -> {
                main.dispose();
            }
            case CHATBOX -> {
                chatBox.dispose();
            }
        }
    }
    public static int getCurrentForm()
    {
        return curForm;
    }
    public static void main(String... args)
    {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ChatBox.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new RunClient();
        });
    }
}
