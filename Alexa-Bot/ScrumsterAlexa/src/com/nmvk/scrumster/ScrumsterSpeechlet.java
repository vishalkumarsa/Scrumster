package com.nmvk.scrumster;

import com.amazon.speech.speechlet.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.OutputSpeech;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Scanner;

/**
 * Entry point of scrumster application.
 *
 * @author raghav
 */
public class ScrumsterSpeechlet implements SpeechletV2 {
    private static final Logger log = LoggerFactory.getLogger(ScrumsterSpeechlet.class);
    //private MockServer mockServer = new MockServer();

    CloseableHttpClient httpClient = HttpClients.createDefault();

    @Override
    public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope) {
        log.info("onSessionStarted requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());
    }

    @Override
    public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
        log.info("onLaunch requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());
        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        IntentRequest request = requestEnvelope.getRequest();
        Session currentSession = requestEnvelope.getSession();
        //mockServer.start();
        log.info("onIntent requestId={}, sessionId={}, Intent Name={}", request.getRequestId(),
                requestEnvelope.getSession().getSessionId(), request.getIntent().getName());

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;
        if (ScrumsterIntent.MOVE_WORK_ITEM.equals(intentName)) {
            return handleMoveTask(intent, currentSession);
        } else if (ScrumsterIntent.SCHEDULE_MEETING.equals(intentName)) {
            return handleMeeting(intent, currentSession);
        } else if (ScrumsterIntent.SUMMARY.equals(intentName)) {
            return handleSummary(intent, currentSession);
        } else if ("AMAZON.CancelIntent".equals(intentName) || "AMAZON.StopIntent".equals(intentName)) {
            return handleIntentStop(intent, currentSession);
        } else if ("AMAZON.HelpIntent".equals(intentName)){
            return handleIntentHelp(intent, currentSession);
        } else {
            //mockServer.stop();
            return getAskResponse(Util.title, "This is unsupported.  Please try something else.");
        }
    }

    @Override
    public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope) {
        log.info("onSessionEnded requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());
        // any cleanup logic goes here
    }

    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getWelcomeResponse() {
        String speechText = "Hi! Your scrum has now started.";
        return getAskResponse(Util.title, speechText);
    }

    /*private String GetFormat (String date, int slot, boolean isStart)
    {
        String start[]={"09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "12:00", "12:30", "01:00", "01:30", "02:00", "02:30", "03:00", "03:30", "04:00", "04:30"};
        String end[]={"09:30", "10:00", "10:30", "11:00", "11:30", "12:00", "12:30", "01:00", "01:30", "02:00", "02:30", "03:00", "03:30", "04:00", "04:30", "05:00"};

        if (isStart) {
            String startdatetime = null;
            startdatetime += date + " "+start [slot-1];

            LocalDateTime x = LocalDateTime.parse(startdatetime, DateTimeFormatter.ofPattern("yyyy-M-d HH:mm"));
            x.atZone(ZoneId.of("America/New_York"));
            return x.toString();
        } else {
            String enddatetime = null;
            enddatetime += date+" "+ end [slot-1];

            LocalDateTime x = LocalDateTime.parse(enddatetime, DateTimeFormatter.ofPattern("yyyy-M-d HH:mm"));
            x.atZone(ZoneId.of("America/New_York"));
            x.pl
            return x.toString();
        }
    }*/

    /**
     * Handle summary.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse handleSummary(Intent intent, Session currentSession) {
        if (!currentSession.isNew()) {
            log.info("In Summary, Old session came back, session ID={}, slots={}", currentSession.getSessionId(), intent.getSlots().toString());
            return handleMoveTask(intent, currentSession);
        }

        String speechText=null;

        try {
            HttpGet httpGet = new HttpGet(Util.host + "/scrum/summary");

            HttpResponse httpResponse = httpClient.execute(httpGet);

            speechText = convertResponseToString(httpResponse);

        } catch (Exception exception) {
            log.error(exception.getMessage());
        }
        // Create the Simple card content.
        SimpleCard card = getSimpleCard(Util.title, speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    private SpeechletResponse handleIntentStop(Intent intent, Session currentSession) {
        String speechText = "Oh!, ok";

        // Create the Simple card content.
        SimpleCard card = getSimpleCard(Util.title, speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    private SpeechletResponse handleIntentHelp(Intent intent, Session currentSession) {
        String speechText = "Hi, my name is Scrumster. Ask me to move a task, or, schedule a meeting, or, ask for sprint summary.";

        // Create the Simple card content.
        SimpleCard card = getSimpleCard(Util.title, speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    /**
     * Handle move task.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse handleMoveTask(Intent intent, Session currentSession) {

        String taskId = intent.getSlot("taskid") == null ? null :intent.getSlot("taskid").getValue();

        if (taskId == null) {
            taskId = (String)currentSession.getAttribute("sessionTaskId");
            log.info("In Move Task, taskid came null in slot session ID={}, task ID={}", currentSession.getSessionId(), taskId);
            if (taskId == null) {
                return getAskResponse(Util.title, "Sure, can you please tell me the task id?");
            }
        }

        currentSession.setAttribute("sessionTaskId", taskId);

        log.info("In Move Task, session ID={}, task ID={}", currentSession.getSessionId(), taskId);
        String status = intent.getSlot("status") == null ? null : intent.getSlot("status").getValue();
        if (status == null) {
            log.info("In Move Task, session ID={}, task ID={}, status is null", currentSession.getSessionId(), taskId);
            return getAskResponse(Util.title, "Where do you want me to move it?");
        }

        log.info("In Move Task, session ID={}, task ID={}, status={}", currentSession.getSessionId(), taskId, status);

        String requestStatus = null;
        boolean error = false;
        switch (status) {
            case "do":
            case "to do":
            case "to-do":       requestStatus="11";
                                break;
            case "progress":
            case "in progress":
            case "in-progress": requestStatus="21";
                                break;
            case "done":        requestStatus="31";
                                break;
            case "log":
            case "back":
            case "backlock":
            case "back lock":
            case "back-lock":
            case "back log":
            case "back-log":
            case "backlog":     requestStatus="backlog";
                                break;
            case "current":
            case "sprint":
            case "current-sprint":
            case "current sprint":
            case "active sprint":
            case "active-sprint":
            case "active":      requestStatus="active";
                                break;
            default: error = true;
        }

        String speechText = "There is no task with task id " + taskId ;

        if (!error) {

            //"Your task " + taskId + " has been successfully moved to " + status

            //speechText = "Your task " + taskId + " has been successfully moved to " + status;

            try {

                HttpPost httpPost = null;
                if ("backlog".equals(requestStatus)) {
                    httpPost = new HttpPost(Util.host + "/scrum/backlog/task/" + taskId);
                } else if ("active".equals(requestStatus)){
                    httpPost = new HttpPost(Util.host + "/scrum/active/task/" + taskId);
                } else {
                    httpPost = new HttpPost(Util.host + "/scrum/task/" + taskId + "/" + requestStatus);
                }
                HttpResponse httpResponse = httpClient.execute(httpPost);

                //if (httpResponse.getStatusLine().getStatusCode() == 201) {
                speechText = convertResponseToString(httpResponse);//"Your task " + taskId + " has been successfully moved to " + status;
                //}
            } catch (IOException exception) {
                log.error(exception.getMessage());
            }
        } else {
            speechText = "Invalid status, " + status;
        }

        // Create the Simple card content.
        SimpleCard card = getSimpleCard(Util.title, speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    /**
     * Handle meetings.
     *
     * @param intent
     * @return SpeechletResponse
     */
    private SpeechletResponse handleMeeting(Intent intent, Session currentSession) {

        String date = intent.getSlot("day") == null ? null :intent.getSlot("day").getValue();

        if (date == null) {
            log.info("In Meeting request, date is null session ID={}", currentSession.getSessionId());
            return getAskResponse(Util.title, "Sure, can you tell me when to schedule?");
        }

        log.info("In Meeting request, session ID={}, date={}", currentSession.getSessionId(), date);

        String speechText = "Could not schedule meeting on " + date;

        log.info("In Schedule Meeting, session ID={}, date={}", currentSession.getSessionId(), date);

        try {
            HttpPost httpPost = new HttpPost(Util.host + "/calendar/" + date);

            HttpResponse httpResponse = httpClient.execute(httpPost);

            speechText = convertResponseToString(httpResponse);
        } catch (Exception exception) {
            log.error(exception.getMessage());
        }

        //speechText = "Successfully scheduled meeting on " + date + " from ten a.m. to ten thirty a.m.";

        // Create the Simple card content.
        SimpleCard card = getSimpleCard(Util.title, speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    /**
     * Creates a {@code SpeechletResponse} for the help intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getHelpResponse() {
        String speechText = "You can say hello to me!";
        return getAskResponse("HelloWorld", speechText);
    }

    /**
     * Helper method that creates a card object.
     * @param title title of the card
     * @param content body of the card
     * @return SimpleCard the display card to be sent along with the voice response.
     */
    private SimpleCard getSimpleCard(String title, String content) {
        SimpleCard card = new SimpleCard();
        card.setTitle(title);
        card.setContent(content);

        return card;
    }

    /**
     * Helper method for retrieving an OutputSpeech object when given a string of TTS.
     * @param speechText the text that should be spoken out to the user.
     * @return an instance of SpeechOutput.
     */
    private PlainTextOutputSpeech getPlainTextOutputSpeech(String speechText) {
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return speech;
    }

    /**
     * Helper method that returns a reprompt object. This is used in Ask responses where you want
     * the user to be able to respond to your speech.
     * @param outputSpeech The OutputSpeech object that will be said once and repeated if necessary.
     * @return Reprompt instance.
     */
    private Reprompt getReprompt(OutputSpeech outputSpeech) {
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(outputSpeech);

        return reprompt;
    }

    /**
     * Helper method for retrieving an Ask response with a simple card and reprompt included.
     * @param cardTitle Title of the card that you want displayed.
     * @param speechText speech text that will be spoken to the user.
     * @return the resulting card and speech text.
     */
    private SpeechletResponse getAskResponse(String cardTitle, String speechText) {
        SimpleCard card = getSimpleCard(cardTitle, speechText);
        String repromptText = "sorry, can you say that again?";
        PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);
        PlainTextOutputSpeech repromptSpeech = getPlainTextOutputSpeech(repromptText);
        Reprompt reprompt = getReprompt(repromptSpeech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    /**
     * Convert response to String.
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
}
