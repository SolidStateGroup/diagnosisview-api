package com.solidstategroup.diagnosisview.utils;

import com.google.gson.JsonObject;
import com.solidstategroup.diagnosisview.utils.impl.AppleReceiptValidationImpl;

/**
 * A utility class for validating Apple in app purchase reciepts
 *
 * @author Michael Da Silva (kronoshadow)
 */
public interface AppleReceiptValidation {

    /**
     * Validates an Apple app store receipt to verify a purchase was made.
     *
     * @param receipt a <code>String</code> containing the receipt data of the in app purchase
     * @param test    a <code>boolean</code> to indicate whether the the validation process should use Apple's sandbox
     *                for testing
     * @return a Google Gson <code>JsonObject</code> containing the JSON returned from the API
     * @throws AppleReceiptValidationImpl.AppleReceiptValidationFailedException gets thrown when the response from the Apple API server responded
     *                                               in an unexpected way or the receipt is invalid
     * @see <a href="https://developer.apple.com/library/ios/releasenotes/General/ValidateAppStoreReceipt/Chapters
     * /ValidateRemotely.html#//apple_ref/doc/uid/TP40010573-CH104-SW1">Apple Developer - Validating Receipts With The
     * App Store</a>
     */
    JsonObject validateReciept(String receipt, boolean test) throws
            AppleReceiptValidationImpl.AppleReceiptValidationFailedException;
}