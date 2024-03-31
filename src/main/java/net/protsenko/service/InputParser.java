package net.protsenko.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.protsenko.model.Request;

public class InputParser {

    public static Request parseInputString(String string) throws JsonProcessingException {
        return new ObjectMapper().readValue(string, Request.class);
    }

}
