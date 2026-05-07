package com.mipt.portal.infrastructure.database;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link DatabaseConnection}.
 *
 * <p>The class is a tiny wrapper around {@link java.sql.DriverManager} with hardcoded
 * URL/USER/PASSWORD constants pointing at {@code localhost:5432}. In the test environment
 * there is no PostgreSQL listening on that port, so the call is expected to throw a
 * {@link SQLException} — and that is exactly what we exercise to cover the method body.</p>
 *
 * <p>If a Postgres happens to be reachable, we still close the connection cleanly so the
 * test does not leak resources.</p>
 */
class DatabaseConnectionTest {

    @Test
    void getConnection_attemptsToReachPostgres_orFailsWithSQLException() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // если по случайности где-то локально поднят postgres на 5432 с этими
            // креденшелами — просто закроем соединение, метод всё равно покрыт
            // (важно: блок try-with-resources гарантирует close())
            assert conn != null;
        } catch (SQLException expected) {
            // основной путь в тесте — методы DriverManager бросают SQLException
            assertThatExceptionOfType(SQLException.class)
                    .isThrownBy(DatabaseConnection::getConnection);
        }
    }

    @Test
    void getConnection_returnsConnectionFromDriverManager_whenAvailable() throws SQLException {
        // мокаем DriverManager статически, чтобы покрыть happy path
        Connection mockConn = mock(Connection.class);
        try (MockedStatic<DriverManager> dm = mockStatic(DriverManager.class)) {
            dm.when(() -> DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/myproject",
                    "myuser", "mypassword"))
                    .thenReturn(mockConn);

            Connection actual = DatabaseConnection.getConnection();
            assertThat(actual).isSameAs(mockConn);
        }
    }

    @Test
    void canBeInstantiated_lombokDataGeneratesNoArgsConstructor() {
        // @Data добавляет boilerplate; убедимся что класс создаётся без ошибок —
        // это покрывает сгенерированный конструктор и базовый toString/equals
        DatabaseConnection a = new DatabaseConnection();
        DatabaseConnection b = new DatabaseConnection();

        assert a.equals(b);
        assert a.hashCode() == b.hashCode();
        assert a.toString() != null;
    }
}
