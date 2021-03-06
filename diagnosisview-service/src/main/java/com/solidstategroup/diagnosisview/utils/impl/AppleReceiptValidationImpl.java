package com.solidstategroup.diagnosisview.utils.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.solidstategroup.diagnosisview.utils.AppleReceiptValidation;
import com.solidstategroup.diagnosisview.utils.HttpConnection;
import com.solidstategroup.diagnosisview.utils.MediaType;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * A utility class for validating Apple in app purchase reciepts
 *
 * @author Michael Da Silva (kronoshadow)
 */
/**
 * {@inheritDoc}.
 */
@Log
@Service
public class AppleReceiptValidationImpl implements AppleReceiptValidation {

    @Value("${IOS_APP_SECRET:NONE}")
    private String appSecret;

    //verify reciept URLs
    public static final String TEST_VALIDATE_RECEIPT_URL = "https://sandbox.itunes.apple.com/verifyReceipt";
    public static final String PRODUCTION_VALIDATE_RECIEPT_URL = "https://buy.itunes.apple.com/verifyReceipt";

    public AppleReceiptValidationImpl() {
    }

    /**
     * Validates an Apple app store receipt to verify a purchase was made.
     *
     * @param receipt a <code>String</code> containing the receipt data of the in app purchase
     * @param test    a <code>boolean</code> to indicate whether the the validation process should use Apple's sandbox
     *                for testing
     * @return a Google Gson <code>JsonObject</code> containing the JSON returned from the API
     * @throws AppleReceiptValidationFailedException gets thrown when the response from the Apple API server responded
     *                                               in an unexpected way or the receipt is invalid
     * @see <a href="https://developer.apple.com/library/ios/releasenotes/General/ValidateAppStoreReceipt/Chapters
     * /ValidateRemotely.html#//apple_ref/doc/uid/TP40010573-CH104-SW1">Apple Developer - Validating Receipts With The
     * App Store</a>
     */
    public JsonObject validateReciept(String receipt, boolean test) throws
            AppleReceiptValidationFailedException {
        //prepare a JSON with the receipt data for a request to Apple
        Map<String, String> receiptData = new HashMap<>();
        receiptData.put("receipt-data", receipt);
        receiptData.put("password", appSecret);
        Gson requestJson = new GsonBuilder().create();
        String json = requestJson.toJson(receiptData);

        //get the URL to use
        String validationUrl = test ? TEST_VALIDATE_RECEIPT_URL : PRODUCTION_VALIDATE_RECIEPT_URL;

        //attempt a connection
        String responseContent = "";
        try {
            responseContent = HttpConnection.sendSecureRequest(validationUrl, HttpConnection.RequestMethod.POST,
                    MediaType.Application.JSON, json);
        } catch (HttpConnection.ConnectionFailedException cfEx) {
            throw new AppleReceiptValidationFailedException("Couldn't validate the receipt because the connection " +
                    "with the Apple API server had failed", cfEx);
        }

        //check response from Apple API server is not empty
        if (responseContent.isEmpty()) {
            throw new AppleReceiptValidationFailedException("Couldn't validate the receipt because the response from " +
                    "the Apple API server was empty.");
        }

        //parse the content from the Apple API server as a JSON object
        JsonParser jsonParser = new JsonParser();
        JsonObject responseJson = null;
        try {
            responseJson = (JsonObject) jsonParser.parse(responseContent);
        } catch (JsonSyntaxException jsEx) {
            throw new AppleReceiptValidationFailedException("Couldn't validate the receipt because the response from " +
                    "the Apple API was a badly formed JSON", jsEx);
        } catch (JsonParseException jpEx) {
            throw new AppleReceiptValidationFailedException("Couldn't validate the receipt because the response from " +
                    "the Apple API was a badly formed JSON", jpEx);
        }

        //if the status returned is 0 then the validation was successful, all other statuses should throw an exception
        if (responseJson != null) {
            String status = responseJson.getAsJsonPrimitive("status").getAsString();
            if (status.equals("0")) {
                JsonObject recieptJson = responseJson.getAsJsonObject("receipt");
                return recieptJson;
            } else if (status.equals("21000")) {
                throw new AppleReceiptValidationFailedException("Couldn't validate the receipt because the Apple API " +
                        "server returned "
                        + "[Status 21000: The App Store could not read the JSON object you provided]");
            } else if (status.equals("21002")) {
                throw new AppleReceiptValidationFailedException("Couldn't validate the receipt because the Apple API " +
                        "server returned "
                        + "[Status 21002: The data in the receipt-data property was malformed or missing]");
            } else if (status.equals("21003")) {
                throw new AppleReceiptValidationFailedException("Couldn't validate the receipt because the Apple API " +
                        "server returned "
                        + "[Status 21003: The receipt could not be authenticated.]");
            } else if (status.equals("21004")) {
                throw new AppleReceiptValidationFailedException("Couldn't validate the receipt because the Apple API " +
                        "server returned "
                        + "[Status 21004: The shared secret you provided does not match the shared secret on file for" +
                        " your account.]");
            } else if (status.equals("21005")) {
                throw new AppleReceiptValidationFailedException("Couldn't validate the receipt because the Apple API " +
                        "server returned "
                        + "[Status 21005: The receipt server is not currently available]");
            } else if (status.equals("21006")) {
                throw new AppleReceiptValidationFailedException("Couldn't validate the receipt because the Apple API " +
                        "server returned "
                        + "[Status 21006: This receipt is valid but the subscription has expired. When this status " +
                        "code is returned to your server, the receipt data is also decoded and returned as part of " +
                        "the response.]");
            } else if (status.equals("21007")) {
                throw new AppleReceiptValidationFailedException("Couldn't validate the receipt because the Apple API " +
                        "server returned "
                        + "[Status 21007: This receipt is from the test environment, but it was sent to the " +
                        "production environment for verification. Send it to the test environment instead]");
            } else if (status.equals("21008")) {
                throw new AppleReceiptValidationFailedException("Couldn't validate the receipt because the Apple API " +
                        "server returned "
                        + "[Status 21008: This receipt is from the production environment, but it was sent to the " +
                        "test environment for verification. Send it to the production environment instead]");
            }
        }
        throw new AppleReceiptValidationFailedException("Couldn't validate the reciept becuase the response from the " +
                "Apple API server was empty.");
    }

    public class AppleReceiptValidationFailedException extends Exception {
        public AppleReceiptValidationFailedException() {
            super();
        }

        public AppleReceiptValidationFailedException(String message) {
            super(message);
        }

        public AppleReceiptValidationFailedException(String message, Throwable cause) {
            super(message, cause);
        }

        public AppleReceiptValidationFailedException(Throwable cause) {
            super(cause);
        }
    }

}