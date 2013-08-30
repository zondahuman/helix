package org.apache.helix.messaging;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.helix.HelixException;
import org.apache.helix.HelixManager;
import org.apache.helix.Mocks;
import org.apache.helix.NotificationContext;
import org.apache.helix.api.Id;
import org.apache.helix.api.MessageId;
import org.apache.helix.messaging.handling.AsyncCallbackService;
import org.apache.helix.messaging.handling.MessageHandler;
import org.apache.helix.model.Message;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class TestAsyncCallbackSvc {
  class MockHelixManager extends Mocks.MockManager {
    public String getSessionId() {
      return "123";
    }
  }

  class TestAsyncCallback extends AsyncCallback {
    HashSet<MessageId> _repliedMessageId = new HashSet<MessageId>();

    @Override
    public void onTimeOut() {
      // TODO Auto-generated method stub

    }

    @Override
    public void onReplyMessage(Message message) {
      // TODO Auto-generated method stub
      _repliedMessageId.add(message.getMsgId());
    }

  }

  @Test(groups = {
    "unitTest"
  })
  public void testAsyncCallbackSvc() throws Exception {
    AsyncCallbackService svc = new AsyncCallbackService();
    HelixManager manager = new MockHelixManager();
    NotificationContext changeContext = new NotificationContext(manager);

    Message msg = new Message(svc.getMessageType(), Id.message(UUID.randomUUID().toString()));
    msg.setTgtSessionId(Id.session(manager.getSessionId()));
    try {
      MessageHandler aHandler = svc.createHandler(msg, changeContext);
    } catch (HelixException e) {
      AssertJUnit.assertTrue(e.getMessage().indexOf(msg.getMsgId().stringify()) != -1);
    }
    Message msg2 = new Message("RandomType", Id.message(UUID.randomUUID().toString()));
    msg2.setTgtSessionId(Id.session(manager.getSessionId()));
    try {
      MessageHandler aHandler = svc.createHandler(msg2, changeContext);
    } catch (HelixException e) {
      AssertJUnit.assertTrue(e.getMessage().indexOf(msg2.getMsgId().stringify()) != -1);
    }
    Message msg3 = new Message(svc.getMessageType(), Id.message(UUID.randomUUID().toString()));
    msg3.setTgtSessionId(Id.session(manager.getSessionId()));
    msg3.setCorrelationId("wfwegw");
    try {
      MessageHandler aHandler = svc.createHandler(msg3, changeContext);
    } catch (HelixException e) {
      AssertJUnit.assertTrue(e.getMessage().indexOf(msg3.getMsgId().stringify()) != -1);
    }

    TestAsyncCallback callback = new TestAsyncCallback();
    String corrId = UUID.randomUUID().toString();
    svc.registerAsyncCallback(corrId, new TestAsyncCallback());
    svc.registerAsyncCallback(corrId, callback);

    List<Message> msgSent = new ArrayList<Message>();
    msgSent.add(new Message("Test", Id.message(UUID.randomUUID().toString())));
    callback.setMessagesSent(msgSent);

    msg = new Message(svc.getMessageType(), Id.message(UUID.randomUUID().toString()));
    msg.setTgtSessionId(Id.session("*"));
    msg.setCorrelationId(corrId);

    MessageHandler aHandler = svc.createHandler(msg, changeContext);
    Map<String, String> resultMap = new HashMap<String, String>();
    aHandler.handleMessage();

    AssertJUnit.assertTrue(callback.isDone());
    AssertJUnit.assertTrue(callback._repliedMessageId.contains(msg.getMsgId()));
  }
}
