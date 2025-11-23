package com.oem.evcampaign.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper; // Import này quan trọng
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.queue}")
    private String queueName;

    @Value("${app.rabbitmq.routing-key}")
    private String routingKey;

    // --- [FIX LỖI ClassNotFoundException] ---
    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();

        // Tạo ClassMapper thủ công
        DefaultClassMapper classMapper = new DefaultClassMapper();

        // QUAN TRỌNG: Map class từ 'evpart' sang 'evcampaign'
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        // Khi nhận được tin từ 'com.oem.evpart...', hãy map nó vào DTO của 'campaign'
        idClassMapping.put("com.oem.evpart.dto.event.InventoryLowEvent", com.oem.evcampaign.dto.event.InventoryLowEvent.class);

        classMapper.setIdClassMapping(idClassMapping);

        // Tin tưởng tất cả các package (để tránh lỗi bảo mật nếu có)
        classMapper.setTrustedPackages("*");

        converter.setClassMapper(classMapper);
        return converter;
    }
    // ----------------------------------------

    @Bean
    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public Queue campaignQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}