package konantech.ai.aikwc.common.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import konantech.ai.aikwc.common.utils.CommonUtil;
import konantech.ai.aikwc.entity.Collector;
import konantech.ai.aikwc.repository.CollectorRepository;
import net.sf.json.JSONArray;

@Component
public class CheckStatusHandler extends TextWebSocketHandler {
	
	private Set<WebSocketSession> sessionList = new HashSet<WebSocketSession>();
	
	@Autowired
	private CollectorRepository collectorRepository;

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		System.out.println(">>>>>>>>>>>>>>>>>>connection established");
		sessionList.add(session);
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		// TODO Auto-generated method stub
		super.handleTextMessage(session, message);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		System.out.println(">>>>>>>>>>>>>>>>>>>connection closed");
		sessionList.remove(session);
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>websocket error");
		sessionList.remove(session);
	}
	
	
	public void sendCollectorStatus() throws IOException {
		Iterator<WebSocketSession> iterator = sessionList.iterator();
		
		
		while(iterator.hasNext()) {
			WebSocketSession session = iterator.next();
			List<Collector> collectors = collectorRepository.findByUseyn("Y");
//			List<Collector> collectors = collectorRepository.findStatus();
			
			JSONArray arr = (JSONArray) CommonUtil.parseToJson(collectors);
			System.out.println(arr.toString());
			
			TextMessage message = new TextMessage(arr.toString());
			session.sendMessage(message);
			
		}
		
	}
	

}