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

package com.digitalpetri.opcua.sdk.server.api;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.digitalpetri.opcua.sdk.server.DiagnosticsContext;
import com.digitalpetri.opcua.sdk.server.OpcUaServer;
import com.digitalpetri.opcua.sdk.server.Session;
import com.digitalpetri.opcua.sdk.server.api.MethodInvocationHandler.NodeIdUnknownHandler;
import com.digitalpetri.opcua.stack.core.StatusCodes;
import com.digitalpetri.opcua.stack.core.types.builtin.DiagnosticInfo;
import com.digitalpetri.opcua.stack.core.types.builtin.NodeId;
import com.digitalpetri.opcua.stack.core.types.builtin.StatusCode;
import com.digitalpetri.opcua.stack.core.types.builtin.Variant;
import com.digitalpetri.opcua.stack.core.types.structured.CallMethodRequest;
import com.digitalpetri.opcua.stack.core.types.structured.CallMethodResult;
import com.google.common.collect.Lists;
import org.slf4j.LoggerFactory;

import static com.digitalpetri.opcua.sdk.server.util.FutureUtils.sequence;

public interface MethodServices {

    /**
     * Invoke one or more methods belonging to this {@link MethodServices}.
     *
     * @param context  the {@link CallContext}.
     * @param requests The {@link CallMethodRequest}s for the methods to invoke.
     */
    default void call(CallContext context, List<CallMethodRequest> requests) {
        List<CompletableFuture<CallMethodResult>> results = Lists.newArrayListWithCapacity(requests.size());

        for (CallMethodRequest request : requests) {
            MethodInvocationHandler handler = getInvocationHandler(request.getMethodId())
                    .orElse(new NodeIdUnknownHandler());

            CompletableFuture<CallMethodResult> resultFuture = new CompletableFuture<>();

            try {
                handler.invoke(request, resultFuture);
            } catch (Throwable t) {
                LoggerFactory.getLogger(getClass())
                        .error("Uncaught Throwable invoking method handler for methodId={}.", request.getMethodId(), t);

                resultFuture.complete(new CallMethodResult(
                        new StatusCode(StatusCodes.Bad_InternalError),
                        new StatusCode[0], new DiagnosticInfo[0], new Variant[0]
                ));
            }

            results.add(resultFuture);
        }

        sequence(results).thenAccept(rs -> context.getFuture().complete(rs));
    }

    /**
     * Get the {@link MethodInvocationHandler} for the method identified by {@code methodId}, if it exists.
     *
     * @param methodId the {@link NodeId} identifying the method.
     * @return the {@link MethodInvocationHandler} for {@code methodId}, if it exists.
     */
    default Optional<MethodInvocationHandler> getInvocationHandler(NodeId methodId) {
        return Optional.empty();
    }

    final class CallContext extends OperationContext<CallMethodRequest, CallMethodResult> {
        public CallContext(OpcUaServer server, Session session,
                           DiagnosticsContext<CallMethodRequest> diagnosticsContext) {

            super(server, session, diagnosticsContext);
        }
    }

}
