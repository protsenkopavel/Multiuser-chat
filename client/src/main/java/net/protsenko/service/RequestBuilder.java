package net.protsenko.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.protsenko.model.EventType;
import net.protsenko.model.PreRequest;

public class RequestBuilder {

    ObjectMapper om = new ObjectMapper();

    public String createRequest(EventType eventType, String credentials, String message) throws JsonProcessingException {
        return om.writeValueAsString(new PreRequest(eventType, credentials, message));
    }
}
