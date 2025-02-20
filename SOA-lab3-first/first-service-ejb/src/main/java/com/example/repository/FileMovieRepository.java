package com.example.repository;

import com.example.model.Movie;
import javax.ejb.Singleton;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class FileMovieRepository {
    private static final String FILE_PATH = System.getProperty("jboss.server.data.dir") + "/movies.dat";
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    public Movie save(Movie movie) {
        List<Movie> movies = readMovies();
        if (movie.getId() == null) {
            movie.setId(idGenerator.getAndIncrement());
        }
        movies.add(movie);
        writeMovies(movies);
        return movie;
    }
    
    @SuppressWarnings("unchecked")
    private List<Movie> readMovies() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<Movie>) ois.readObject();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    private void writeMovies(List<Movie> movies) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(movies);
        } catch (IOException e) {
            throw new RuntimeException("Error writing movies to file", e);
        }
    }

    public Movie findById(Long id) {
        List<Movie> movies = readMovies();
        return movies.stream()
            .filter(m -> m.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    public List<Movie> findAll() {
        return readMovies();
    }

    public void deleteById(Long id) {
        List<Movie> movies = readMovies();
        movies.removeIf(m -> m.getId().equals(id));
        writeMovies(movies);
    }

    public void updateById(Long id, Movie movie) {
        List<Movie> movies = readMovies();
        movies.removeIf(m -> m.getId().equals(id));
        movies.add(movie);
        writeMovies(movies);
    }
    
} 