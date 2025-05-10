import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @Author VLN
 * @Descrption
 * FileChannel :文件通道。用于文件的数据读写
 * SocketChannel: 套接字通道，用于套接字TCP连接的数据读写
 * ServerChannelChannel: 服务器套接字通道，允许我们监听TCP连接请求，并为每个监听到的请求创建一个SocketChannel通道
 * DatagramChannel: 数据报通道，用户UDP的数据读写
 * @Date 2025/4/27
 * @Version
 */
public class NioChannelTest {
    /**
     * 复制两个资源目录下的文件
     */
    public static void  nioCopyResourceFile(){
        //源
        String srcPath="/Users/nanbei/Downloads/IMG_0475.PNG";
        System.out.println("srcPath="+srcPath);
        String dstPath="/Users/nanbei/Downloads/IMG_0475-copy.PNG";
        System.out.println("dstPath="+dstPath);
        nioCopyFile(srcPath,dstPath);
    }

    /**
     * Nio方式复制文件
     * @param srcPath 源路径
     * @param destPath 目标路径
     */
    public static void nioCopyFile(String srcPath,String destPath){
        File srcFile=new File(srcPath);
        File destFile=new File(destPath);
        try{
            if(!destFile.exists()){
                destFile.createNewFile();
            }
        }catch (Exception e){
            System.out.println("err.="+e.getMessage());
        }
        long startTime=System.currentTimeMillis();
        FileInputStream fis=null;
        FileOutputStream fos=null;
        FileChannel inChannel=null;//输入通道
        FileChannel outChannel=null;//输出通道
        try{
            fis= new FileInputStream(srcFile);
            fos=new FileOutputStream(destFile);
            inChannel=fis.getChannel();
            outChannel=fos.getChannel();
            int length=-1;
            //新建buf，处于写模式
            ByteBuffer buf = ByteBuffer.allocate(1024);
            //从输入通道读取数据到buf
            while ((length=inChannel.read(buf)) !=-1){
                buf.flip();
                int outLength=0;
                while ((outLength=outChannel.write(buf))!=0){
                    System.out.println("写入的字节数:"+outLength);
                }
                //buf第二次模式切换：清除buf，变成写模式
                buf.clear();
            }
            //强制刷新到磁盘
            outChannel.force(true);
        }catch (Exception e){
            System.out.println("err.="+e.getMessage());
        }finally {
            try{
                outChannel.close();
                fos.close();
                inChannel.close();
                fis.close();
            }catch (Exception e){
                System.out.println("close err="+e.getMessage());
            }

        }
        long endTime=System.currentTimeMillis();
        System.out.println("base 复制毫秒数:"+(endTime-startTime));
    }

    public static void main(String[] args) {
        nioCopyResourceFile();
    }

}
