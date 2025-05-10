import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * @Author VLN
 * @Descrption
 * channel buffer selector  数据总是从通道读到缓冲区，或者从缓冲区写入通道中
 * 通道与选择器之间的关系
 * channel.register(selector,ops);ops: SelectionKey.*
 * 若需要监控通道中多种事件，需要用按位｜实现
 * int key= SelectionKey.OP_READ| SelectionKey.OP_WRITE;
 * Selector.open().底层实现使用一种可拓展的服务提供和发现机制。Java通过SPI(service Provider Interface)的方式提供定制化版本的选择器的动态
 * 替换或者拓展
 * serverSocketChannel.configureBlocking(false);
 * serverSocketChannel.register(); 注册到选择器的通道必须处于非阻塞模式下
 *
 *
 * @Date 2025/4/27
 * @Version
 */
public class SelectorTest {
    static final int port=19999;
    public static void startServer() throws IOException {
        //1. 获取选择器
        Selector selector=Selector.open();
        //2. 获取通道
        ServerSocketChannel serverSocketChannel=ServerSocketChannel.open();
        //3. 设置为非阻塞
        serverSocketChannel.configureBlocking(false);
        //4. 绑定连接
        serverSocketChannel.bind(new InetSocketAddress(port));
        System.out.println("服务器启动成功");
        //5. 将通道注册的"接受新连接"IO事件注册到选择器上
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        //6.轮训感兴趣的IO就绪事件(选择键集合)
        while (selector.select()>0){
            System.out.println("selector.select");
            //7.获取选择键集合
            Iterator<SelectionKey> selectionKeys=selector.selectedKeys().iterator();
            while (selectionKeys.hasNext()){
                System.out.println("selector.key");
                //8.获取单个的选择键
                SelectionKey next = selectionKeys.next();
                //9.判断key是具体的什么事件
                if(next.isAcceptable()){
                    //10.若选择键的IO事件是"连接就绪"，获取客户端连接
                    SocketChannel socketChannel=serverSocketChannel.accept();
                    //11. 将新连接切换为非阻塞模式
                    socketChannel.configureBlocking(false);
                    //12. 将新连接的通道的可读事件注册到选择器上
                    socketChannel.register(selector,SelectionKey.OP_READ);
                    System.out.println("连接已就绪！");
                }else if(next.isReadable()){
                    //13. 若选择键的IO事件是可读，那么则读取数据
                    SocketChannel socketChannel = (SocketChannel)next.channel();
                    //14. 读取数据，然后丢弃
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int length=0;
                    while ((length=socketChannel.read(buffer))>0){
                        buffer.flip();
                        System.out.println("read byte:"+String.valueOf(buffer));
                        buffer.clear();
                    }
                    socketChannel.close();
                }
            }
            //移除选择键
            selectionKeys.remove();;
        }
        //16. 关闭连接
        serverSocketChannel.close();
    }


    public static void startClient() throws IOException {
        InetSocketAddress address=new InetSocketAddress("127.0.0.1",port);
        //1. 获取通道
        SocketChannel socketChannel=SocketChannel.open(address);
        //2. 切换为阻塞模式
        socketChannel.configureBlocking(false);
        //3. 不断自旋，等待连接完成，或者做一些其他的事情
        while (!socketChannel.finishConnect()){
            //
            System.out.println("自旋等待");
        }
        System.out.println("客户端连接成功");
        //4. 分配指定大小的缓冲区
        ByteBuffer buffer=ByteBuffer.allocate(1024);
        buffer.put("hello world".getBytes());
        buffer.flip();

        //发送到服务器
        socketChannel.write(buffer);
        socketChannel.shutdownOutput();
        socketChannel.close();
    }

    public static void main(String[] args) throws IOException {

        Thread thread2=new Thread(()->{
            try {
                //Thread.sleep(2000);
                startClient();
            } catch (IOException  e) {
                e.printStackTrace();
            }
        });
        thread2.start();
        Thread thread=new Thread(()->{
            try {
                startServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();

        //

    }

}
