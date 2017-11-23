package org.datazup.apiinternal;

/**
 * Created by ninel on 11/22/17.
 */
public class CommonApiResponse {
    private Object result;
    private Class type;

    public CommonApiResponse(){}
    public CommonApiResponse(Object result, Class type){
        this.result = result;
        this.type = type;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }
}
