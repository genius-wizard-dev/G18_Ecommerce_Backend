package com.g18.ecommerce.IdentifyService.configurations;

import com.g18.ecommerce.IdentifyService.utils.Constant;
import com.google.gson.Gson;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class GlobalConfiguration {
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public Gson gson(){
        return new Gson();
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, String> repliesContainer(ConsumerFactory<String, String> consumerFactory){
        ContainerProperties containerProperties = new ContainerProperties(Constant.PROFILE_ONBOARDED);
        containerProperties.setGroupId("onboarded-group");
        return new ConcurrentMessageListenerContainer<>(consumerFactory,containerProperties);
    }

    @Bean
    public ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate(
            ProducerFactory<String, String> producerFactory,
            ConcurrentMessageListenerContainer<String, String> repliesContainer) {
        return new ReplyingKafkaTemplate<>(producerFactory, repliesContainer);
    }

}

