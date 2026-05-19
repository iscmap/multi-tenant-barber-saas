package com.barbersaas.availability.config.messaging;

import com.barbersaas.shared.messaging.config.MessagingProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MessagingProperties.class)
public class AvailabilityMessagingConfiguration {}
