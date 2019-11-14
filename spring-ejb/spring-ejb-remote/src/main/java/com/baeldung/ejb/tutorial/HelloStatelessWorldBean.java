package com.baeldung.ejb.tutorial;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless(name = "HelloStatelessWorld")
public class HelloStatelessWorldBean implements HelloStatelessWorld {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(TxType.MANDATORY)
    public String getHelloWorld() {

        logger.info("getHelloWorld stateless invocation");

        logger.info("Transaction status: {}", transactionSynchronizationRegistry.getTransactionStatus());

        Query query = entityManager.createNativeQuery("INSERT INTO table (column1) VALUES (?)");
        query.setParameter(1, "Ciao");

        query.executeUpdate();

//        if (true) {
//            throw new RuntimeException("REMOTE ROLLBACK!");
//        }

        return "Hello Stateless World!";
    }
 
}
