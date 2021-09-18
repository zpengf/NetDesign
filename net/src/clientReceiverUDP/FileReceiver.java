package clientReceiverUDP;

import java.awt.*;
        import java.awt.event.ActionEvent;
        import java.awt.event.ActionListener;
        import java.io.*;
        import java.net.*;
        import javax.swing.*;
public class FileReceiver {
    static final int receivePort = 10000;
    DatagramPacket receiveDp = null;
    DatagramSocket receiveDs = null;
    int dataLen = 10240;
    public byte[] inBuff = new byte[dataLen];
    String filePath = null;
    InetAddress clientIp = null;
    String myUserId = "小富";

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        FileReceiver fr = new FileReceiver();
        fr.fileReceiver();
    }

    public void fileReceiver() {
        try {
            // 写一个TCP接收协议，判断是否接受对方发过来的信息
            ServerSocket ss = new ServerSocket(9999);
            // 阻塞，等待接收
            Socket s = ss.accept();
            PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
            // 读取客户端信息
            InputStreamReader isr = new InputStreamReader(s.getInputStream());
            BufferedReader br = new BufferedReader(isr);
            // 阻塞，等待接收从缓存中读取
            String fileInfo = br.readLine();
            String headInfomation[] = fileInfo.split("/");
            String fileName = headInfomation[0];
            String fileLen0 = headInfomation[1];
            System.out.println("tcp接受到的内容为=" + headInfomation[0]);
            System.out.println("tcp接受到的内容为=" + headInfomation[1]);
            int fileLen = Integer.parseInt(fileLen0);
            // 显示面板，显示对方发过来的文件信息，文件名称及文件大小，并确定是否收文件
            ReceiveConfirm rc = new ReceiveConfirm(myUserId, fileName, fileLen);
            String wait = rc.getLocationpath();
            // 等待存储文件的路径的产生
            while (wait.equals("wait")) {
                wait = rc.getLocationpath();
                System.out.println("我在这儿等待接收存储方的文件目录");
            }
            String headInfo = "YES";
            pw.println(headInfo);
            ss.close();
            String filePath = rc.getLocationpath();
            System.out.println("保存文件到目录" + fileInfo);
            DataOutputStream fileOut = new DataOutputStream(
                    new BufferedOutputStream(new FileOutputStream(filePath)));
            receiveDs = new DatagramSocket(receivePort);
            System.out.println("我是文件接收器1，我已经运行");
            int times = fileLen / dataLen;// 循环接收的次数
            int restSize = fileLen % dataLen;// 接收剩下的字节
            for (int i = 0; i < times; i++) {
                System.out.println("服务器已启动");
                receiveDp = new DatagramPacket(inBuff, inBuff.length);
                receiveDs.receive(receiveDp);
                fileOut.write(inBuff, 0, receiveDp.getLength());
                fileOut.flush();
            }
            // 接收最后剩下，在inBuffer中能存下。
            if (restSize != 0) {
                System.out.println("我有剩余");
                receiveDp = new DatagramPacket(inBuff, inBuff.length);
                receiveDs.receive(receiveDp);
                fileOut.write(inBuff, 0, receiveDp.getLength());
                fileOut.flush();
                fileOut.close();
            }
            System.out.println("接收完毕" + fileLen);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            if (receiveDs != null) {
                // 关闭receiveDs的对象
                receiveDs.close();
            }
            JOptionPane.showMessageDialog(null,
                    "发送信息异常，请确认10000(接收端)号端口空闲，且网络连接正常", "网络异常",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}

class ReceiveConfirm implements ActionListener {
    // dingyi
    JLabel jl;
    JButton jb1, jb2;
    JPanel jp1, jp2;
    String headInfo = null;
    String myUserId = null;
    int fileLen = 0;
    float result = 0f;
    JFrame jf = null;
    private static String locationpath = "wait";

    public ReceiveConfirm(String myUserId, String headInfo, int fileLen) {
        jf = new JFrame();
        this.headInfo = headInfo;
        this.myUserId = myUserId;
        this.fileLen = fileLen;
        result = fileLen / 1024;
        System.out.println(myUserId + headInfo);
        // 创建
        jl = new JLabel(myUserId + " 发来文件:【 " + headInfo + " 】，文件大小" + result
                + "KB,是否接受");
        jb1 = new JButton("是");
        jb1.addActionListener(this);
        jb2 = new JButton("否");
        jb2.addActionListener(this);
        jp1 = new JPanel();
        jp2 = new JPanel();
        // 布局管理设置
        // 添加组件
        jp1.add(jl);
        jp2.add(jb1);
        jp2.add(jb2);
        jf.add(jp1, "Center");
        jf.add(jp2, "South");
        // 设置属性
        jf.setSize(500, 120);
        jf.setTitle("提示信息");
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setVisible(true);
        jf.setLocation(550, 300);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        if (e.getSource() == jb1) {
            jf.dispose();
            // FileDialogTest fdt=new FileDialogTest();
            JFrame jf = new JFrame();
            FileDialog fd = new FileDialog(jf, "选择保存文件路径", FileDialog.SAVE);
            fd.setVisible(true);
            System.out.println("保存位置" + fd.getDirectory() + fd.getFile());
            String filePath = fd.getDirectory() + fd.getFile();
            locationpath = filePath.replaceAll("\\\\", "/");
            System.out.println("保存位置1" + locationpath);
        }
    }

    public String getLocationpath() {
        System.out.println("保存位置2" + locationpath);
        return locationpath;
    }
}