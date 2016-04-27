package edu.augustana.quadsquad.householdmanager.data.firebaseobjects;

import java.util.Calendar;

/**
 * Created by micha on 4/26/2016.
 */
public class ToDoItem {
    protected String actionText;
    protected Calendar dueDate;
    protected String avatarUrl;
    protected String assignedBy;
    protected String assignedTo;
    protected String groupTag;
    protected boolean completed;

    public ToDoItem() {

    }

    public ToDoItem(String actionText, String assignedBy, String assignedTo, String avatarUrl, boolean completed, Calendar dueDate, String groupTag) {
        this.actionText = actionText;
        this.assignedBy = assignedBy;
        this.assignedTo = assignedTo;
        this.avatarUrl = avatarUrl;
        this.completed = completed;
        this.dueDate = dueDate;
        this.groupTag = groupTag;
    }

    public String getActionText() {
        return actionText;
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Calendar getDueDate() {
        return dueDate;
    }

    public String getGroupTag() {
        return groupTag;
    }
}
