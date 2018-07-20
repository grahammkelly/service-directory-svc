package com.mttnow.system.team.repository;

import com.mttnow.system.team.model.Person;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PersonRepository extends MongoRepository<Person, String> {

}
