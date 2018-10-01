package com.travelport.service.directory.acceptance.common

TestSetup.metaClass.mixin(cucumber.api.groovy.Hooks)
TestSetup.metaClass.mixin(cucumber.api.groovy.EN)

class TestContext {
  Map<String, Object> appStatus = [:]
}

World {
  new TestContext()
}
