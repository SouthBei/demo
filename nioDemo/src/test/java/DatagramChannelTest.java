import client.UdpClient;
import server.UdpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Scanner;

/**
 * @Author VLN
 * @Descrption
 * UDP传输数据
 * DatagramChannel datagramChannel=DatagramChannel.open();
 * datagramChannel.configureBlocking(false);
 * channel.socket().bind(new InetSocketAddress(18999));
 * //read
 * ByteBuffer buf=ByteBuffer.allocate(1024);
 * SocketAddress clientAddr=datagramChannel.receive(buf);
 *
 * 把缓冲区翻转为读模式
 * buffer.flip();
 * 调用send()方法，把数据发送到目标IP和端口
 * datagramChannel.send(buffer,new InetSocketAddress("127.0.0.1",18899));
 *清空缓冲区，切换到写模式
 * buffer.clear();
 * datagramChannel.close();
 *
 * @Date 2025/4/27
 * @Version
 */
public class DatagramChannelTest {

    public static void main(String[] args) {
        //启动UDPServer
        Thread thread=new Thread(
                ()->{
                    try{
                        UdpServer udpServer=new UdpServer();
                        udpServer.receive();
                    }catch (Exception e){
                        System.out.println("udp服务端异常"+e.getMessage());
                    }

                }
        );
        thread.start();

        UdpClient udpClient=new UdpClient();
        try{
            udpClient.send();
        }catch (Exception e){
            System.out.println("发送消息异常"+e.getMessage());
        }


    }


//    public void send() throws IOException {
//        DatagramChannel dChannel=DatagramChannel.open();
//        //设置为非阻塞
//        dChannel.configureBlocking(false);
//        ByteBuffer buffer=ByteBuffer.allocate(1024);
//        Scanner scanner=new Scanner(System.in);
//        System.out.println("UDP客户端启动成功！");
//        System.out.println("请输入发送内容:");
//        while (scanner.hasNext()){
//            String next=scanner.next();
//            buffer.put((System.currentTimeMillis()+">>"+next).getBytes());
//            buffer.flip();
//            //通过datagramChannel发送数据
//            dChannel.send(buffer,new InetSocketAddress("127.0.0.1",18999));
//            buffer.clear();
//        }
//        dChannel.close();
//    }
}
