/*
 * digitalpetri OPC-UA SDK
 *
 * Copyright (C) 2015 Kevin Herron
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.digitalpetri.opcua.sdk.server.model.variables;

import java.util.Optional;

import com.digitalpetri.opcua.sdk.core.AttributeIds;
import com.digitalpetri.opcua.sdk.core.model.UaMandatory;
import com.digitalpetri.opcua.sdk.core.model.variables.ServerStatusType;
import com.digitalpetri.opcua.sdk.core.nodes.VariableNode;
import com.digitalpetri.opcua.sdk.server.api.UaNamespace;
import com.digitalpetri.opcua.sdk.server.util.UaVariableType;
import com.digitalpetri.opcua.stack.core.types.builtin.DataValue;
import com.digitalpetri.opcua.stack.core.types.builtin.DateTime;
import com.digitalpetri.opcua.stack.core.types.builtin.LocalizedText;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.QualifiedName;
import com.digitalpetri.opcua.stack.core.types.builtin.Variant;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UByte;
import com.digitalpetri.opcua.stack.core.types.builtin.unsigned.UInteger;
import com.digitalpetri.opcua.stack.core.types.enumerated.ServerState;
import com.digitalpetri.opcua.stack.core.types.structured.BuildInfo;
import com.digitalpetri.opcua.stack.core.types.structured.ServerStatusDataType;

@UaVariableType(name = "ServerStatusType")
public class ServerStatusNode extends BaseDataVariableNode implements ServerStatusType {

    public ServerStatusNode(UaNamespace namespace,
                            NodeId nodeId,
                            QualifiedName browseName,
                            LocalizedText displayName,
                            Optional<LocalizedText> description,
                            Optional<UInteger> writeMask,
                            Optional<UInteger> userWriteMask,
                            DataValue value,
                            NodeId dataType,
                            Integer valueRank,
                            Optional<UInteger[]> arrayDimensions,
                            UByte accessLevel,
                            UByte userAccessLevel,
                            Optional<Double> minimumSamplingInterval,
                            boolean historizing) {

        super(namespace, nodeId, browseName, displayName, description, writeMask, userWriteMask,
                value, dataType, valueRank, arrayDimensions, accessLevel, userAccessLevel, minimumSamplingInterval, historizing);

    }

    @Override
    public DataValue getValue() {
        ServerStatusDataType value = new ServerStatusDataType(
                getStartTime(),
                getCurrentTime(),
                getState(),
                getBuildInfo(),
                getSecondsTillShutdown(),
                getShutdownReason()
        );

        return new DataValue(new Variant(value));
    }

    @Override
    public synchronized void setValue(DataValue value) {
        ServerStatusDataType v = (ServerStatusDataType) value.getValue().getValue();

        setStartTime(v.getStartTime());
        setCurrentTime(v.getCurrentTime());
        setState(v.getState());
        setBuildInfo(v.getBuildInfo());
        setSecondsTillShutdown(v.getSecondsTillShutdown());
        setShutdownReason(v.getShutdownReason());

        fireAttributeChanged(AttributeIds.Value, value);
    }

    @Override
    @UaMandatory("StartTime")
    public DateTime getStartTime() {
        Optional<VariableNode> node = getVariableComponent("StartTime");

        return node.map(n -> (DateTime) n.getValue().getValue().getValue()).orElse(null);
    }

    @Override
    @UaMandatory("CurrentTime")
    public DateTime getCurrentTime() {
        Optional<VariableNode> node = getVariableComponent("CurrentTime");

        return node.map(n -> (DateTime) n.getValue().getValue().getValue()).orElse(null);
    }

    @Override
    @UaMandatory("State")
    public ServerState getState() {
        Optional<VariableNode> node = getVariableComponent("State");
        return node.map(n -> {
            Integer value = (Integer) n.getValue().getValue().getValue();

            return ServerState.from(value);
        }).orElse(null);
    }

    @Override
    @UaMandatory("BuildInfo")
    public BuildInfo getBuildInfo() {
        Optional<VariableNode> node = getVariableComponent("BuildInfo");

        return node.map(n -> (BuildInfo) n.getValue().getValue().getValue()).orElse(null);
    }

    @Override
    @UaMandatory("SecondsTillShutdown")
    public UInteger getSecondsTillShutdown() {
        Optional<VariableNode> node = getVariableComponent("SecondsTillShutdown");

        return node.map(n -> (UInteger) n.getValue().getValue().getValue()).orElse(null);
    }

    @Override
    @UaMandatory("ShutdownReason")
    public LocalizedText getShutdownReason() {
        Optional<VariableNode> node = getVariableComponent("ShutdownReason");

        return node.map(n -> (LocalizedText) n.getValue().getValue().getValue()).orElse(null);
    }

    @Override
    public synchronized void setStartTime(DateTime startTime) {
        getVariableComponent("StartTime").ifPresent(n -> {
            n.setValue(new DataValue(new Variant(startTime)));

            fireAttributeChanged(AttributeIds.Value, getValue());
        });
    }

    @Override
    public synchronized void setCurrentTime(DateTime currentTime) {
        getVariableComponent("CurrentTime").ifPresent(n -> {
            n.setValue(new DataValue(new Variant(currentTime)));

            fireAttributeChanged(AttributeIds.Value, getValue());
        });
    }

    @Override
    public synchronized void setState(ServerState state) {
        getVariableComponent("State").ifPresent(n -> {
            Integer value = state.getValue();

            n.setValue(new DataValue(new Variant(value)));

            fireAttributeChanged(AttributeIds.Value, getValue());
        });
    }

    @Override
    public synchronized void setBuildInfo(BuildInfo buildInfo) {
        getVariableComponent("BuildInfo").ifPresent(n -> {
            n.setValue(new DataValue(new Variant(buildInfo)));

            fireAttributeChanged(AttributeIds.Value, getValue());
        });
    }

    @Override
    public synchronized void setSecondsTillShutdown(UInteger secondsTillShutdown) {
        getVariableComponent("SecondsTillShutdown").ifPresent(n -> {
            n.setValue(new DataValue(new Variant(secondsTillShutdown)));

            fireAttributeChanged(AttributeIds.Value, getValue());
        });
    }

    @Override
    public synchronized void setShutdownReason(LocalizedText shutdownReason) {
        getVariableComponent("ShutdownReason").ifPresent(n -> {
            n.setValue(new DataValue(new Variant(shutdownReason)));

            fireAttributeChanged(AttributeIds.Value, getValue());
        });
    }

}
