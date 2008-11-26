/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.mpt;

/**
 * Runs protocol scripts.
 */
public class Runner {
    
    /** The Protocol session which is run before the testElements */
    private final ProtocolSession preElements = new ProtocolSession();

    /** The Protocol session which contains the tests elements */
    private final ProtocolSession testElements = new ProtocolSession();

    /** The Protocol session which is run after the testElements. */
    private final ProtocolSession postElements = new ProtocolSession();
    
    public void continueAfterFailure() {
        preElements.setContinueAfterFailure(true);
        testElements.setContinueAfterFailure(true);
        postElements.setContinueAfterFailure(true);
    }
    
    /**
     * Gets protocol session run after test.
     * @return not null
     */
    public ProtocolSession getPostElements() {
        return postElements;
    }

    /**
     * Gets protocol session run before test.
     * @return not null
     */
    public ProtocolSession getPreElements() {
        return preElements;
    }
    /**
     * Gets protocol session run on test.
     * @return not null
     */
    public ProtocolSession getTestElements() {
        return testElements;
    }



    /**
     * Runs the pre,test and post protocol sessions against a local copy of the
     * ImapServer. This does not require that James be running, and is useful
     * for rapid development and debugging.
     * 
     * Instead of sending requests to a socket connected to a running instance
     * of James, this method uses the {@link MockImapServer} to simplify
     * testing. One mock instance is required per protocol session/connection.
     * These share the same underlying Mailboxes, because of the way
     * {@link MockImapServer#getImapSession()} works.
     */
    public void runSessions(final SessionFactory factory) throws Exception {
        class SessionContinuation implements HostSystem.Continuation {

            public ProtocolSession session;

            public void doContinue() {
                if (session != null) {
                    session.doContinue();
                }
            }

        }
        SessionContinuation continuation = new SessionContinuation();

        Session[] sessions = new Session[testElements
                .getSessionCount()];

        for (int i = 0; i < sessions.length; i++) {
            sessions[i] = factory.newSession(continuation);
            sessions[i].start();
        }
        try {
            continuation.session = preElements;
            preElements.runSessions(sessions);
            continuation.session = testElements;
            testElements.runSessions(sessions);
            continuation.session = postElements;
            postElements.runSessions(sessions);
        } finally {
            for (int i = 0; i < sessions.length; i++) {
                sessions[i].stop();
            }
        }
    }

}
