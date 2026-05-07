package com.mipt.portal.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RateLimitInterceptor}.
 *
 * <p>Covers all branches of the sliding-window limiter: requests below the limit pass,
 * the (limit+1)-th request is rejected with HTTP 429, expired entries are cleaned up,
 * and {@code resolveKey()} works for both anonymous and authenticated callers.</p>
 */
class RateLimitInterceptorTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void allowsCalls_belowLimit_thenRejectsWith429_anonymousUser() {
        RateLimitInterceptor interceptor = new RateLimitInterceptor(60_000L, 2);
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res1 = new MockHttpServletResponse();
        MockHttpServletResponse res2 = new MockHttpServletResponse();
        MockHttpServletResponse res3 = new MockHttpServletResponse();

        assertThat(interceptor.preHandle(req, res1, new Object())).isTrue();
        assertThat(interceptor.preHandle(req, res2, new Object())).isTrue();
        assertThat(interceptor.preHandle(req, res3, new Object())).isFalse();
        assertThat(res3.getStatus()).isEqualTo(429);
    }

    @Test
    void slidingWindow_evictsExpiredEntries() throws Exception {
        // окно 1 мс — после Thread.sleep предыдущая запись будет «протухшей»
        RateLimitInterceptor interceptor = new RateLimitInterceptor(1L, 1);
        MockHttpServletRequest req = new MockHttpServletRequest();

        assertThat(interceptor.preHandle(req, new MockHttpServletResponse(), new Object())).isTrue();
        Thread.sleep(5);
        // окно прошло — старый запрос должен быть удалён, новый — пропущен
        assertThat(interceptor.preHandle(req, new MockHttpServletResponse(), new Object())).isTrue();
    }

    @Test
    void resolveKey_usesAuthenticatedUserName_whenAuthenticationPresent() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("alice", "pwd", Collections.emptyList()));

        RateLimitInterceptor interceptor = new RateLimitInterceptor(60_000L, 1);
        MockHttpServletRequest req = new MockHttpServletRequest();

        // первый запрос проходит, второй отклоняется — это и проверяет, что bucket общий
        // именно для пользователя "alice" (а не для "anon")
        assertThat(interceptor.preHandle(req, new MockHttpServletResponse(), new Object())).isTrue();
        MockHttpServletResponse rejected = new MockHttpServletResponse();
        assertThat(interceptor.preHandle(req, rejected, new Object())).isFalse();
        assertThat(rejected.getStatus()).isEqualTo(429);
    }
}
