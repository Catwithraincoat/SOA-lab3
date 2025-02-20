package com.example.consul;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

@WebListener
public class ConsulRegistrator implements ServletContextListener {
    
    private ConsulClient consulClient;
    private static final String SERVICE_ID = "first-service";
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Attempting to register service with Consul...");
        try {
            String consulHost = "localhost";
            int consulPort = 8500;
            System.out.println("Connecting to Consul at " + consulHost + ":" + consulPort);
            
            consulClient = new ConsulClient(consulHost, consulPort);
            
            NewService newService = new NewService();
            newService.setId(SERVICE_ID);
            newService.setName("first-service");
            newService.setPort(8443);
            newService.setAddress("localhost");
            
            System.out.println("Registering service: " + newService.getName() + " on port " + newService.getPort());
            consulClient.agentServiceRegister(newService);
            System.out.println("Service registered successfully");
            
        } catch (Exception e) {
            System.err.println("Failed to register service: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            if (consulClient != null) {
                consulClient.agentServiceDeregister(SERVICE_ID);
                System.out.println("Service deregistered from Consul");
            }
        } catch (Exception e) {
            System.err.println("Failed to deregister service: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 