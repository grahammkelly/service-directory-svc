package com.travelport.service.glossary.acceptance.common

import com.jayway.restassured.http.ContentType
import com.jayway.restassured.specification.RequestSpecification
import com.travelport.service.glossary.acceptance.ServerConfig

import static com.jayway.restassured.RestAssured.given
import static com.jayway.restassured.config.DecoderConfig.decoderConfig
import static com.jayway.restassured.config.EncoderConfig.encoderConfig
import static com.jayway.restassured.config.RestAssuredConfig.newConfig

class BaseApiClient {

  static RequestSpecification givenApiClient(final Map<String,String> headers = [:]) {
    String port = getPort()
    String baseUrl = getBaseUrl() + getContext()
    def config = newConfig()
        .encoderConfig(encoderConfig().defaultContentCharset('utf-8'))
        .decoderConfig(decoderConfig().defaultContentCharset('utf-8'))

    RequestSpecification api = given().contentType(ContentType.JSON).port(port as int)
        .config(config)
        .baseUri(baseUrl)
        .log().all()

    headers.forEach {k, v -> api.header(k, v)}

    api
  }

  static RequestSpecification givenAuthenticatedTenantApiClient(String tenantId, String user) {
    givenApiClient(stdAuthHeaders(tenantId, user))
  }

  static Map<String, String> stdAuthHeaders(String tenantId, String user) {
    ['Authorization': "Bearer ${user}Token", 'X-MTT-Tenant-ID': tenantId]
  }

  static String getApiClientUrl() {
    getBaseUrl() + ':' + getPort() + getContext()
  }

  private static String getPort() {
    ServerConfig.serverPort
  }

  private static getBaseUrl() {
    "http://localhost"
  }

  private static getContext() {
    ServerConfig.serverContextPath
  }
}
