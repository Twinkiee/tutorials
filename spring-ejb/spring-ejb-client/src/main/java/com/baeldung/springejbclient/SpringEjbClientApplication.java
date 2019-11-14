package com.baeldung.springejbclient;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.baeldung.ejb.tutorial.HelloStatefulWorld;
import com.baeldung.ejb.tutorial.HelloStatelessWorld;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

@SpringBootApplication
@EnableTransactionManagement
public class SpringEjbClientApplication {

  @Bean
  public Context context() throws NamingException {
    Properties jndiProps = new Properties();
    jndiProps
        .put("java.naming.factory.initial", "org.jboss.naming.remote.client.InitialContextFactory");
    jndiProps.put("jboss.naming.client.ejb.context", true);
    jndiProps.put("java.naming.provider.url", "http-remoting://localhost:8080");
    return new InitialContext(jndiProps);
  }

  @Bean
  public HelloStatelessWorld helloStatelessWorld(Context context) throws NamingException {
    return (HelloStatelessWorld) context.lookup(this.getFullName(HelloStatelessWorld.class));
  }

  @Bean
  public HelloStatefulWorld helloStatefulWorld(Context context) throws NamingException {
    return (HelloStatefulWorld) context.lookup(this.getFullName(HelloStatefulWorld.class));
  }

  @SuppressWarnings("rawtypes")
  private String getFullName(Class classType) {
    String moduleName = "spring-ejb-remote/";
    String beanName = classType.getSimpleName();
    String viewClassName = classType.getName();

    return moduleName + beanName + "!" + viewClassName;
  }

  public static void main(String[] args) {
    SpringApplication.run(SpringEjbClientApplication.class, args);
  }

}
