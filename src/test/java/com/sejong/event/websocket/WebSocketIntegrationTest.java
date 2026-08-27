package com.sejong.event.websocket;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class WebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Test
    @DisplayName("STOMP 클라이언트가 /ws 에 연결·구독하고 부스 토픽 브로드캐스트를 수신한다")
    void stompClientReceivesBoothBroadcast() throws Exception {
        WebSocketStompClient stompClient = new WebSocketStompClient(
                new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient()))));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        BlockingQueue<Object> received = new LinkedBlockingQueue<>();

        StompSession session = stompClient
                .connectAsync("http://localhost:" + port + "/ws", new StompSessionHandlerAdapter() { })
                .get(5, TimeUnit.SECONDS);

        session.subscribe("/topic/booths/777", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                received.add(payload);
            }
        });
        Thread.sleep(300); // 구독 등록 완료 대기

        messagingTemplate.convertAndSend("/topic/booths/777",
                Map.of("boothId", 777, "waitingCount", 3, "isOpen", true));

        Object payload = received.poll(5, TimeUnit.SECONDS);
        assertThat(payload).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) payload;
        assertThat(body).containsEntry("boothId", 777)
                .containsEntry("waitingCount", 3)
                .containsEntry("isOpen", true);

        session.disconnect();
        stompClient.stop();
    }
}
