/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.wasp.ipc;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import org.apache.hadoop.util.StringUtils;

import java.io.IOException;

/**
 * Used for server-side protobuf RPC service invocations.  This handler allows
 * invocation exceptions to easily be passed through to the RPC server from coprocessor
 * {@link com.google.protobuf.Service} implementations.
 *
 * <p>
 * When implementing {@link com.google.protobuf.Service} defined methods, coprocessor endpoints can use the following
 * pattern to pass exceptions back to the RPC client:
 * <code>
 * public void myMethod(RpcController controller, MyRequest request, RpcCallback<MyResponse> done) {
 *   MyResponse response = null;
 *   try {
 *     // do processing
 *     response = MyResponse.getDefaultInstance();  // or use a new builder to populate the response
 *   } catch (IOException ioe) {
 *     // pass exception back up
 *     ResponseConverter.setControllerException(controller, ioe);
 *   }
 *   done.run(response);
 * }
 * </code>
 * </p>
 */
public class ServerRpcController implements RpcController {
  /**
   * The exception thrown within
   * {@link com.google.protobuf.Service#callMethod(com.google.protobuf.Descriptors.MethodDescriptor, com.google.protobuf.RpcController, com.google.protobuf.Message, com.google.protobuf.RpcCallback)},
   * if any.
   */
  // It would be good widen this to just Throwable, but IOException is what we allow now
  private IOException serviceException;
  private String errorMessage;

  @Override
  public void reset() {
    serviceException = null;
    errorMessage = null;
  }

  @Override
  public boolean failed() {
    return (failedOnException() || errorMessage != null);
  }

  @Override
  public String errorText() {
    return errorMessage;
  }

  @Override
  public void startCancel() {
    // not implemented
  }

  @Override
  public void setFailed(String message) {
    errorMessage = message;
  }

  @Override
  public boolean isCanceled() {
    return false;
  }

  @Override
  public void notifyOnCancel(RpcCallback<Object> objectRpcCallback) {
    // not implemented
  }

  /**
   * Sets an exception to be communicated back to the {@link com.google.protobuf.Service} client.
   * @param ioe the exception encountered during execution of the service method
   */
  public void setFailedOn(IOException ioe) {
    serviceException = ioe;
    setFailed(StringUtils.stringifyException(ioe));
  }

  /**
   * Returns any exception thrown during service method invocation, or {@code null} if no exception
   * was thrown.  This can be used by clients to receive exceptions generated by RPC calls, even
   * when {@link com.google.protobuf.RpcCallback}s are used and no {@link com.google.protobuf.ServiceException} is
   * declared.
   */
  public IOException getFailedOn() {
    return serviceException;
  }

  /**
   * Returns whether or not a server exception was generated in the prior RPC invocation.
   */
  public boolean failedOnException() {
    return serviceException != null;
  }
}
