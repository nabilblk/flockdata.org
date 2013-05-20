package com.auditbucket.audit.bean;

/**
 * User: mike
 * Date: 8/05/13
 * Time: 7:41 PM
 */
public class AuditInputBean {
    //'{"eventType":"change","auditKey":"c27ec2e5-2e17-4855-be18-bd8f82249157","fortressUser":"miketest","when":"2012-11-10","what":"{name: 22}"}'
    String auditKey;
    String eventType;
    String fortressUser;
    String when;
    String what;
    String yourRef;
    private String comment;

    public AuditInputBean() {
    }

    public AuditInputBean(String auditKey, String fortressUser, String when, String what) {
        this.auditKey = auditKey;
        this.fortressUser = fortressUser;
        this.when = when;
        this.what = what;

    }

    public String getAuditKey() {
        return auditKey;
    }

    public void setAuditKey(String auditKey) {
        this.auditKey = auditKey;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getFortressUser() {
        return fortressUser;
    }

    public void setFortressUser(String fortressUser) {
        this.fortressUser = fortressUser;
    }

    public String getWhen() {
        return when;
    }

    public void setWhen(String when) {
        this.when = when;
    }

    public String getWhat() {
        return what;
    }

    public void setWhat(String what) {
        this.what = what;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getYourRef() {
        return yourRef;
    }

    public void setYourRef(String yourRef) {
        this.yourRef = yourRef;
    }
}
