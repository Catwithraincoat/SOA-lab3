package com.example;

import javax.ejb.Remote;
import java.util.List;
import java.util.Map;
import com.example.model.Movie;
import com.example.model.MpaaRating;

@Remote
public interface FirstService {
    String sayHello(String name);
    Movie createMovie(Movie movie);
    Movie getMovieById(Long id);
    Movie updateMovieById(Long id, Movie movie);
    void deleteMovieById(Long id);
    List<Movie> getMovies(List<String> sort, String filter, int page, int pageSize);
    Map<String, Object> getMoviesCount(MpaaRating mpaaRating, int page, int pageSize);
} 