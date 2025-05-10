package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * @Author VLN
 * @Descrption
 * @Date 2025/4/23
 * @Version
 */
public class UdpClient {

    private ByteBuffer buffer;

    public void send() throws IOException {
        //获取DatagramChannel
        DatagramChannel datagramChannel=DatagramChannel.open();
        //设置为非阻塞
        datagramChannel.configureBlocking(false);
        buffer = ByteBuffer.allocate(1024);
        Scanner scanner=new Scanner(System.in);
        System.out.println("UDP 客户端启动成功！");
        System.out.println("请输入发送内容:");
        while (scanner.hasNext()){
            String next=scanner.next();
            buffer.put((">>>>"+next).getBytes(StandardCharsets.UTF_8));
            buffer.flip();
            //通过DatagramChannel 发送数据
            datagramChannel.send(buffer,new InetSocketAddress("127.0.0.1",18999));
            buffer.clear();
        }
        datagramChannel.close();

    }
}
