IO module

Blocking IO (BIO)

Non-Blocking IO (NIO)

IO Multiplexing 


Asynchronous IO  AIO


NIO channel  type:
FileChannel  文件通道，用于文件的数据读写，特别：阻塞模式，不能设置为非阻塞模式
SocketChannel   套接字通道，用于套接字TCP连接的数据读写
ServerSocketChannel  服务器套接字通道（服务器监听通道），允许我们监听TCP连接请求，为每个监听到的请求创建一个SocketChannel通道
DatagramChannel  数据报通道，用于UDP的数据读写