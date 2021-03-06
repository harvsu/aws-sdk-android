/*
 *  Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *  Licensed under the Amazon Software License (the "License").
 *  You may not use this file except in compliance with the
 *  License. A copy of the License is located at
 *
 *      http://aws.amazon.com/asl/
 *
 *  or in the "license" file accompanying this file. This file is
 *  distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *  CONDITIONS OF ANY KIND, express or implied. See the License
 *  for the specific language governing permissions and
 *  limitations under the License.
 */

package com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.services.cognitoidentityprovider.model.RespondToAuthChallengeResult;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Tests the {@link MultiFactorAuthenticationContinuation}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest= Config.NONE, sdk = 27)
public final class MultiFactorAuthenticationContinuationTest {
    /**
     * What happens when we try to pass client metadata into the handler?
     * @throws InterruptedException If {@link CountDownRunnable} is interrupted while pending result
     */
    @Test
    public void setMetadataAndContinueTask() throws InterruptedException {
        AuthenticationHandler handler = mock(AuthenticationHandler.class);
        CognitoUser user = mock(CognitoUser.class);

        String mfaCode = "mfa";
        Map<String, String> metadata = Collections.singletonMap("blacklives", "matter");
        RespondToAuthChallengeResult result = new RespondToAuthChallengeResult();
        MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation =
            new MultiFactorAuthenticationContinuation(user, getApplicationContext(), result, false, handler);
        multiFactorAuthenticationContinuation.setClientMetaData(metadata);
        multiFactorAuthenticationContinuation.setMfaCode(mfaCode);

        CountDownRunnable countDownRunnable = CountDownRunnable.create();
        doReturn(countDownRunnable)
            .when(user)
            .respondToMfaChallenge(eq(metadata), eq(mfaCode), eq(result), eq(handler), eq(false));

        // Act: continue task
        multiFactorAuthenticationContinuation.continueTask();

        // Assert: action was invoked
        assertTrue(countDownRunnable.await(5, TimeUnit.SECONDS));
        assertEquals(metadata, multiFactorAuthenticationContinuation.getClientMetaData());
    }
}