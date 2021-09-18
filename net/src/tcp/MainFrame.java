package tcp;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;

/**
 * 测试代码
 */
public class MainFrame extends JFrame{
    public MainFrame() {
        this.setSize(1280, 768);
        getContentPane().setLayout(null);

        JButton btnNewButton = new JButton("传输文件");    //点击按钮进行文件传输
        btnNewButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // TODO 自动生成的方法存根
                super.mouseClicked(e);
                JFileChooser fileChooser = new JFileChooser();    //fileChooser用来选择要传输的文件
                fileChooser.setDialogTitle("选择要传输的文件");
                int stFile = fileChooser.showOpenDialog(null);
                if(stFile == fileChooser.APPROVE_OPTION){    //选择了文件
                    JFileChooser targetPathChooser = new JFileChooser();    //targetPathChooser用来选择目标路径
                    targetPathChooser.setDialogTitle("选择目标路径");
                    targetPathChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);    //只能选择路径
                    int stPath = targetPathChooser.showOpenDialog(null);
                    if(stPath == targetPathChooser.APPROVE_OPTION) {    //选择了路径
                        //新建一个线程实例化客户端
                        new Thread(new NewClient( fileChooser.getSelectedFile().getPath(), targetPathChooser.getSelectedFile().getPath())).start();
                    }
                }
            }
        });
        btnNewButton.setBounds(526, 264, 237, 126);
        getContentPane().add(btnNewButton);
    }
    class NewClient implements Runnable {    //用于实例化客户端的线程
        private String fileP;    //需复制文件路径
        private String targetP;    //目标路径
        public NewClient(String fileP, String targetP) {    //构造函数
            this.fileP = fileP;
            this.targetP = targetP;
        }
        @Override
        public void run() {
            // TODO 自动生成的方法存根
            try {
                @SuppressWarnings("resource")
                FileTransferClient ftc = new FileTransferClient();
                //实例化客户端
                ftc.sendFile(fileP, targetP);
            } catch (Exception e1) {
                // TODO 自动生成的 catch 块
                e1.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
        // TODO 自动生成的方法存根
        MainFrame mainFrame = new MainFrame();
        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try {
            @SuppressWarnings("resource")
            FileTransferServer server = new FileTransferServer(); // 启动服务端
            server.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}