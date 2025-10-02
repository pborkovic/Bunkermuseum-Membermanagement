package com.bunkermuseum.membermanagement.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;

/**
 * Security headers configuration for OWASP compliance.
 *
 * <p>This configuration adds comprehensive security headers to all HTTP responses
 * to protect against common web vulnerabilities as defined by OWASP Top 10 and
 * industry best practices.</p>
 *
 * <h3>Security Headers Implemented:</h3>
 * <ul>
 *     <li><strong>Content-Security-Policy:</strong> Prevents XSS and data injection attacks</li>
 *     <li><strong>Strict-Transport-Security (HSTS):</strong> Enforces HTTPS connections</li>
 *     <li><strong>X-Content-Type-Options:</strong> Prevents MIME type sniffing</li>
 *     <li><strong>X-Frame-Options:</strong> Prevents clickjacking attacks</li>
 *     <li><strong>Referrer-Policy:</strong> Controls referrer information leakage</li>
 *     <li><strong>Permissions-Policy:</strong> Controls browser features and APIs</li>
 *     <li><strong>X-XSS-Protection:</strong> Enables browser XSS protection</li>
 * </ul>
 */
@Configuration
public class SecurityHeadersConfig {

    /**
     * Creates a filter that adds security headers to all HTTP responses.
     *
     * <p>This filter is applied to all requests and adds comprehensive security
     * headers to protect against various attack vectors.</p>
     *
     * <h3>Header Details:</h3>
     *
     * <h4>Content-Security-Policy:</h4>
     * <ul>
     *     <li><code>default-src 'self'</code> - Only allow resources from same origin</li>
     *     <li><code>script-src 'self' 'unsafe-inline' 'unsafe-eval'</code> - Required for Vaadin</li>
     *     <li><code>style-src 'self' 'unsafe-inline'</code> - Required for Vaadin styling</li>
     *     <li><code>img-src 'self' data: https:</code> - Allow images from HTTPS and data URIs</li>
     *     <li><code>font-src 'self' data:</code> - Allow fonts from same origin and data URIs</li>
     *     <li><code>connect-src 'self'</code> - Only allow AJAX requests to same origin</li>
     *     <li><code>frame-ancestors 'none'</code> - Prevent framing (clickjacking protection)</li>
     * </ul>
     *
     * <h4>Strict-Transport-Security (HSTS):</h4>
     * <ul>
     *     <li><code>max-age=31536000</code> - Enforce HTTPS for 1 year</li>
     *     <li><code>includeSubDomains</code> - Apply to all subdomains</li>
     *     <li><code>preload</code> - Eligible for browser HSTS preload list</li>
     * </ul>
     *
     * <h4>X-Content-Type-Options:</h4>
     * <ul>
     *     <li><code>nosniff</code> - Prevents MIME type sniffing attacks</li>
     * </ul>
     *
     * <h4>X-Frame-Options:</h4>
     * <ul>
     *     <li><code>DENY</code> - Prevents page from being displayed in frame/iframe</li>
     * </ul>
     *
     * <h4>Referrer-Policy:</h4>
     * <ul>
     *     <li><code>strict-origin-when-cross-origin</code> - Only send origin on HTTPS cross-origin</li>
     * </ul>
     *
     * <h4>Permissions-Policy:</h4>
     * <ul>
     *     <li>Disables potentially dangerous browser features (camera, microphone, geolocation, etc.)</li>
     * </ul>
     *
     * @return A configured Filter that adds security headers
     *
     * @author Philipp Borkovic
     */
    @Bean
    public Filter securityHeadersFilter() {
        return new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {

                HttpServletResponse httpResponse = (HttpServletResponse) response;

                httpResponse.setHeader("Content-Security-Policy",
                        "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                        "style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data: https:; " +
                        "font-src 'self' data:; " +
                        "connect-src 'self'; " +
                        "frame-ancestors 'none'; " +
                        "base-uri 'self'; " +
                        "form-action 'self'");

                httpResponse.setHeader("Strict-Transport-Security",
                        "max-age=31536000; includeSubDomains; preload");
                httpResponse.setHeader("X-Content-Type-Options", "nosniff");
                httpResponse.setHeader("X-Frame-Options", "DENY");
                httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
                httpResponse.setHeader("Permissions-Policy",
                        "camera=(), microphone=(), geolocation=(), payment=(), usb=(), " +
                        "magnetometer=(), gyroscope=(), accelerometer=()");
                httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
                httpResponse.setHeader("X-DNS-Prefetch-Control", "off");
                httpResponse.setHeader("X-Download-Options", "noopen");
                httpResponse.setHeader("X-Permitted-Cross-Domain-Policies", "none");

                chain.doFilter(request, response);
            }
        };
    }
}
