package org.apache.helix.monitoring;

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

import org.apache.helix.NotificationContext;
import org.apache.helix.model.Message;
import org.apache.helix.participant.statemachine.StateModel;
import org.apache.helix.participant.statemachine.StateModelInfo;
import org.apache.helix.participant.statemachine.Transition;
import org.apache.log4j.Logger;

@StateModelInfo(initialState = "OFFLINE", states = {
    "DROPPED", "OFFLINE", "ONLINE"
})
public class RiemannAgentStateModel extends StateModel {
  private static final Logger LOG = Logger.getLogger(RiemannAgentStateModel.class);

  void logTransition(Message message) {
    String toState = message.getToState();
    String fromState = message.getFromState();
    String resourceName = message.getResourceName();
    String partittionName = message.getPartitionName();

    LOG.info("Become " + toState + " from " + fromState + " for resource: " + resourceName
        + ", partition: " + partittionName);
  }

  @Transition(to = "ONLINE", from = "OFFLINE")
  public void onBecomeOnlineFromOffline(Message message, NotificationContext context) {
    logTransition(message);
  }

  @Transition(to = "OFFLINE", from = "ONLINE")
  public void onBecomeOfflineFromOnline(Message message, NotificationContext context) {
    logTransition(message);
  }
}
