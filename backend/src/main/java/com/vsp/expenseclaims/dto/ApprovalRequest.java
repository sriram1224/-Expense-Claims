package com.vsp.expenseclaims.dto;

public class ApprovalRequest {

    private String comments;

    public ApprovalRequest() {}

    public ApprovalRequest(String comments) {
        this.comments = comments;
    }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}
