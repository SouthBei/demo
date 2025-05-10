import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

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
 * @Version
 */
public class SocketChannelTest {

    static class NioSendClientTest{
        private Charset charSet= Charset.forName("UTF-8");
        /**
         * 向服务端传输文件
         */
        public void sendFile(){
            try{
                String srcPath="";
                String dstFileName="";
                File file= new File(srcPath);
                if(!file.exists()){
                    return;
                }
                FileChannel fileChannel=new FileInputStream(file).getChannel();
                SocketChannel socketChannel=SocketChannel.open();
                socketChannel.socket().connect(new InetSocketAddress("127.0.0.1",19999));
                socketChannel.configureBlocking(false);
                System.out.println("成功连接服务器");
                while (!socketChannel.finishConnect()){
                    //发送文件名称和长度
                    ByteBuffer byteBuffer = sendFileNameAndLength(dstFileName, file, socketChannel);
                    //发送文件内容
                    int length=sendContent(file,fileChannel,socketChannel,byteBuffer);
                    if(length==-1){
                        //关闭所有流
                    }
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
            int fileNameLen=fileNameByteBuf.capacity();
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
            System.out.println("文件偿付发送完成:"+file.length());
            return buffer;
        }

    }
}
