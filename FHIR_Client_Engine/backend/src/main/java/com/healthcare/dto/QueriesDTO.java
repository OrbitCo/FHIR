package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class QueriesDTO {
    @JsonProperty("queries")
    private QueryDTO[] queries;

    public String toString() {
        String out = "";
        for(QueryDTO q : queries) {
            out += q.getQuery() + "; ";
        }
        out = out.substring(0, out.length() - 2);
        out += ".";
        return out;
    }
}
