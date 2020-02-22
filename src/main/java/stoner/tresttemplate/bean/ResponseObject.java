package stoner.tresttemplate.bean;

import lombok.Data;

@Data
public class ResponseObject <T> {
    private int code;
    private int datarows;
    private T data;
    private String datasource;
    private String message;
}
