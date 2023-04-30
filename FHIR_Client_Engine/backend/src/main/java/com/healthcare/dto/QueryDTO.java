package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONPropertyIgnore;

@Setter
@Getter
public class QueryDTO {
    @JsonProperty("order")
    private int order;
    @JsonProperty("returnValue")
    private String returnValue; //It's worth noting that this returnValue applies to the PREVIOUS query in the sequence
    @JsonProperty("connection")
    private String connection;
    @JsonProperty("query")
    private String query;
    @JsonProperty("type")
    private String type; //'full' or 'nested' (probably best as an ENUM)
    @JsonProperty("authentication")
    private AuthRequest authentication; //Null if we expect to already be authenticated

    //These two should really be trimmed, since they're URLs.
    public String getConnection() {
        return this.connection.trim();
    }
    public String getQuery() {
        return this.query.trim();
    }
}
