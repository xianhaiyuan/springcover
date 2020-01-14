package core.common;

import java.io.Serializable;

/**
 * 为统一返回结果做的顶层设计，主要包括状态码、结果说明内容和返回数据
 * @param <T>
 */
public class ResultMsg<T> implements Serializable {
    private static final long serialVersionUID = 2635002588308355758L;
    private int status;
    private String msg;
    private T data;
    public ResultMsg() {}
    public ResultMsg(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public ResultMsg(int status, String msg, T data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    public ResultMsg(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }
}
