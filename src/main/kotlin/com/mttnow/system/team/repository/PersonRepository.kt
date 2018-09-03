package com.mttnow.system.team.repository

import com.mttnow.system.team.model.Person
import org.springframework.data.mongodb.repository.MongoRepository

interface PersonRepository : MongoRepository<Person, String>
