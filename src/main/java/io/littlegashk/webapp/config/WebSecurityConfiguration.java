package io.littlegashk.webapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {



  @Override
  protected void configure(HttpSecurity http) throws Exception {

    http.cors().configurationSource(corsConfigurationSource())
        .and()
        .csrf().disable()
        .authorizeRequests()
        //.antMatchers("/admin/**").hasAnyAuthority("SCOPE_https://api.littlegashk.ga/bot","SCOPE_https://api.littlegashk.ga/admin")
        .antMatchers("/bot/**").hasAuthority("SCOPE_https://api.littlegashk.ga/bot")
        .antMatchers("/**").permitAll()
        // this disables session creation on Spring Security
        .and()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .oauth2ResourceServer().jwt();
  }


  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration corsConfiguration = new CorsConfiguration();
    corsConfiguration.applyPermitDefaultValues();
    corsConfiguration.addAllowedMethod("*");
    corsConfiguration.addAllowedOrigin("http://localhost");
    source.registerCorsConfiguration("/**", corsConfiguration);
    return source;
  }
}
