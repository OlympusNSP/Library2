package ru.olympusnsp.library.model;

public class ErrorItem {
    private String message;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    private Integer status;


    public ErrorItem(){
        message="";
        status=0;
    }
    public ErrorItem(String message, Integer status) {
        this.message = message;
        this.status = status;
    }
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
