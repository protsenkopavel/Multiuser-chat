package net.protsenko.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.protsenko.model.Request;

public class InputParser {

    private static ObjectMapper om = new ObjectMapper();

    public static Request parseInputString(String string) throws JsonProcessingException {
        return om.readValue(string, Request.class);
    }

}
