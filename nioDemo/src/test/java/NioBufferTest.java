import java.nio.IntBuffer;
import java.util.logging.Logger;

/**
 * @Author VLN
 * @Descrption
 * @Date 2025/4/25
 * @Version  三个核心类： Buffer, Channel, Selector
 */
public class NioBufferTest {

    static IntBuffer intBuffer=null;
    public static void allocateTest(){
        intBuffer=IntBuffer.allocate(20);
        System.out.println("-----------after allocate-------");
        System.out.println("-----------buffer limit:="+intBuffer.limit());
        System.out.println("-----------buffer position:="+intBuffer.position());
        System.out.println("-----------buffer capacity:="+intBuffer.capacity());
    }

    public static void putTest(){
        for (int i=0;i<5;i++){
            //写入一个整数到缓冲区
            intBuffer.put(i);
        }
        System.out.println("-----------after put-------");
        System.out.println("-----------buffer limit:="+intBuffer.limit());
        System.out.println("-----------buffer position:="+intBuffer.position());
        System.out.println("-----------buffer capacity:="+intBuffer.capacity());
    }

    public static void flipTest(){
        //反转缓冲区，从写模式翻转成读模式
        intBuffer.flip();
        //输出缓冲区的主要属性值
        System.out.println("-----------after put-------");
        System.out.println("-----------buffer limit:="+intBuffer.limit());
        System.out.println("-----------buffer position:="+intBuffer.position());
        System.out.println("-----------buffer capacity:="+intBuffer.capacity());
    }

    public static void get(){
        //调用flip 将缓冲区切换成读模式之后，就可以开始从缓冲区读取数据了。
        for (int i=0;i<2;i++){
            int j=intBuffer.get();
            System.out.println("read value="+j);
        }
        System.out.println("-----------after first read-------");
        System.out.println("-----------buffer limit:="+intBuffer.limit());
        System.out.println("-----------buffer position:="+intBuffer.position());
        System.out.println("-----------buffer capacity:="+intBuffer.capacity());

        for (int i=0;i<3;i++){
            int j=intBuffer.get();
            System.out.println("read again value="+j);
        }
        System.out.println("-----------after second get-------");
        System.out.println("-----------buffer limit:="+intBuffer.limit());
        System.out.println("-----------buffer position:="+intBuffer.position());
        System.out.println("-----------buffer capacity:="+intBuffer.capacity());
    }
    //已经读完的数据，如果需要在读一遍，可以调用rewind方法
    public static void rewindTest(){
        intBuffer.rewind();
        System.out.println("-----------after rewind-------");
        System.out.println("-----------buffer limit:="+intBuffer.limit());
        System.out.println("-----------buffer position:="+intBuffer.position());
        System.out.println("-----------buffer capacity:="+intBuffer.capacity());
    }

    public static void reRead(){
        for (int i=0;i<5;i++){
            if(i==2){
                //临时保存，标记一下第三个位置
                intBuffer.mark();
            }
            int j=intBuffer.get();
            System.out.println("-----reRead value:"+j);
            System.out.println("-------afer reRead-----------");
            System.out.println("-----------buffer limit:="+intBuffer.limit());
            System.out.println("-----------buffer position:="+intBuffer.position());
            System.out.println("-----------buffer capacity:="+intBuffer.capacity());
        }
    }

    //mark() 和 reset()
    //mark 和reset 两个方法是配套使用的，Buffer.mark()方法将当前Position 的值保存起来放在mark属性
    //让mark属性记住这个临时位置，然后可以调用Buffer.reset()方法mark的值回复到position中
    public static void afterReset(){
        //紧跟着上面的方法
        System.out.println("---------after reset ----------");
        intBuffer.reset();
        System.out.println("-----------buffer limit:="+intBuffer.limit());
        System.out.println("-----------buffer position:="+intBuffer.position());
        System.out.println("-----------buffer capacity:="+intBuffer.capacity());
        for (int i=2;i<5;i++){
            int j=intBuffer.get();
            System.out.println("-----reset read value:="+j);
        }
    }

    //clear() 将缓冲区切换为写模式，此方法非作用是：
    //将position清零
    //limit 设置为capacity 最大值，可以一直写入，直到缓冲区写满
    public static void clearDemo(){
        System.out.println("---------prepare clear");
        intBuffer.clear();
        System.out.println("-----------buffer limit:="+intBuffer.limit());
        System.out.println("-----------buffer position:="+intBuffer.position());
        System.out.println("-----------buffer capacity:="+intBuffer.capacity());
    }

    public static void main(String[] args) {
        allocateTest();
        putTest();
        flipTest();
        get();
        rewindTest();
        reRead();
        afterReset();
        clearDemo();
    }
}
