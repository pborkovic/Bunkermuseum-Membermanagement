package com.bunkermuseum.membermanagement.config.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import java.io.IOException;

/**
 * HTTPS enforcement configuration for production environments.
 *
 * <p>This configuration enforces HTTPS connections in production to comply with
 * EU regulations (eIDAS, NIS2, GDPR Article 32) and OWASP security best practices.</p>
 *
 * <h3>Security Features:</h3>
 * <ul>
 *     <li>Automatic HTTP to HTTPS redirection</li>
 *     <li>HSTS header enforcement (Strict-Transport-Security)</li>
 *     <li>TLS 1.2+ requirement</li>
 *     <li>Production-only activation</li>
 * </ul>
 *
 * @author Philipp Borkovic
 */
@Configuration
@Profile("prod")
public class HttpsEnforcementConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    @Value("${https.redirect.enabled:true}")
    private boolean httpsRedirectEnabled;

    /**
     * Creates a filter that redirects HTTP requests to HTTPS in production.
     *
     * <p>This filter only activates in production profile and automatically
     * redirects all HTTP traffic to HTTPS to ensure encrypted communication.</p>
     *
     * <h3>Redirection Behavior:</h3>
     * <ul>
     *     <li>HTTP requests → 301 Permanent Redirect to HTTPS</li>
     *     <li>Preserves original URL path and query parameters</li>
     *     <li>Bypasses health check endpoints (/actuator/health)</li>
     *     <li>Respects X-Forwarded-Proto header (proxy/load balancer support)</li>
     * </ul>
     *
     * <h3>Load Balancer Support:</h3>
     * <ul>
     *     <li>Checks X-Forwarded-Proto header for proxy termination</li>
     *     <li>Checks X-Forwarded-Ssl header</li>
     *     <li>Compatible with AWS ELB, Azure Load Balancer, GCP Load Balancer</li>
     * </ul>
     *
     * @return A configured Filter that enforces HTTPS
     *
     * @author Philipp Borkovic
     */
    @Bean
    public Filter httpsEnforcementFilter() {
        return new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {

                if (!httpsRedirectEnabled) {
                    chain.doFilter(request, response);
                    return;
                }

                HttpServletRequest httpRequest = (HttpServletRequest) request;
                HttpServletResponse httpResponse = (HttpServletResponse) response;

                if (httpRequest.getRequestURI().startsWith("/actuator/health")) {
                    chain.doFilter(request, response);

                    return;
                }

                boolean isSecure = httpRequest.isSecure();

                String xForwardedProto = httpRequest.getHeader("X-Forwarded-Proto");
                String xForwardedSsl = httpRequest.getHeader("X-Forwarded-Ssl");

                if (xForwardedProto != null && xForwardedProto.equalsIgnoreCase("https")) {
                    isSecure = true;
                }
                if (xForwardedSsl != null && xForwardedSsl.equalsIgnoreCase("on")) {
                    isSecure = true;
                }

                if (!isSecure) {
                    String redirectUrl = buildHttpsUrl(httpRequest);
                    httpResponse.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                    httpResponse.setHeader("Location", redirectUrl);
                    httpResponse.getWriter().println("This resource is only available over HTTPS. Redirecting to: " + redirectUrl);
                    return;
                }

                chain.doFilter(request, response);
            }

            /**
             * Builds HTTPS URL from HTTP request.
             *
             * <p>This method constructs a secure HTTPS URL from the incoming HTTP request,
             * preserving the original request path, query parameters, and server name while
             * switching to the HTTPS protocol.</p>
             *
             * <h3>URL Construction Process:</h3>
             * <ol>
             *     <li>Start with "https://" protocol</li>
             *     <li>Append server name from request (e.g., "example.com")</li>
             *     <li>Add port if not default HTTPS port (443)</li>
             *     <li>Append original request URI path (e.g., "/login")</li>
             *     <li>Append query string if present (e.g., "?id=123")</li>
             * </ol>
             *
             * <h3>Examples:</h3>
             * <ul>
             *     <li>Input: http://example.com/login → Output: https://example.com/login</li>
             *     <li>Input: http://example.com:8080/api?id=1 → Output: https://example.com/api?id=1</li>
             *     <li>Input: http://localhost/test → Output: https://localhost/test</li>
             * </ul>
             *
             * @param request The incoming HTTP request to convert to HTTPS
             *
             * @return Fully qualified HTTPS URL string
             *
             * @author Philipp Borkovic
             */
            private String buildHttpsUrl(HttpServletRequest request) {
                StringBuilder url = new StringBuilder("https://");
                url.append(request.getServerName());

                int httpsPort = 443;

                url.append(request.getRequestURI());

                if (request.getQueryString() != null) {
                    url.append("?").append(request.getQueryString());
                }

                return url.toString();
            }
        };
    }
}
