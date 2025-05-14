import sun.nio.ch.IOUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @Author VLN
 * @Descrption
 * SocketChannel socketChannel= SocketChannel.open();
 * socketChannel.configureBlocking();
 * socketChannel.connect(new InetSocketAddress("127.0.0.1",8080));
 * client:
 * SocketChannel socketChannel= SocketChannel.open();
 * socketChannel.configureBlocking(false);
 * socketChannel.connect(new InetSocketAddress("127.0.0.1",80));
 * while(!socketChannel.finishConnect()){
 *     //do something
 * }
 * server:
 * ServerSocketChannel server=(ServerSocketChannel) key.channel;
 * SocketChannel socketChannel= server.accept();
 * socketChannel.configureBlocking(false);
 *
 * @Date 2025/4/27
 * @Version  使用socketChannel 发送和接受文件的案例
 *
 * 客户端传文件都是分为多次传输，首先传入文件名称，其次是文件大小，然后是文件内容
 * 对于每一个客户端socketChannel,创建一个客户端对象，用户保存客户端状态，分别保存文件名、文件大小和写入的目标文件通道outChannel.
 * socketChannel对象和client 对象 是一一对应的关系
 * 此用例为了简单，没有解决沾包和半包的问题。
 */
public class SocketChannelTest {

    static class NioSendClientTest{
        private Charset charSet= Charset.forName("UTF-8");
        /**
         * 向服务端传输文件.文件发送过程：首先发送文件名称和文件长度，然后发送文件内容
         */
        public void sendFile(){
            try{
                String srcPath="./test.txt";
                String dstFileName="testserver.txt";
                File file= new File(srcPath);
                if(!file.exists()){
                    return;
                }
                FileChannel fileChannel=new FileInputStream(file).getChannel();
                SocketChannel socketChannel=SocketChannel.open();
                socketChannel.socket().connect(new InetSocketAddress("127.0.0.1",18899));
                socketChannel.configureBlocking(false);
                System.out.println("成功连接服务器");
                while (!socketChannel.finishConnect()){
                    System.out.println("自旋等待");
                }
                //发送文件名称和长度
                ByteBuffer byteBuffer = sendFileNameAndLength(dstFileName, file, socketChannel);
                //发送文件内容
                int length=sendContent(file,fileChannel,socketChannel,byteBuffer);
                if(length==-1){
                    //关闭所有流
                    fileChannel.close();
                    socketChannel.shutdownOutput();
                    socketChannel.close();
                }
                System.out.println("文件传输完成");
            }catch (Exception e){
                System.out.println("err.="+e.getMessage());
            }
        }
        /**
         * 发送文件内容
         */
        public int sendContent(File file, FileChannel fileChannel, SocketChannel socketChannel, ByteBuffer byteBuffer)
        throws IOException {
            System.out.println("开始传输文件");
            int length=0;
            long progress=0;
            while ((length=fileChannel.read(byteBuffer))>0){
                byteBuffer.flip();
                socketChannel.write(byteBuffer);
                progress+=length;
                byteBuffer.clear();
                System.out.println("======send progress ====="+(100*progress/file.length())+"%");
            }
            return length;
        }

        /**
         * 方法：发送文件名称和长度
         * @param destFile
         * @param file
         * @param socketChannel
         * @return
         */
        public ByteBuffer sendFileNameAndLength(String destFile,File file,SocketChannel socketChannel)
                throws IOException {
                //发送文件名称
            ByteBuffer fileNameByteBuf=charSet.encode(destFile);
            ByteBuffer buffer=ByteBuffer.allocate(1024);
            int fileNameLen=fileNameByteBuf.remaining();
            buffer.putInt(fileNameLen);
            buffer.flip();
            socketChannel.write(buffer);
            buffer.clear();
            System.out.println("文件名称长度发送完成："+fileNameLen);

            //发送文件名称
            socketChannel.write(fileNameByteBuf);
            System.out.println("client 文件名称发送完成："+destFile);

            //发送文件长度
            buffer.putLong(file.length());
            buffer.flip();
            socketChannel.write(buffer);
            buffer.clear();
            System.out.println("文件长度发送完成:"+file.length());
            return buffer;
        }

    }


    static class NioReceiveServerTest{
        private static final String RECEIVE_PATH="./";
        private Charset charset=Charset.forName("UTF-8");
        private ByteBuffer buffer=ByteBuffer.allocate(1024);
        Map<SelectableChannel,Client> clientMap=new HashMap<SelectableChannel,Client>();
        public void startServer() throws IOException {
            //1。获取选择器
            Selector selector=Selector.open();
            //2。 获取通道
            ServerSocketChannel serverSocketChannel=ServerSocketChannel.open();
            ServerSocket serverSocket = serverSocketChannel.socket();
            //3. 设置为非阻塞
            serverSocketChannel.configureBlocking(false);
            //4. 绑定连接
            InetSocketAddress inetSocketAddress=new InetSocketAddress(18899);
            serverSocket.bind(inetSocketAddress);
            //5、将通道注册到选择器上，并且注册的IO事件为"接收新连接"
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("serverChannel is listening ......");
            //6. 轮询感兴趣的IO就绪事件（选择键集合）
            while (selector.select()>0){
                //7. 选择键集合
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()){

                    //8. 获取单个的选择键，并处理
                    SelectionKey key = iterator.next();
                    //去重，NIO的特点是只会累加，已选择键的集合不会删除
                    //9. 判断key是什么具体的事件，是否为新连接事件
                    if(key.isAcceptable()){
                        //10. 若接受的事件是"新连接"，则获取客户端新连接
                        ServerSocketChannel server=(ServerSocketChannel)key.channel();
                        SocketChannel socketChannel = server.accept();
                        if(socketChannel==null){
                            continue;
                        }
                        //11. 客户端新连接，切换为非阻塞模式
                        socketChannel.configureBlocking(false);
                        //12. 将客户端新连接通道注册到selector上
                        SelectionKey selectionKey=socketChannel.register(selector,SelectionKey.OP_READ);
                        //接下来为业务处理
                        Client client=new Client();
                        client.remoteAddress=(InetSocketAddress) socketChannel.getRemoteAddress();
                        clientMap.put(socketChannel,client);
                        System.out.println(socketChannel.getRemoteAddress()+"连接成功.....");
                    }else if(key.isReadable()){
                        //处理数据
                        processData(key);
                    }
                    iterator.remove();
                }
            }

        }


        public void  processData(SelectionKey key) throws IOException{
            Client client=clientMap.get(key.channel());
            SocketChannel socketChannel=(SocketChannel)key.channel();
            int num=0;
            try{
                buffer.clear();
                while ((num=socketChannel.read(buffer))>0){
                    buffer.flip();
                    //对客户端发送过来的文件进行处理，首先处理文件名
                    if(null==client.fileName){
                        if(buffer.remaining()<4){
                            continue;
                        }
                        int filleNameLen=buffer.getInt();
                        byte[] fileNameBytes=new byte[filleNameLen];
                        buffer.get(fileNameBytes);

                        //文件名
                        String fileName=new String(fileNameBytes,charset);
                        File directory=new File(RECEIVE_PATH);
                        if(!directory.exists()){
                            directory.mkdir();
                        }
                        System.out.println("NIO 传输目标dir:"+directory);
                        client.fileName=fileName;
                        String fullName= directory.getAbsolutePath()+File.separatorChar+fileName;
                        System.out.println("NIO 传输目标文件："+fullName);
                        File file=new File(fullName.trim());
                        if(!file.exists()){
                            file.createNewFile();
                        }

                        FileChannel fileChannel=new FileOutputStream(file).getChannel();
                        client.fileChannel=fileChannel;

                        if(buffer.capacity()<8){
                            continue;
                        }
                        long fileLength=buffer.getLong();
                        client.fileLength=fileLength;
                        client.startTime=System.currentTimeMillis();
                        System.out.println("NIo 传输开始：");
                        client.receiveLength+=buffer.capacity();
                        if(buffer.remaining()>0){
                            //写入文件
                            client.fileChannel.write(buffer);
                        }
//                        if(client.isFinished()){
//                          finished(key,client);
//                        }
                        buffer.clear();
                    }else {
                        client.receiveLength+=buffer.capacity();
                        //写入文件
                        client.fileChannel.write(buffer);
                        if(client.isFinished()){
                            finished(key,client);
                        }
                        buffer.clear();
                    }
                }
                key.cancel();
            }catch (IOException e){
                key.cancel();
                System.out.println("处理数据异常"+e.getMessage());
                return;
            }
            //调用close为-1，达到末尾
            if(num==-1){
                finished(key,client);
                buffer.clear();
            }
        }

        private void finished(SelectionKey key,Client client) throws IOException {
            client.fileChannel.close();
            key.cancel();
            System.out.println("文件接收成功，file Name:"+client.fileName);
        }


    }

    static class Client{
        //文件名称
        String fileName;
        //长度
        long fileLength;
        //开始传输的事件
        long startTime;
        //客户端地址
        InetSocketAddress remoteAddress;
        //输出的文件通道
        FileChannel fileChannel;
        //接收长度
        long receiveLength;
        public boolean isFinished(){
            return receiveLength>=fileLength;
        }
    }


    public static void main(String[] args) throws InterruptedException {
        Thread thread=new Thread(()->{
            NioReceiveServerTest serverTest=new NioReceiveServerTest();
            try {
                serverTest.startServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        thread.start();
        Thread.sleep(2000);
        NioSendClientTest sendClientTest=new NioSendClientTest();
        sendClientTest.sendFile();
    }
}
