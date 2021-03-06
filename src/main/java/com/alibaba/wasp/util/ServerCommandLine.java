/**
 * Copyright The Apache Software Foundation
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.alibaba.wasp.util;

import com.alibaba.wasp.conf.WaspConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

public abstract class ServerCommandLine extends Configured implements Tool {
  private static final Log LOG = LogFactory.getLog(ServerCommandLine.class);

  /**
   * Implementing subclasses should return a usage string to print out.
   */
  protected abstract String getUsage();

  /**
   * Print usage information for this command line.
   *
   * @param message if not null, print this message before the usage info.
   */
  protected void usage(String message) {
    if (message != null) {
      System.err.println(message);
      System.err.println("");
    }

    System.err.println(getUsage());
  }

  /**
   * Log information about the currently running JVM.
   */
  public static void logJVMInfo() {
    // Print out vm stats before starting up.
    RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
    if (runtime != null) {
      LOG.info("vmName=" + runtime.getVmName() + ", vmVendor=" +
               runtime.getVmVendor() + ", vmVersion=" + runtime.getVmVersion());
      LOG.info("vmInputArguments=" + runtime.getInputArguments());
    }
  }

  /**
   * Parse and run the given command line. This may exit the JVM if
   * a nonzero exit code is returned from <code>run()</code>.
   */
  public void doMain(String args[]) {
    try {
      int ret = ToolRunner.run(WaspConfiguration.create(), this, args);
      if (ret != 0) {
        System.exit(ret);
      }
    } catch (Exception e) {
      LOG.error("Failed to run", e);
      System.exit(-1);
    }
  }
}
