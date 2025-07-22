package com.shs.playrabbitmqbackend.config;

import com.shs.playrabbitmqbackend.chat.RabbitMQListener;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "chat.exchange";
    public static final String QUEUE_NAME = "chat.queue";
    public static final String ROUTING_KEY = "chat.routingKey";

    // Exchange 설정
    @Bean
    TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    // Queue 설정
    @Bean
    Queue queue() {
        return new Queue(QUEUE_NAME, true);
    }

    // Exchange와 Queue 바인딩
    @Bean
    Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    // RabbitTemplate (메시지 발행)
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }

    // 메시지 리스너 (메시지 수신 처리)
    @Bean
    public SimpleMessageListenerContainer messageListenerContainer(
        ConnectionFactory connectionFactory,
        MessageListenerAdapter listenerAdapter) {

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(QUEUE_NAME);
        container.setMessageListener(listenerAdapter);
        return container;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(RabbitMQListener listener) {
        return new MessageListenerAdapter(listener, "handleMessage");
    }
}