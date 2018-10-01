package com.travelport.service.directory.acceptance.steps

import com.travelport.service.directory.acceptance.common.BaseApiClient

this.metaClass.mixin(cucumber.api.groovy.Hooks)
this.metaClass.mixin(cucumber.api.groovy.EN)

Given(~/^the application has started$/) { ->
  assert BaseApiClient.givenAuthenticatedTenantApiClient('mock', 'appUser')
      .get('/status').statusCode == 200
}

When(~/^we call '([^']+)'$/) { String endpoint ->
  appStatus = BaseApiClient.givenAuthenticatedTenantApiClient('mock', 'appUser')
      .get(endpoint).body().as(Map)
}

Then(~/^the '([^']+)' property should be '([^']+)'$/) { String field, String contents ->
  assert (appStatus[field] as String) == contents
}
