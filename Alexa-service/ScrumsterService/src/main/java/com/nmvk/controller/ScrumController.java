package com.nmvk.controller;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Transition;
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput;
import com.atlassian.util.concurrent.Promise;
import com.nmvk.config.Config;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

//import org.springframework.http.*;


/**
 * Controller to communicate with JIRA.
 *
 * @author raghav
 */
@RestController
@RequestMapping("/scrum")
public class ScrumController {

    final String prefix = "AT-";
    final String BOARD_ID = "1";

    /**
     * Httpclient for httprequest.
     */
    @Autowired
    private JiraRestClient jiraRestClient;

    @Autowired
    private HttpClient httpClient;

    // All helper methods goes below
    /*
    * Below method gives active sprintId for sprint board {boardId}
    * */
    private String getActiveSprintId(String boardId) throws IOException, JSONException {
        HttpGet getMethod = new HttpGet("https://scrumster.atlassian.net/rest/agile/1.0/board/"+ boardId +"/sprint?state=active");
        signRequest(getMethod);
        HttpResponse getResponse = httpClient.execute(getMethod);
        String stringResponse = convertResponseToString(getResponse);
        JSONObject jsonResponse = new JSONObject(stringResponse);
        JSONArray values = jsonResponse.getJSONArray("values");
        int id = ((JSONObject)values.get(0)).getInt("id");
        return Integer.toString(id);
    }

    @RequestMapping(value = "/task/{taskId}/{stateId}", method = RequestMethod.POST)
    public ResponseEntity<String> moveTasktoStatus(@PathVariable("taskId") String taskId, @PathVariable("stateId") String stateId ) throws Exception{
        try {
            Promise<Issue> issuePromise = jiraRestClient.getIssueClient().getIssue(prefix + taskId);
            Issue issue = issuePromise.claim();

            System.out.println("issue id" + taskId + " " + issue);
            if (issue == null) {
                return ResponseEntity.ok("Sorry, there is no task with task id " + taskId);
            }

            Promise<Iterable<Transition>> transitions = jiraRestClient.getIssueClient().getTransitions(issuePromise.claim().getTransitionsUri());
            Transition changeProgress = null;
            for (Transition trans : transitions.claim()) {
                if (trans.getId() == (Integer.parseInt(stateId))) {
                    changeProgress = trans;
                }
            }
            TransitionInput ti = new TransitionInput(changeProgress.getId(), null, null);
            jiraRestClient.getIssueClient().transition(issuePromise.claim().getTransitionsUri(), ti);

            System.out.println(taskId);
            Map<String, String> data = new HashMap<>();
            data.put("11","To do");
            data.put("21", "In progress");
            data.put("31", "Done");

            return ResponseEntity.ok(issue.getIssueType().getName()+ " " + taskId + " successfully moved to " + changeProgress.getName() + " state ");

        } catch (RestClientException exception) {
            return ResponseEntity.ok("Sorry, task " + taskId +" not found.");
        }
    }

    @RequestMapping(value = "/backlog/task/{taskId}", method = RequestMethod.POST)
    public ResponseEntity<String> moveTasktoBacklog(@PathVariable("taskId") String taskId) throws Exception {
         String issueArray = "{\"issues\":[\"" + prefix + taskId + "\"]}";
         System.out.println(issueArray);
        HttpPost postMethod = new HttpPost("https://scrumster.atlassian.net/rest/agile/1.0/backlog/issue");
        signRequest(postMethod);
        HttpEntity httpEntity = new StringEntity(issueArray);
        postMethod.setEntity(httpEntity);
        postMethod.setHeader("Accept", "application/json");
        postMethod.setHeader("Content-Type", "application/json");
        HttpResponse response = httpClient.execute(postMethod);
        String msg = "";
        if (response.getStatusLine().getStatusCode() == 400) {
            msg = "Task " + taskId + " does not exist";
        } else {
            msg = "Task " + taskId + " successfully moved to backlog";
        }
        return ResponseEntity.ok(msg);
    }

    @RequestMapping(value = "/active/task/{taskId}", method = RequestMethod.POST)
    public ResponseEntity<String> moveTasktoActiveSprint(@PathVariable("taskId") String taskId) throws Exception {
        String issueArray = "{\"issues\":[\"" + prefix + taskId + "\"]}";
        System.out.println(issueArray);

        String sprintId = getActiveSprintId(BOARD_ID);

        HttpPost postMethod = new HttpPost("https://scrumster.atlassian.net/rest/agile/1.0/sprint/"+sprintId+"/issue");
        signRequest(postMethod);
        HttpEntity httpEntity = new StringEntity(issueArray);
        postMethod.setEntity(httpEntity);
        postMethod.setHeader("Accept", "application/json");
        postMethod.setHeader("Content-Type", "application/json");
        HttpResponse response = httpClient.execute(postMethod);
        String msg = "";
        if (response.getStatusLine().getStatusCode() == 400) {
            msg = "Task " + taskId + " does not exist or there is no active sprint";
        } else {
            msg = "Task " + taskId + " successfully moved to the active sprint";
        }
        return ResponseEntity.ok(msg);
    }

    @RequestMapping(method = RequestMethod.GET)
    public String sayHello() throws Exception{
        HttpGet httpGet = new HttpGet("https://scrumster.atlassian.net/rest/api/latest/issue/AT-1");
        signRequest(httpGet);

        return convertResponseToString(httpClient.execute(httpGet));
    }

    @CrossOrigin
    @RequestMapping(value = "/summary", method = RequestMethod.GET)
    public ResponseEntity<String> getSummary() throws Exception{
        String sprintId = getActiveSprintId(BOARD_ID);

        HttpGet httpGet = new HttpGet("https://scrumster.atlassian.net/rest/agile/1.0/board/"+ BOARD_ID +"/sprint/"+ sprintId +"/issue");

        signRequest(httpGet);
        String response = convertResponseToString(httpClient.execute(httpGet));

        JSONObject jsonObject = new JSONObject(response);
        JSONArray issues = jsonObject.getJSONArray("issues");
        int totalpoints = 0;
        int burned = 0;

        for (int i = 0; i < issues.length(); i++) {
            JSONObject issue = (JSONObject) issues.get(i);
            JSONObject issueType = issue.getJSONObject("fields").getJSONObject("issuetype");
            //System.out.println(issueType.has("name"));
            if (issueType.has("name") && issueType.get("name").equals("Story")) {
                if (issue.getJSONObject("fields").has("customfield_10034"))
                totalpoints += issue.getJSONObject("fields").getInt("customfield_10034");
            }
            if (issue.getJSONObject("fields").has("status")) {
                JSONObject resolution = issue.getJSONObject("fields").getJSONObject("status");

                if (resolution.has("name") && resolution.get("name").equals("Done")) {
                    if (issue.getJSONObject("fields").has("customfield_10034"))
                    burned += issue.getJSONObject("fields").getInt("customfield_10034");
                }
            }
        }

        return ResponseEntity.ok("For the current sprint team has burned " + burned + " story points out of " + totalpoints + " story points.");
    }

    /**
     * Convert response to String.
     *
     * @param response
     * @return response string
     * @throws IOException
     */
    private String convertResponseToString(HttpResponse response) throws IOException {
        InputStream responseStream = response.getEntity().getContent();
        Scanner scanner = new Scanner(responseStream, "UTF-8");
        String responseString = scanner.useDelimiter("\\Z").next();
        scanner.close();
        return responseString;
    }

    public void signRequest(final HttpRequestBase request) {
        byte[] credentials = (Config.username + ':' + Config.password).getBytes();
        String encoded =  new String(Base64.encodeBase64(credentials));
        request.setHeader(Config.AUTHORIZATION_HEADER, "Basic " + encoded);
    }

}
