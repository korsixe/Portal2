package com.mipt.portal.support;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Composite "E2E" annotation closely modelled on the lecture example.
 *
 * <p>Boots the slim test app on a random port, activates the {@code test} + {@code e2e}
 * profiles, plugs in the shared PostgreSQL container via {@link WithPostgres @WithPostgres}
 * and tags the test as {@code e2e} so it can be excluded by default in fast local runs:</p>
 *
 * <pre>{@code mvn test -DexcludedGroups=e2e}</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@SpringBootTest(classes = AbstractWebMvcTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "e2e"})
@WithPostgres
@AutoConfigureMockMvc
@Tag("e2e")
public @interface E2ETest {

  @AliasFor(annotation = SpringBootTest.class, attribute = "properties")
  String[] properties() default {};
}
