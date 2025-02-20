package com.example;

import java.util.ArrayList;
import javax.ejb.Stateless;
import javax.ejb.Remote;
import javax.ejb.EJB;
import javax.annotation.PostConstruct;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import com.example.repository.PersonRepository;
import com.example.repository.FileMovieRepository;
import com.example.model.Movie;
import com.example.model.MovieGenre;
import com.example.model.MpaaRating;
import com.example.model.Person;
import com.example.model.Coordinates;
import com.example.filter.FilterCriteria;
import com.example.filter.FilterOperator;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.Map;
import java.util.List;
import com.example.exception.ServiceException;
import java.util.Set;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Collections;
import java.util.HashMap;

@Stateless(name = "FirstServiceEJB")
@Remote(FirstService.class)
public class FirstServiceImpl implements FirstService {
    
    private static final Set<String> ALLOWED_FILTER_FIELDS = Set.of(
        "id", "name", "creationDate", 
        "coordinates.x", "coordinates.y",
        "oscarsCount", "genre", "mpaaRating",
        "director.name", "director.weight", "director.passportID", 
        "director.height", "director.location.name",
        "director.location.x", "director.location.y"
    );


    @EJB
    private PersonRepository personRepository;
    
    @EJB
    private FileMovieRepository movieRepository;
    
    @PostConstruct
    public void init() {
        System.out.println("Attempting to register service with Consul...");
        try {
            String consulHost = "localhost";
            int consulPort = 8500;
            System.out.println("Connecting to Consul at " + consulHost + ":" + consulPort);
            
            ConsulClient client = new ConsulClient(consulHost, consulPort);
            
            NewService newService = new NewService();
            newService.setId("first-service");
            newService.setName("first-service");
            newService.setPort(8443);
            newService.setAddress("localhost");
            
            System.out.println("Registering service: " + newService.getName() + " on port " + newService.getPort());
            client.agentServiceRegister(newService);
            System.out.println("Service registered successfully");
            
        } catch (Exception e) {
            System.err.println("Failed to register service: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public Movie createMovie(Movie movie) {
        Person director = personRepository.getDirector(movie.getDirectorName());
        if (director == null) {
            throw new ServiceException("Режиссер с таким именем не найден");
        }
        movie.setCreationDate(Instant.now().truncatedTo(ChronoUnit.MILLIS));
        movie.setDirector(director);
        return movieRepository.save(movie);
    }

    @Override
    public Movie getMovieById(Long id) {
        Movie movie = movieRepository.findById(id);
        if (movie == null) {
            throw new ServiceException("Фильм с таким id не найден");
        }
        return movie;
    }

    @Override
    public Movie updateMovieById(Long id, Movie movie) {
        
        if (movieRepository.findById(id) == null) {
            throw new ServiceException("Фильм с таким id не найден");
        }

        Person director = personRepository.getDirector(movie.getDirectorName());
        if (director == null) {
            throw new ServiceException("Режиссер с таким именем не найден");
        }

        movie.setId(id);
        movie.setDirector(director);
        movie.setCreationDate(Instant.now().truncatedTo(ChronoUnit.MILLIS));
        
        movieRepository.updateById(id, movie);
        return movie;
    }

    @Override
    public void deleteMovieById(Long id) {
        if (movieRepository.findById(id) == null) {
            throw new ServiceException("Фильм с таким id не найден");
        }
        movieRepository.deleteById(id);
    }
    
    @Override
    public String sayHello(String name) {
        return "Hello, " + name + "!";
    }

    @Override
    public List<Movie> getMovies(List<String> sort, String filter, int page, int pageSize) {
        try {
            List<Movie> movieList = new ArrayList<>(movieRepository.findAll());

            if (page < 1 || pageSize < 1) {
                throw new ServiceException(List.of(
                    Map.of("field", "pagination",
                          "inner_message", "Page and pageSize must be greater than 0")
                ).toString());
            }

            if (filter != null && !filter.isEmpty()) {
                List<FilterCriteria> filterCriteria = parseFilterCriteria(Arrays.asList(filter.split(",")));
                movieList = movieList.stream()
                        .filter(movie -> matchesAllCriteria(movie, filterCriteria))
                        .collect(Collectors.toList());
            }

            if (sort != null && !sort.isEmpty()) {
                Set<String> normalizedFields = new HashSet<>();
                for (String field : sort) {
                    String normalizedField = field.startsWith("-") ? field.substring(1) : field;
                    if (!normalizedFields.add(normalizedField)) {
                        throw new ServiceException(List.of(
                                        Map.of("field", "sort",
                                              "inner_message", "Duplicate sort field: " + normalizedField)).toString());
                    }
                }

                Comparator<Movie> comparator = null;
                for (String sortField : sort) {
                    boolean ascending = !sortField.startsWith("-");
                    String field = ascending ? sortField : sortField.substring(1);
                    
                    Comparator<Movie> fieldComparator = getComparator(field);
                    if (!ascending) {
                        fieldComparator = fieldComparator.reversed();
                    }
                    
                    comparator = comparator == null ? fieldComparator : comparator.thenComparing(fieldComparator);
                }
                
                if (comparator != null) {
                    movieList.sort(comparator);
                }
            }

            int fromIndex = (page - 1) * pageSize;
            if (fromIndex >= movieList.size()) {
                return new ArrayList<>();
            }
            
            int toIndex = Math.min(fromIndex + pageSize, movieList.size());
            return new ArrayList<>(movieList.subList(fromIndex, toIndex));
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(List.of(
                Map.of("field", "filter",
                      "inner_message", "Ошибка в формате фильтра")
            ).toString());
        }
    }

    @Override
    public Map<String, Object> getMoviesCount(MpaaRating mpaaRating, int page, int pageSize) {
        if (page < 1 || pageSize < 1) {
            throw new ServiceException(List.of(
                Map.of("field", "pagination",
                      "inner_message", "Page and pageSize must be greater than 0")
            ).toString());
        }

        List<Movie> allMovies = new ArrayList<>(
            movieRepository.findAll().stream()
                .filter(m -> m.getMpaaRating() != null && mpaaRating != null && 
                           m.getMpaaRating().ordinal() < mpaaRating.ordinal())
                .collect(Collectors.toList())
        );
        
        int fromIndex = (page - 1) * pageSize;
        List<Movie> pagedMovies;
        if (fromIndex >= allMovies.size()) {
            pagedMovies = new ArrayList<>();
        } else {
            int toIndex = Math.min(fromIndex + pageSize, allMovies.size());
            pagedMovies = new ArrayList<>(allMovies.subList(fromIndex, toIndex));
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", pagedMovies.size());
        response.put("movies", pagedMovies);
        
        return response;
    }

    


    private Comparator<Movie> getComparator(String field) {
        switch (field) {
            case "id":
                return Comparator.comparingLong(Movie::getId);
            case "name":
                return Comparator.comparing(Movie::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "creationDate":
                return Comparator.comparing(Movie::getCreationDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case "coordinates.x":
                return Comparator.comparingInt(m -> m.getCoordinates().getX());
            case "coordinates.y":
                return Comparator.comparing(m -> m.getCoordinates().getY(), Comparator.nullsLast(Comparator.naturalOrder()));
            case "oscarsCount":
                return Comparator.comparing(Movie::getOscarsCount, Comparator.nullsLast(Comparator.naturalOrder()));
            case "genre":
                return Comparator.comparing(m -> m.getGenre() != null ? m.getGenre().name() : "", 
                                         Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "mpaaRating":
                return Comparator.comparing(m -> m.getMpaaRating(), 
                                         Comparator.nullsLast(Comparator.naturalOrder()));
            case "director.name":
                return Comparator.comparing(m -> m.getDirector().getName(), 
                                         Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "director.weight":
                return Comparator.comparing(m -> m.getDirector().getWeight(), 
                                         Comparator.nullsLast(Comparator.naturalOrder()));
            case "director.passportID":
                return Comparator.comparing(m -> m.getDirector().getPassportID(), 
                                         Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "director.height":
                return Comparator.comparing(m -> m.getDirector().getHeight(), 
                                         Comparator.nullsLast(Comparator.naturalOrder()));
            case "director.location.name":
                return Comparator.comparing(m -> m.getDirector().getLocation().getName(), 
                                         Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case "director.location.x":
                return Comparator.comparing(m -> m.getDirector().getLocation().getX(), 
                                         Comparator.nullsLast(Comparator.naturalOrder()));
            case "director.location.y":
                return Comparator.comparing(m -> m.getDirector().getLocation().getY(), 
                                         Comparator.nullsLast(Comparator.naturalOrder()));
            default:
                return (m1, m2) -> 0;
        }
    }

    private List<FilterCriteria> parseFilterCriteria(List<String> filters) {
        List<Map<String, String>> errors = new ArrayList<>();
        List<FilterCriteria> criteria = new ArrayList<>();
        
        for (String filter : filters) {
            int bracketStart = filter.indexOf('[');
            int bracketEnd = filter.indexOf(']');
            int equalSign = filter.indexOf('=', bracketEnd);
            
            if (bracketStart > 0 && bracketEnd > bracketStart && equalSign > bracketEnd) {
                String field = filter.substring(0, bracketStart);
                String operator = filter.substring(bracketStart + 1, bracketEnd);
                String value = filter.substring(equalSign + 1);

                // Проверяем, что поле допустимо для фильтрации
                if (!ALLOWED_FILTER_FIELDS.contains(field)) {
                    errors.add(Map.of(
                        "field", field,
                        "inner_message", "Неизвестное поле для фильтрации"
                    ));
                    continue;
                }
                
                try {
                    // Валидация значения в зависимости от поля
                    switch (field) {
                        case "id":
                            if (!value.equals("null")) {
                                try {
                                    Long.parseLong(value);
                                } catch (NumberFormatException e) {
                                    errors.add(Map.of(
                                        "field", field,
                                        "inner_message", "Это поле принимает только целочисленные значения"
                                    ));
                                    continue;
                                }
                            }
                            break;
                            
                        case "oscarsCount":
                            if (!value.equals("null")) {
                                try {
                                    long oscars = Long.parseLong(value);
                                    if (oscars <= 0) {
                                        errors.add(Map.of(
                                            "field", field,
                                            "inner_message", "Это поле должно быть больше 0, поле может быть null"
                                        ));
                                        continue;
                                    }
                                } catch (NumberFormatException e) {
                                    errors.add(Map.of(
                                        "field", field,
                                        "inner_message", "Это поле принимает только целочисленные значения"
                                    ));
                                    continue;
                                }
                            }
                            break;
                            
                        case "coordinates.x":
                            try {
                                int x = Integer.parseInt(value);
                                if (x > 775) {
                                    errors.add(Map.of(
                                        "field", field,
                                        "inner_message", "Значение должно быть меньше или равно 775"
                                    ));
                                    continue;
                                }
                            } catch (NumberFormatException e) {
                                errors.add(Map.of(
                                    "field", field,
                                    "inner_message", "Это поле принимает только целочисленные значения"
                                ));
                                continue;
                            }
                            break;
                            
                        case "coordinates.y":
                            try {
                                double y = Double.parseDouble(value);
                                if (y < -531) {
                                    errors.add(Map.of(
                                        "field", field,
                                        "inner_message", "Значение должно быть больше -531"
                                    ));
                                    continue;
                                }
                            } catch (NumberFormatException e) {
                                errors.add(Map.of(
                                    "field", field,
                                    "inner_message", "Это поле принимает только числовые значения"
                                ));
                                continue;
                            }
                            break;
                    }

                    try {
                        FilterOperator filterOperator = FilterOperator.valueOf(operator.toUpperCase());
                        criteria.add(new FilterCriteria(field, filterOperator, value));
                    } catch (IllegalArgumentException e) {
                        errors.add(Map.of(
                            "field", field,
                            "inner_message", "Неверный оператор фильтрации"
                        ));
                    }
                } catch (Exception e) {
                    errors.add(Map.of(
                        "field", field,
                        "inner_message", "Ошибка обработки фильтра"
                    ));
                }
            }
        }
        
        if (!errors.isEmpty()) {
            throw new ServiceException(errors.toString());
        }
        
        return criteria;
    }

    private boolean matchesAllCriteria(Movie movie, List<FilterCriteria> criteria) {
        return criteria.stream().allMatch(c -> matchesCriterion(movie, c));
    }

    private boolean matchesCriterion(Movie movie, FilterCriteria criteria) {
        Comparable value = getFieldValue(movie, criteria.getField());
        String criteriaValue = criteria.getValue();
        
        if ("null".equalsIgnoreCase(criteriaValue)) {
            switch (criteria.getOperator()) {
                case EQ:
                    return value == null;
                case NE:
                    return value != null;
                default:
                    return false;
            }
        }
        
        if (value == null) {
            return false;
        }

        Comparable parsedValue = parseValue(criteriaValue, value.getClass());
        if (parsedValue == null) {
            return false;
        }

        switch (criteria.getOperator()) {
            case EQ:
                return value.compareTo(parsedValue) == 0;
            case NE:
                return value.compareTo(parsedValue) != 0;
            case GT:
                return value.compareTo(parsedValue) > 0;
            case LT:
                return value.compareTo(parsedValue) < 0;
            case LTE:
                return value.compareTo(parsedValue) <= 0;
            case GTE:
                return value.compareTo(parsedValue) >= 0;
            default:
                return false;
        }
    }

    @SuppressWarnings("unchecked")
    private Comparable getFieldValue(Movie movie, String field) {
        switch (field) {
            case "id":
                return movie.getId();
            case "name":
                return movie.getName();
            case "creationDate":
                return movie.getCreationDate();
            case "coordinates.x":
                return movie.getCoordinates().getX();
            case "coordinates.y":
                return movie.getCoordinates().getY();
            case "oscarsCount":
                return movie.getOscarsCount();
            case "genre":
                return movie.getGenre() != null ? movie.getGenre().name() : null;
            case "mpaaRating":
                return movie.getMpaaRating();
            case "director.name":
                return movie.getDirector().getName();
            case "director.weight":
                return movie.getDirector().getWeight();
            case "director.passportID":
                return movie.getDirector().getPassportID();
            case "director.height":
                return movie.getDirector().getHeight();
            case "director.location.name":
                return movie.getDirector().getLocation().getName();
            case "director.location.x":
                return movie.getDirector().getLocation().getX();
            case "director.location.y":
                return movie.getDirector().getLocation().getY();
            default:
                return null;
        }
    }

    private Comparable parseValue(String value, Class<?> targetType) {
        try {
            if (targetType == Long.class) {
                return Long.parseLong(value);
            } else if (targetType == Integer.class) {
                return Integer.parseInt(value);
            } else if (targetType == Float.class) {
                return Float.parseFloat(value);
            } else if (targetType == Double.class) {
                return Double.parseDouble(value);
            } else if (targetType == Instant.class) {
                return Instant.parse(value).truncatedTo(ChronoUnit.MILLIS);
            } else if (targetType.isEnum()) {
                @SuppressWarnings("unchecked")
                Class<? extends Enum> enumType = (Class<? extends Enum>) targetType;
                return Enum.valueOf(enumType, value);
            }
            return value;
        } catch (Exception e) {
            return null;
        }
    }
} 