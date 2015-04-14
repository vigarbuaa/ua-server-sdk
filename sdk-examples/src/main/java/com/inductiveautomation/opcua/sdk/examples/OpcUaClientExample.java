/*
 * Copyright 2015
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.inductiveautomation.opcua.sdk.examples;

import java.util.Arrays;
import java.util.UUID;

import com.inductiveautomation.opcua.sdk.client.OpcUaClient;
import com.inductiveautomation.opcua.sdk.client.OpcUaClientConfig;
import com.inductiveautomation.opcua.sdk.client.subscriptions.OpcUaMonitoredItem;
import com.inductiveautomation.opcua.sdk.client.subscriptions.OpcUaSubscription;
import com.inductiveautomation.opcua.sdk.client.subscriptions.SubscriptionManager;
import com.inductiveautomation.opcua.stack.client.UaTcpClient;
import com.inductiveautomation.opcua.stack.client.UaTcpClientBuilder;
import com.inductiveautomation.opcua.stack.core.Identifiers;
import com.inductiveautomation.opcua.stack.core.types.builtin.LocalizedText;
import com.inductiveautomation.opcua.stack.core.types.builtin.QualifiedName;
import com.inductiveautomation.opcua.stack.core.types.enumerated.MessageSecurityMode;
import com.inductiveautomation.opcua.stack.core.types.enumerated.MonitoringMode;
import com.inductiveautomation.opcua.stack.core.types.enumerated.TimestampsToReturn;
import com.inductiveautomation.opcua.stack.core.types.structured.EndpointDescription;
import com.inductiveautomation.opcua.stack.core.types.structured.MonitoringParameters;
import com.inductiveautomation.opcua.stack.core.types.structured.ReadValueId;

import static com.inductiveautomation.opcua.stack.core.types.builtin.unsigned.Unsigned.ubyte;
import static com.inductiveautomation.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

public class OpcUaClientExample {

    public static void main(String[] args) throws Exception {
        EndpointDescription[] endpoints = UaTcpClient.getEndpoints("opc.tcp://localhost:4096").get();

        EndpointDescription endpoint = Arrays.stream(endpoints)
                .filter(e -> e.getSecurityMode() == MessageSecurityMode.None)
                .findFirst()
                .orElseThrow(() -> new Exception("no desired endpoints returned"));

        System.out.println("Connecting to endpoint: " + endpoint.getEndpointUrl() + " [" + endpoint.getSecurityPolicyUri() + "]");

        UaTcpClient stackClient = new UaTcpClientBuilder()
                .setApplicationName(LocalizedText.english("Stack Example Client"))
                .setApplicationUri(String.format("urn:example-client:%s", UUID.randomUUID()))
//                .setCertificate(certificate)
//                .setKeyPair(keyPair)
                .build(endpoint);

        OpcUaClientConfig config = OpcUaClientConfig.builder()
                .setStackClient(stackClient)
                .build();

        OpcUaClient client = new OpcUaClient(config);

        client.connect().get();

        SubscriptionManager subscriptionManager = new SubscriptionManager(client);

        OpcUaSubscription subscription = new OpcUaSubscription(
                client,
                1000.0,
                uint(15),
                uint(5),
                uint(0),
                true,
                ubyte(0));

        subscriptionManager.addSubscription(subscription).thenCompose(s -> {
            OpcUaMonitoredItem item = new OpcUaMonitoredItem(new ReadValueId(
                    Identifiers.Server_ServerStatus_CurrentTime,
                    uint(13), null, QualifiedName.NULL_VALUE));

            return subscription.createItems(TimestampsToReturn.Both, createItemsContext -> {
                MonitoringParameters parameters = new MonitoringParameters(
                        uint(1),
                        500.0,
                        null,
                        uint(1),
                        true);

                createItemsContext.addItem(item, parameters, MonitoringMode.Reporting);
            });
        });

        Thread.sleep(999999999);
    }

}
