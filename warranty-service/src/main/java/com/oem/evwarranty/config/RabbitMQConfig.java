package com.oem.evwarranty.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.queue}")
    private String queueName;

    @Value("${app.rabbitmq.routing-key:vehicle.created}")
    private String routingKey;

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // [QUAN TRỌNG] Tự động khai báo Queue/Exchange
    @Bean
    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public Queue warrantyQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName);
    }

    @Value("${app.rabbitmq.consumer.queue:part.claim.approved.queue}")
    private String claimQueueName;

    @Value("${app.rabbitmq.consumer.routing-key:claim.approved}")
    private String claimRoutingKey;


    // 1. Khai báo Queue nhận tin Claim
    @Bean
    public Queue claimQueue() {
        return new Queue(claimQueueName, true);
    }


    // 2. Binding vào Exchange
    @Bean
    public Binding claimBinding(Queue claimQueue, TopicExchange exchange) {
        return BindingBuilder.bind(claimQueue).to(exchange).with(claimRoutingKey);
    }
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}