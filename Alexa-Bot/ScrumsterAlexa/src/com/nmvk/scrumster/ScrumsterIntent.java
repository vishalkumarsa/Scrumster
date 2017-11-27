package com.nmvk.scrumster;

/**
 * Contains list of intent used in the application.
 *
 * @author Sweekrut
 */
public interface ScrumsterIntent {

    /**
     * Alexa help intent.
     */
    String HELP_INTENT = "AMAZON.HelpIntent";

    /**
     * Move work item.
     */
    String MOVE_WORK_ITEM = "MoveWorkItem";

    /**
     * Remove work item.
     */
    String REMOVE_WORK_ITEM = "RemoveWorkItem";

    /**
     * Schedule meeting.
     */
    String SCHEDULE_MEETING = "ScheduleMeeting";

    /**
     * Summary.
     */
    String SUMMARY = "Summary";
}
