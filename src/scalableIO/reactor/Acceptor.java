package scalableIO.reactor;

import static scalableIO.Logger.log;
import static scalableIO.ServerContext.nextSubReactor;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public abstract class Acceptor extends Thread {

	protected final Selector selector;
	protected final ServerSocketChannel serverChannel;
	
	public Acceptor(Selector selector, ServerSocketChannel serverChannel){
		this.selector = selector;
		this.serverChannel = serverChannel;
	}
	
	@Override
	public void run() {
		log(selector+" accept...");
		try {
			 SocketChannel clientChannel = serverChannel.accept();
			 if(clientChannel != null){
				 log(selector+" clientChannel not null...");
				 //���ʹ��������select��ʽ����Ŀ���ǿ����˶��reactor�أ�������mainReactor��subReactor�Ĺ�ϵ�Ļ���
				 //������Ͳ���nextSubSelector().selector�����Ǹ�Ϊ����selector����
				 handle(nextSubReactor().selector/*selector*/, clientChannel);
			 }else{
				 log(selector+" clientChannel is null...");
			 }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public abstract void handle(Selector selector, SocketChannel clientSocket);

}
