package clientReceiverUDP;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;
import javax.swing.*;
public class FileClient extends JFrame {
    FileDialog fd1 = null;
    DatagramSocket ds = null;
    DatagramPacket sendDp = null;
    static int sendDataLen = 10240;
    public byte[] sendBuff = new byte[sendDataLen];
    public InetAddress udpIP = null;
    static int udpPort = 10000;
    static int tcpPort = 9999;

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        FileClient fc = new FileClient();
        fc.fileSender();

    }

    public void fileSender() {
        try {
            // 打开windows的文件对话框
            fd1 = new FileDialog(this, "请选择需要打开的文件", FileDialog.LOAD);
            fd1.setVisible(true);
            String filePath = fd1.getDirectory() + fd1.getFile();
            String location = filePath.replaceAll("\\\\", "/");
            System.out.println("绝对文件目录+文件名" + filePath);
            System.out.println("绝对文件目录+文件名" + location);
            DataInputStream dis = new DataInputStream(new BufferedInputStream(
                    new FileInputStream(location)));
            // 单位是字节
            int fileLen = dis.available();
            System.out.println("文件长度" + fileLen);
            // ****************************************************
            // 写一个TCP协议发送文件标题，让接受端确认是否接受
            Socket s = new Socket("127.0.0.1", tcpPort);// 发送到本机
            PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
            String headInfo = fd1.getFile() + "/" + fileLen;

            pw.println(headInfo);
            // 等待对方确认
            InputStreamReader isr = new InputStreamReader(s.getInputStream());
            BufferedReader br = new BufferedReader(isr);
            // 阻塞等待
            String info = br.readLine();
            System.out.println("我接收到文件接收器给我返回的内容了=" + info);
            if (info.equals("YES")) {
                s.close();
                System.out.println("我是文件发送器UDP，我已经开始发送了");
                // 主机从任意空闲端口发送；
                ds = new DatagramSocket();
                udpIP = InetAddress.getByName("127.0.0.1");
                while (dis.read(sendBuff) > 0) {
                    sendDp = new DatagramPacket(sendBuff, sendBuff.length,
                            udpIP, udpPort);
                    ds.send(sendDp);
                    TimeUnit.MICROSECONDS.sleep(1);// 限制传输速度
                    // ******************************************
                }
            } else {
                JOptionPane.showMessageDialog(null, "对方拒绝接受文件", "消息提示",
                        JOptionPane.WARNING_MESSAGE);
                dis.close();
                s.close();
            }
            System.out.println("发送完毕");
            dis.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }
}
