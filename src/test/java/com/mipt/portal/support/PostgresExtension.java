package com.mipt.portal.support;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Singleton-style PostgreSQL Testcontainer for E2E tests.
 *
 * <p>Loosely follows the «JUnit Extension» pattern from lecture #11: the container is
 * started once per JVM, system properties are set so Spring picks up real JDBC coords,
 * and {@link CloseableResource} guarantees a single shutdown at the end of the suite.</p>
 *
 * <p>Wire it into a class with {@link WithPostgres @WithPostgres}.</p>
 */
public class PostgresExtension implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {

  private static final Logger log = LoggerFactory.getLogger(PostgresExtension.class);

  public static final Network PG_NETWORK = Network.newNetwork();
  private static final Lock LOCK = new ReentrantLock();
  private static final AtomicBoolean STARTED = new AtomicBoolean(false);

  private static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
          .withNetworkAliases("postgres")
          .withNetwork(PG_NETWORK)
          .withCommand("postgres", "-c", "max_connections=200");

  @Override
  public void beforeAll(ExtensionContext context) {
    LOCK.lock();
    try {
      if (!STARTED.compareAndExchange(false, true)) {
        log.info("Starting shared PostgreSQL test container");
        Startables.deepStart(POSTGRES).join();
        System.setProperty("spring.datasource.url", POSTGRES.getJdbcUrl());
        System.setProperty("spring.datasource.username", POSTGRES.getUsername());
        System.setProperty("spring.datasource.password", POSTGRES.getPassword());
        System.setProperty("spring.datasource.driver-class-name", POSTGRES.getDriverClassName());
        context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL)
            .put("POSTGRES Container", this);
      }
    } finally {
      LOCK.unlock();
    }
  }

  @Override
  public void close() {
    log.info("Closing shared PostgreSQL test container");
    POSTGRES.close();
    STARTED.set(false);
  }
}
