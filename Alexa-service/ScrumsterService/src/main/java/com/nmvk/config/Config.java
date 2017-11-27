package com.nmvk.config;

import com.atlassian.httpclient.apache.httpcomponents.DefaultHttpClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.Calendar;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

/**
 * Spring config.
 *
 * @author raghav
 */
@Configuration
public class Config {
    public static final String username = "scrumuser2017@gmail.com";

    public static final String password = "scrumster2017";

    public static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * Jira client.
     *
     * @return Jira rest client.
     */
    @Bean
    public JiraRestClient getClient() throws URISyntaxException {
        JiraRestClientFactory restClientFactory = new AsynchronousJiraRestClientFactory();

        JiraRestClient restClient = restClientFactory.createWithBasicHttpAuthentication(new URI("https://scrumster.atlassian.net"),
                username, password);
        return restClient;
    }

    /**
     * Get default httpclient.
     *
     * @return HttpClient.
     */
    @Bean
    public HttpClient getHttpClient() {
        HttpClient httpclient = HttpClientBuilder.create().build();
        return httpclient;
    }

    /**
     * Get authenticated service client.
     *
     * @return Google calendar
     */
    @Bean
    public Calendar getCalendarService() throws Exception{
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        //Path path = Paths.get(getClass().getClassLoader()
          //      .getResource("client-secret.p12").toURI());
        String path = "/opt/client-secret.p12";
        GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport)
                .setJsonFactory(jsonFactory).setServiceAccountId("calender@ourcal-185819.iam.gserviceaccount.com")
                .setServiceAccountPrivateKeyFromP12File(new File(path.toString()))
                .setServiceAccountScopes(Collections.singleton(CalendarScopes.CALENDAR))
                .setServiceAccountUser("scrum@ouruse.com")
                .build();

        Calendar service = new com.google.api.services.calendar.Calendar.Builder(httpTransport, jsonFactory, credential).setApplicationName("OurCal").build();

        return service;
    }

}
