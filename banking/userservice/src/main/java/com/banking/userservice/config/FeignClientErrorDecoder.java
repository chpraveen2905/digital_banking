package com.banking.userservice.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FeignClientErrorDecoder implements ErrorDecoder {
    /**
     * Overrides the decode method to handle exception decoding.
     *
     * @param s        The string being decoded.
     * @param response The response from the server.
     * @return The decoded exception.
     */
    @Override
    public Exception decode(String s, Response response) {

        return null;
    }
}
