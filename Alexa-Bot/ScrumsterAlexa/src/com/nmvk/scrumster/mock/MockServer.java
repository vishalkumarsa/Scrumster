package com.nmvk.scrumster.mock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
/**
 * Mock server. This class is responsible for serving mock data.
 *
 * @author raghav
 */
public class MockServer {
    WireMockServer wireMockServer = new WireMockServer(options().port(8080));

    /**
     * Start mock server.
     */
    public void start() {
        wireMockServer.start();
        setUpData();
    }

    /**
     * Stop mock server.
     */
    public void stop() {
        wireMockServer.stop();
    }

    /**
     * Setup all data required.
     */
    private void setUpData() {
        WireMock.stubFor(get(urlEqualTo("/summary"))
                .willReturn(aResponse().withBody("Team has burnt 12 points, team is on track.")));

        WireMock.stubFor((post(urlPathMatching("/task/1.*"))
                .willReturn(aResponse().withBody(MockData.issues[0]).withStatus(201))));

        WireMock.stubFor((post(urlPathMatching("/task/5.*")))
                .willReturn(aResponse().withStatus(404)));

        // Calendar API
        WireMock.stubFor((get(urlPathMatching("/calendar/2017-10.*")))
                .willReturn(aResponse().withBody(MockData.calendar[0])));

        WireMock.stubFor((get(urlPathMatching("/calendar/2017-01.*")))
                .willReturn(aResponse().withBody(MockData.calendar[1])));

        //Calendar POST
        WireMock.stubFor(post(urlPathMatching("/calendar/2017-10.*"))
                .willReturn(aResponse().withStatus(201)));
    }
}
