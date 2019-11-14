package com.baeldung.springejbclient;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.atomikos.jdbc.AtomikosSQLException;
import javax.sql.DataSource;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import org.postgresql.xa.PGXADataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.wildfly.transaction.client.RemoteTransactionContext;
import org.wildfly.transaction.client.RemoteUserTransaction;

@Configuration
public class AtomikosJtaConfiguration {

  @Autowired
  private Environment environment;

//	@Bean
//	public UserTransaction userTransaction() throws Throwable {
//		UserTransactionImp userTransactionImp = new UserTransactionImp();
//		userTransactionImp.setTransactionTimeout(1000);
//		return userTransactionImp;
//	}

  @Bean(initMethod = "init", destroyMethod = "close")
  public TransactionManager transactionManager() throws Throwable {
    UserTransactionManager userTransactionManager = new UserTransactionManager();
    userTransactionManager.setForceShutdown(false);
    return userTransactionManager;
  }

  @Bean(destroyMethod = "close")
  public DataSource dataSource() throws AtomikosSQLException {
    PGXADataSource pgxaDataSource = new PGXADataSource();
    pgxaDataSource.setUrl("jdbc:postgresql://localhost:5432/postgres");
    pgxaDataSource.setPassword("docker");
    pgxaDataSource.setUser("postgres");

    AtomikosDataSourceBean xaDataSource = new AtomikosDataSourceBean();
    xaDataSource.setXaDataSource(pgxaDataSource);
    xaDataSource.setUniqueResourceName("xads");
    xaDataSource.setPoolSize(5);
    xaDataSource.init();
    return xaDataSource;
  }

  @Bean
  public JtaTransactionManager jtaTransactionManager(TransactionManager transactionManager
      /*, @Qualifier("userTransaction") UserTransaction userTransaction*/) {

    JtaTransactionManager jtaTransactionManager = new JtaTransactionManager() {
      @Override
      public UserTransaction getUserTransaction() {

        Logger logger = LoggerFactory.getLogger(getClass());
        return new UserTransactionImp() {

          @Override
          public void begin() throws NotSupportedException, SystemException {
            RemoteUserTransaction remoteUserTransaction = RemoteTransactionContext.getInstance()
                .getUserTransaction();
            remoteUserTransaction.begin();

            try {
              super.begin();
            } catch (Exception e) {
              logger.error("An error occurred while beginning local transaction. Setting remote transaction for rollback only.");
              remoteUserTransaction.setRollbackOnly();

              throw e;
            }
          }

          @Override
          public void commit()
              throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SystemException, IllegalStateException, SecurityException {

            RemoteUserTransaction remoteUserTransaction = RemoteTransactionContext.getInstance()
                .getUserTransaction();

            try {
							remoteUserTransaction.commit();
						} catch (Exception e) {
							logger.error("An error occurred while committing remote transaction. Setting remote and local transaction for rollback only.");

              try {
                // Unlikely to succeed but still
                remoteUserTransaction.setRollbackOnly();
              } finally {
                super.setRollbackOnly();
              }
              throw e;
						}
            super.commit();
          }

          @Override
          public void rollback() throws IllegalStateException, SystemException, SecurityException {
            RemoteUserTransaction remoteUserTransaction = RemoteTransactionContext.getInstance()
                .getUserTransaction();

            try {
              remoteUserTransaction.rollback();
            } catch (Exception e) {
              logger.error("An error occurred while rolling back remote transaction. Setting local transaction for rollback only.");
              throw e;
            } finally {
              super.setRollbackOnly();
            }
          }

          @Override
          public void setRollbackOnly() throws IllegalStateException, SystemException {
            RemoteUserTransaction remoteUserTransaction = RemoteTransactionContext.getInstance()
                .getUserTransaction();

            try {
              remoteUserTransaction.setRollbackOnly();
            } catch (Exception e) {
              logger.warn(
                  "An error occurred while setting remote transaction for rollback only. Rolling back remote and local transaction.",
                  e);

              try {
                // Unlikely to succeed but still
                remoteUserTransaction.rollback();
              } catch (Exception remoteException) {
                // Do nothing. We're setting local transaction for rollback anyway
              }
            } finally {
              super.setRollbackOnly();
            }
          }

          @Override
          public int getStatus() throws SystemException {
            int status = super.getStatus();
            RemoteUserTransaction remoteUserTransaction = RemoteTransactionContext.getInstance()
                .getUserTransaction();
            int remoteStatus = remoteUserTransaction.getStatus();
            if (status != remoteStatus) {
              logger.error("Local status [ " + status + " ] is inconsistent with remote status [ "
                  + remoteStatus + " ]. Setting remote and local connection for rollback only.");

              try {
                remoteUserTransaction.setRollbackOnly();
              } finally {
                super.setRollbackOnly();
              }

              throw new IllegalStateException(
                  "Local status [ " + status + " ] is inconsistent with remote status [ "
                      + remoteStatus + " ]");
            }
            return status;
          }

          @Override
          public void setTransactionTimeout(int seconds) throws SystemException {
            super.setTransactionTimeout(seconds);
            RemoteTransactionContext.getInstance().getUserTransaction()
                .setTransactionTimeout(seconds);
          }

//					@Override
//					public Reference getReference() throws NamingException {
//						return super.getReference();
//					}
        };

      }
    };

    jtaTransactionManager.setTransactionManager(transactionManager);
//		jtaTransactionManager.setUserTransaction(userTransaction);
    return jtaTransactionManager;
  }

}