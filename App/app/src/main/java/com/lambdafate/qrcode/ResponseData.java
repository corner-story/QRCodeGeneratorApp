package com.lambdafate.qrcode;

public class ResponseData {
    private String status;
    private String url;
    private String error;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "ResponseData{" +
                "status='" + status + '\'' +
                ", url='" + url + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}
