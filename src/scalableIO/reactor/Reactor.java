package scalableIO.reactor;

import static scalableIO.Logger.log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

public abstract class Reactor extends Thread{

	protected final int port;
	protected final ServerSocketChannel serverChannel;
	protected final boolean isMainReactor;
	protected final boolean useMultipleReactors;
	protected final long timeout;
	protected Selector selector;
	
	public Reactor(int port, ServerSocketChannel serverChannel, boolean isMainReactor, boolean useMultipleReactors, long timeout){
		this.port = port;
		this.serverChannel = serverChannel;
		this.isMainReactor = isMainReactor;
		this.useMultipleReactors = useMultipleReactors;
		this.timeout = timeout;
	}
	
	public void init(){
		try {
			selector = Selector.open();
			log(selector+" isMainReactor="+isMainReactor);
			
			if(isMainReactor){
				log(getClass().getSimpleName()+" start on "+port+" ..."+"\n");
				//serverChannel = ServerSocketChannel.open();
				serverChannel.socket().bind(new InetSocketAddress(port));
				serverChannel.configureBlocking(false);
				SelectionKey key = serverChannel.register(selector, SelectionKey.OP_ACCEPT);
				key.attach(newAcceptor(selector));
			}else{
				
			}
			
			//���ʹ��������select��ʽ���ҿ�������Ĵ���Ļ����൱�ڿ����˶��reactor�أ�������mainReactor��subReactor�Ĺ�ϵ��
			//SelectionKey key = serverChannel.register(selector, SelectionKey.OP_ACCEPT);
			//key.attach(newAcceptor(selector, serverChannel));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public abstract Acceptor newAcceptor(Selector selector);
	
	@Override
	public void run(){
		try {
			while(!Thread.interrupted()){
				//������ʹ��������select��ʽ������accept��subReactor��selector��register��ʱ���һֱ����
				//�����޸�Ϊ���г�ʱ��select����selectNow��subReactor��selector��register�Ͳ���������
				//����ѡ���˴��г�ʱ��select����Ϊʹ��selectNow������ѭ���ᵼ��CPU쭸��ر��
				//selector.select();
				//if(selector.selectNow() > 0){
				if(selector.select(timeout) > 0){
					log(selector+" isMainReactor="+isMainReactor+" select...");
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
