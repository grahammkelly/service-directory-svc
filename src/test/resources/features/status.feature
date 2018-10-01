Feature: Status checks

#  @Ignore
  Scenario: 1 - Application running should give a /status code of OK
    Given the application has started
    When we call '/status'
    Then the 'status' property should be 'OK'

