package com.example;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class FirstServiceResource {

    @EJB(lookup = "java:global/first-service-ejb-1.0-SNAPSHOT/FirstServiceEJB!com.example.FirstService")
    private FirstService firstService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/hello")
    public String hello(@QueryParam("name") String name) {
        return firstService.sayHello(name != null ? name : "World");
    }
} 