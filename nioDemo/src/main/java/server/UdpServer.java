package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * @Author VLN
 * @Descrption
 * @Date 2025/4/23
 * @Version
 */
public class UdpServer {


    public void receive() throws IOException {
        DatagramChannel datagramChannel = DatagramChannel.open();
        datagramChannel.configureBlocking(false);
        datagramChannel.bind(new InetSocketAddress("127.0.0.1", 18999));
        System.out.println("UDP服务器启动成功！");

        Selector selector = Selector.open();
        datagramChannel.register(selector, SelectionKey.OP_READ);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while (selector.select() > 0) {
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                iterator.remove(); // 移除已处理的事件
                if (selectionKey.isReadable()) {
                    buffer.clear(); // 确保 buffer 是干净的
                    SocketAddress client = datagramChannel.receive(buffer);
                    buffer.flip();
                    String message = StandardCharsets.UTF_8.decode(buffer).toString();
                    System.out.println("UDP客户端 [" + client + "] 发送的消息: " + message);
                }
            }
        }

        System.out.println("开始释放UDP资源");
        selector.close();
        datagramChannel.close();
        System.out.println("UDP资源释放完成");
    }

}
