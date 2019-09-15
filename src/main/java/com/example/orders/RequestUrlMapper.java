package com.example.orders;

import static  fi.iki.elonen.NanoHTTPD.Method.*;
import static fi.iki.elonen.NanoHTTPD.Response.Status.*;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;


public class RequestUrlMapper {

    private static final String ADD_ORDER_URL = "/order/add";
    private static final String GET_ORDER_URL = "/order/get";
    private static final String GET_ALL_ORDER_URL = "/order/getAll";

    public Response delegateRequest(IHTTPSession session){

        if(session.getMethod().equals(GET) && session.getUri().equals(GET_ORDER_URL)){
            return null;
        }else if(session.getMethod().equals(GET) && session.getUri().equals(GET_ALL_ORDER_URL)){
            return null;
        }else if (session.getMethod().equals(POST) && session.getUri().equals(ADD_ORDER_URL)){
            return null;
        }
        return NanoHTTPD.newFixedLengthResponse(NOT_FOUND, "text/plain", "Not Found");
    }
}
