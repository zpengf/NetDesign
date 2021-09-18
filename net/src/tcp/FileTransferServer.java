package tcp;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * 服务器代码
 */
public class FileTransferServer extends ServerSocket {

    private static final int SERVER_PORT = 8899; // 服务端端口

    public FileTransferServer() throws Exception {
        super(SERVER_PORT);
    }

    public void load() throws Exception {
        while (true) {
            // server尝试接收其他Socket的连接请求，server的accept方法是阻塞式的
            Socket socket = this.accept();

            // 每接收到一个Socket就建立一个新的线程来处理它
            new Thread(new Task(socket)).start();
        }
    }
    //处理客户端传输过来的文件线程类
    class Task implements Runnable {

        private Socket socket;
        private DataInputStream dis;
        private DataOutputStream dos;
        private RandomAccessFile rad;
        private JFrame frame;    //用来显示进度条
        private Container contentPanel;
        private JProgressBar progressbar;
        private JLabel label;

        public Task(Socket socket) {
            frame = new JFrame("文件传输");
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
                String targetPath = dis.readUTF();    //接收目标路径
                String fileName = dis.readUTF();    //接收文件名
                //System.out.println("服务器：接收文件名");
                long fileLength = dis.readLong();    //接收文件长度
                //System.out.println("服务器：接收文件长度");
                File directory = new File(targetPath);    //目标地址
                if(!directory.exists()) {    //目标地址文件夹不存在则创建该文件夹
                    directory.mkdir();
                }
                File file = new File(directory.getAbsolutePath() + File.separatorChar + fileName + ".temp");    //建立临时数据文件.temp
                //System.out.println("服务器：加载temp文件");
                rad = new RandomAccessFile(directory.getAbsolutePath() + File.separatorChar + fileName + ".temp", "rw");
                long size = 0;
                if(file.exists() && file.isFile()){    //如果目标路径存在且是文件，则获取文件大小
                    size = file.length();
                }
                //System.out.println("服务器：获的当前已接收长度");
                dos.writeLong(size);    //向客户端发送当前数据文件大小
                dos.flush();
                //System.out.println("服务器：发送当前以接收文件长度");
                int barSize = (int)(fileLength / 1024);    //进度条当前进度
                int barOffset = (int)(size / 1024);        //进度条总长
                frame.setSize(300,120); //传输界面
                contentPanel = frame.getContentPane();
                contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
                progressbar = new JProgressBar();    //进度条
                label = new JLabel(fileName + " 接收中");
                contentPanel.add(label);
                progressbar.setOrientation(JProgressBar.HORIZONTAL);    //进度条为水平
                progressbar.setMinimum(0);    //进度条最小值
                progressbar.setMaximum(barSize);    //进度条最大值
                progressbar.setValue(barOffset);    //进度条当前值
                progressbar.setStringPainted(true); //显示进度条信息
                progressbar.setPreferredSize(new Dimension(150, 20));    //进度条大小
                progressbar.setBorderPainted(true);    //为进度条绘制边框
                progressbar.setBackground(Color.pink);    //进度条颜色为骚粉
                JButton cancel = new JButton("取消");    //取消按钮
                JPanel barPanel = new JPanel();
                barPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
                barPanel.add(progressbar);
                barPanel.add(cancel);
                contentPanel.add(barPanel);
                cancel.addActionListener(new cancelActionListener());
                //为取消按钮注册监听器
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
                rad.seek(size);    //移动文件指针
                //System.out.println("服务器：文件定位完成");
                int length;
                byte[] bytes=new byte[1024];
                while((length = dis.read(bytes, 0, bytes.length)) != -1){
                    rad.write(bytes,0, length);    //写入文件
                    progressbar.setValue(++barOffset);    //更新进度条（由于进度条每个单位代表大小为1kb，所以太小的文件就显示不出啦）
                }
                if (barOffset >= barSize) {    //传输完成后的重命名
                    if(rad != null)
                        rad.close();
                    if(!file.renameTo(new File(directory.getAbsolutePath() + File.separatorChar + fileName))) {
                        file.delete();
                        //防御性处理删除临时文件
                    }
                    //System.out.println("服务器：临时文件重命名完成");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {    //关闭资源
                    if(rad != null)
                        rad.close();
                    if(dis != null)
                        dis.close();
                    if(dos != null)
                        dos.close();
                    frame.dispose();
                    socket.close();
                } catch (Exception e) {}
            }
        }
        class cancelActionListener implements ActionListener{    //取消按钮监听器
            public void actionPerformed(ActionEvent e){
                try {
                    //System.out.println("服务器：接收取消");
                    if(dis != null)
                        dis.close();
                    if(dos != null)
                        dos.close();
                    if(rad != null)
                        rad.close();
                    frame.dispose();
                    socket.close();
                    JOptionPane.showMessageDialog(frame, "已取消接收，连接关闭！", "提示：", JOptionPane.INFORMATION_MESSAGE);
                    label.setText(" 取消接收,连接关闭");
                } catch (IOException e1) {

                }
            }
        }
    }
}