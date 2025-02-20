package com.example.resource;

import com.example.model.*;
import com.example.filter.FilterCriteria;
import com.example.filter.FilterOperator;
import com.example.FirstService;
import com.example.exception.ServiceException;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Arrays;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import javax.ejb.EJB;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MovieResource {

    @EJB(lookup = "java:global/first-service-ejb-1.0-SNAPSHOT/FirstServiceEJB!com.example.FirstService")
    private FirstService firstService;

    @POST
    @Path("/movie")
    public Response createMovie(@NotNull @Valid Movie movie) {
        try {
            Movie created = firstService.createMovie(movie);
            return Response.status(Response.Status.CREATED)
                         .build();
        } catch (ServiceException e) {
            return Response.status(404)
                         .entity(Map.of("message", List.of(Map.of("inner_message", e.getMessage()))))
                         .build();
        } catch (Exception e) {
            return Response.status(500)
                         .entity(Map.of("message", "Внутренняя ошибка сервера"))
                         .build();
        }
    }

    @GET
    @Path("/movies/{id}")
    public Response getMovieById(@PathParam("id") Long id) {
        try {
            Movie movie = firstService.getMovieById(id);
            return Response.status(Response.Status.OK)
                         .entity(movie)
                         .build();
        } catch (ServiceException e) {
            return Response.status(404)
                         .entity(Map.of("message", List.of(Map.of("inner_message", e.getMessage()))))
                         .build();
        } catch (Exception e) {
            return Response.status(500)
                         .entity(Map.of("message", e.getMessage()))
                         .build();
        }
    }

    @DELETE
    @Path("/movies/{id}")
    public Response deleteMovie(@PathParam("id") Long id) {
        try {
            firstService.deleteMovieById(id);
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (ServiceException e) {
            return Response.status(404)
                         .entity(Map.of("message", List.of(Map.of("inner_message", e.getMessage()))))
                         .build();
        } catch (Exception e) {
            return Response.status(500)
                         .entity(Map.of("message", e.getMessage()))
                         .build();
        }
    }

        @PATCH
    @Path("/movies/{id}")
    public Response updateMovie(@PathParam("id") Long id, @NotNull @Valid Movie movie) {
        try {
            Movie updated = firstService.updateMovieById(id, movie);
            return Response.status(Response.Status.OK)
                         .build();
        } catch (ServiceException e) {
            return Response.status(404)
                         .entity(Map.of("message", List.of(Map.of("inner_message", e.getMessage()))))
                         .build();
        } catch (Exception e) {
            return Response.status(500)
                         .entity(Map.of("message", e.getMessage()))
                         .build();
        }
    }

    @GET
    @Path("/movies")
    public Response getMovies(
            @QueryParam("sort") List<String> sort,
            @QueryParam("filter") String filter,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        try {
            return Response.status(Response.Status.OK)
                         .entity(firstService.getMovies(sort, filter, page, pageSize))
                         .build();
        } catch (ServiceException e) {
            return Response.status(400)
                         .entity(Map.of("message", e.getMessage()))
                         .build();
        }
    }

        @GET
    @Path("/movies/count")
    public Response getMoviesCount(
            @QueryParam("mpaaRating") MpaaRating mpaaRating,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        try {
            return Response.status(Response.Status.OK)
                         .entity(firstService.getMoviesCount(mpaaRating, page, pageSize))
                         .build();
        } catch (ServiceException e) {
            return Response.status(400)
                         .entity(Map.of("message", e.getMessage()))
                         .build();
        }
    }
} 