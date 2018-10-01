package com.travelport.service.directory.acceptance

class ServerConfig {
  static final String springActiveProfile = 'default, metrics, mock'
  static final int serverPort
  static final String serverContextPath = ''

  static {
    def rnd = new Random(System.currentTimeMillis())
    serverPort = rnd.nextInt(10_000) + 10_000
  }
}
