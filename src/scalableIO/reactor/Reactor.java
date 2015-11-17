package scalableIO.reactor;

import static scalableIO.Logger.log;
import static scalableIO.ServerContext.isMainReactor;
import static scalableIO.ServerContext.selectTimeOut;
import static scalableIO.ServerContext.serverChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

public abstract class Reactor extends Thread{

	protected final int port;
	protected Selector selector;
//	protected ServerSocketChannel serverChannel;
	
	public Reactor(int port){
		this.port = port;
	}
	
	public void configure(){
		ServerSocketChannel serverChannel = serverChannel();
		try {
			selector = Selector.open();
			log(selector+" isMainReactor="+isMainReactor(this));
			if(isMainReactor(this)){
				log(getClass().getSimpleName()+" start on "+port+" ..."+"\n");
				//serverChannel = ServerSocketChannel.open();
				serverChannel.socket().bind(new InetSocketAddress(port));
				serverChannel.configureBlocking(false);
				SelectionKey key = serverChannel.register(selector, SelectionKey.OP_ACCEPT);
				key.attach(newAcceptor(selector, serverChannel));
			}else{
				
			}
			//���ʹ��������select��ʽ���ҿ�������Ĵ���Ļ����൱�ڿ����˶��reactor�أ�������mainReactor��subReactor�Ĺ�ϵ��
			//SelectionKey key = serverChannel.register(selector, SelectionKey.OP_ACCEPT);
			//key.attach(newAcceptor(selector, serverChannel));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public abstract Acceptor newAcceptor(Selector selector, ServerSocketChannel serverChannel);
	
	@Override
	public void run(){
		try {
			while(!Thread.interrupted()){
				//������ʹ��������select��ʽ������accept��subReactor��selector��register��ʱ���һֱ����
				//�����޸�Ϊ���г�ʱ��select����selectNow��subReactor��selector��register�Ͳ���������
				//����ѡ���˴��г�ʱ��select����Ϊʹ��selectNow������ѭ���ᵼ��CPU쭸��ر��
				//selector.select();
				//if(selector.selectNow() > 0){
				if(selector.select(selectTimeOut) > 0){
					//log(selector+" isMainReactor="+isMainReactor(this)+" select...");
					Iterator<SelectionKey> keyIt = selector.selectedKeys().iterator();
					while(keyIt.hasNext()){
						SelectionKey key = keyIt.next();
						dispatch(key);
						keyIt.remove();
					}
				}
			}
			log(getClass().getSimpleName()+" end on "+port+" ..."+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void dispatch(SelectionKey key){
		Runnable r = (Runnable)key.attachment();
		if(r != null){
			r.run();
		}
	}
	
//	public void wakeup(){
//		selector.wakeup();
//		log(selector+" isMainReactor="+isMainReactor(this)+" wakeup...");
//	}
	
}
