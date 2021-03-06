package com.solidstategroup.diagnosisview.api.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;


/**
 * Generic exception handler, returns JSON on exception.
 */
@RestController
public class ExceptionController implements ErrorController {

    private final ErrorAttributes errorAttributes;

    public ExceptionController(ErrorAttributes errorAttributes) {

        this.errorAttributes = errorAttributes;
    }

    /**
     * Generic exception handler to return errors in standard ErrorJson format.
     *
     * @param webRequest HTTP servlet request
     * @param response   HTTP servlet response
     * @return ErrorJson containing error details
     * @throws Exception thrown handling exception
     */
    @RequestMapping("/error")
    @ResponseBody
    public ErrorJson error(WebRequest webRequest, final HttpServletResponse response) {

        Map<String, Object> err = errorAttributes.getErrorAttributes(webRequest, false);

        return new ErrorJson(
                response.getStatus(),
                (String) err.get("error"),
                (String) err.get("message"),
                err.get("timestamp").toString());
    }

    /**
     * Required override for error path.
     *
     * @return String error path
     */
    @Override
    public String getErrorPath() {
        return "/error";
    }

    /**
     * Helper class for error messages.
     */
    @Data
    @AllArgsConstructor
    private class ErrorJson {
        private Integer status;
        private String error;
        private String message;
        private String timestamp;
    }
}
