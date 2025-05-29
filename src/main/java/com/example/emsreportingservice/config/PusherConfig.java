package com.example.emsreportingservice.config; // Adjust package as needed

import com.pusher.rest.Pusher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PusherConfig {

    @Value("${pusher.appId}")
    private String appId;

    @Value("${pusher.key}")
    private String key;

    @Value("${pusher.secret}")
    private String secret;

    @Value("${pusher.cluster}")
    private String cluster;

    @Value("${pusher.encrypted}")
    private boolean encrypted;

    /**
     * Configures and provides a Pusher client bean using set methods.
     * This method will be called by Spring to create a singleton instance
     * of the Pusher client that can be injected into other components.
     *
     * @return A configured Pusher client instance.
     */
    @Bean
    public Pusher pusher() {
        // Initialize Pusher with the core credentials
        Pusher pusher = new Pusher(appId, key, secret);

        // Set properties using the set methods as you specified
        pusher.setCluster(cluster);
        pusher.setEncrypted(encrypted);

        return pusher;
    }
}