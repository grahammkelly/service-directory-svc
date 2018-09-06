package com.travelport.example.repository

import com.travelport.example.model.Person
import org.springframework.data.mongodb.repository.MongoRepository

interface PersonRepository : MongoRepository<Person, String>
