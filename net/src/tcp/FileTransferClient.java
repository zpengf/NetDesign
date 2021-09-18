package tcp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;

/**
 * 客户端代码
 */
public class FileTransferClient extends Socket {

    private static final String SERVER_IP = "127.0.0.1"; // 服务端IP
    private static final int SERVER_PORT = 8899; // 服务端端口
    private Socket client;
    private DataOutputStream dos;
    private DataInputStream dis;
    private RandomAccessFile rad;

    public FileTransferClient() throws Exception {
        super(SERVER_IP, SERVER_PORT);
        this.client = this;
        //System.out.println("客户端：成功连接服务端");
    }

    public void sendFile(String filePath, String targetPath) throws Exception {
        try {
            File file = new File(filePath);

            if(file.exists()) {
                dos = new DataOutputStream(client.getOutputStream());     //发送信息 getOutputStream方法会返回一个java.io.OutputStream对象
                dis = new DataInputStream(client.getInputStream());    //接收远程对象发送来的信息  getInputStream方法会返回一个java.io.InputStream对象
                dos.writeUTF(targetPath); //发送目标路径
                dos.writeUTF(file.getName()); //发送文件名
                //System.out.println("客户端：发送文件名");
                rad = new RandomAccessFile(file.getPath(), "r");
                /*
                 * RandomAccessFile是Java输入输出流体系中功能最丰富的文件内容访问类，既可以读取文件内容，也可以向文件输出数据。
                 * 与普通的输入/输出流不同的是，RandomAccessFile支持跳到文件任意位置读写数据，RandomAccessFile对象包含一个记录指针，用以标识当前读写处的位置。
                 * 当程序创建一个新的RandomAccessFile对象时，该对象的文件记录指针对于文件头 r代表读取
                 */
                dos.flush();    //作用见下方介绍
                dos.writeLong(file.length()); //发送文件长度
                //System.out.println("客户端：发送文件长度");
                dos.flush();
                long size = dis.readLong();    //读取当前已发送文件长度
                //System.out.println("客户端：开始传输文件 ");
                int length = 0;
                byte[] bytes = new byte[1024];    //每1kb发送一次
                if (size < rad.length()) {
                    rad.seek(size);
                    //System.out.println("客户端：文件定位完成");
                    //移动文件指针
                    while((length = rad.read(bytes)) > 0){
                        dos.write(bytes, 0, length);
                        dos.flush();
                        //每1kb清空一次缓冲区
                        //为了避免每读入一个字节都写一次，java的输流有了缓冲区，读入数据时会首先将数据读入缓冲区，等缓冲区满后或执行flush或close时一次性进行写入操作
                    }
                }
                //System.out.println("客户端：文件传输成功 ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {    //关闭资源
            if(dos != null)
                dos.close();
            if(dis != null)
                dis.close();
            if(rad != null)
                rad.close();
            client.close();
        }

    }

    class cancelActionListener implements ActionListener{    //关闭按钮监听器
        public void actionPerformed(ActionEvent e3){
            try {
                //System.out.println("客户端：文件传输取消");
                if(dis != null)
                    dis.close();
                if(dos != null)
                    dos.close();
                if(rad != null)
                    rad.close();
                client.close();
            } catch (IOException e1) {

            }
        }
    }
}