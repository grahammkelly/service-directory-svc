package com.mttnow.system.team.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotEmpty;

@Document
@Data
@EqualsAndHashCode
public class Person {

  @Id
  String id;

  @NotEmpty
  String firstName;
  String lastName;
  int age;

}
