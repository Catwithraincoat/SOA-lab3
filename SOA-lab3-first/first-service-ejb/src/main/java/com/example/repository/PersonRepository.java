package com.example.repository;

import com.example.model.Person;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import javax.annotation.PostConstruct; 
import com.example.model.Location;
import com.example.model.Coordinates;
import com.example.model.Movie;
import com.example.model.MovieGenre;
import com.example.model.MpaaRating;

@Stateless
public class PersonRepository {
    private static final Map<String, Person> directors = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        Person balabanov = new Person();
        balabanov.setName("Sergei Balabanov");
        balabanov.setHeight(180.0);
        balabanov.setWeight(100.0);
        balabanov.setPassportID("2024 123456");
        Location itmo = new Location();
        itmo.setX(100.1f);
        itmo.setY(100.1f);
        itmo.setName("ITMO University");
        balabanov.setLocation(itmo);
        directors.put(balabanov.getName(), balabanov);

        Person nolan = new Person();
        nolan.setName("Christopher Nolan");
        nolan.setHeight(181.5);
        nolan.setWeight(82.3);
        nolan.setPassportID("UK 789012");
        Location warner = new Location();
        warner.setX(34.1f);
        warner.setY(-118.3f);
        warner.setName("Warner Bros. Studios");
        nolan.setLocation(warner);
        directors.put(nolan.getName(), nolan);

        Person tarantino = new Person();
        tarantino.setName("Quentin Tarantino");
        tarantino.setHeight(188.0);
        tarantino.setWeight(95.2);
        tarantino.setPassportID("US 345678");
        Location hollywood = new Location();
        hollywood.setX(34.0f);
        hollywood.setY(-118.5f);
        hollywood.setName("Hollywood Hills");
        tarantino.setLocation(hollywood);
        directors.put(tarantino.getName(), tarantino);
    }

    public Person getDirector(String name) {
        return directors.get(name);
    }
} 
