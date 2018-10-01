package com.travelport.service.directory.repository

import com.travelport.service.directory.model.Tag
import org.springframework.data.mongodb.repository.MongoRepository

interface TagRepository: MongoRepository<Tag, String>
