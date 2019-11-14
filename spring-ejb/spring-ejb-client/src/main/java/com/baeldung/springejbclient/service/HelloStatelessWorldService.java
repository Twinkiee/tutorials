package com.baeldung.springejbclient.service;

import com.baeldung.ejb.tutorial.HelloStatelessWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HelloStatelessWorldService {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private final HelloStatelessWorld helloStatelessWorld;
  private final JdbcTemplate jdbcTemplate;

  public HelloStatelessWorldService(HelloStatelessWorld helloStatelessWorld
      , JdbcTemplate jdbcTemplate) {

    this.helloStatelessWorld = helloStatelessWorld;
    this.jdbcTemplate = jdbcTemplate;
  }


  @Transactional
  public String getHelloWorld() {

    jdbcTemplate.execute("INSERT INTO jta_table(column1) VALUES ( 'Bau' ) ");

    logger.info("LOCAL INSERT DONE!");

    String message = helloStatelessWorld.getHelloWorld();

    logger.info("REMOTE INSERT DONE!");

    return message;
  }
}
