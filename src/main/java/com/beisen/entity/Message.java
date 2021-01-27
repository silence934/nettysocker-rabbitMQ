package com.beisen.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Author : silence
 * @Date: 2021-01-22 11:24
 * @Description :
 */
@Data
public class Message implements Serializable {
    private String content;

    private String from;

    private String to;

    @Override
    public String toString() {
        return String.format("来自【%s】的消息 : %s 时间-[%s]", getFrom(), getContent(), LocalDateTime.now());
    }
}
