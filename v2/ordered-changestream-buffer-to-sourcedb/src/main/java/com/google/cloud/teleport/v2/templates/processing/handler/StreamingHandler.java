/*
 * Copyright (C) 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.cloud.teleport.v2.templates.processing.handler;

import com.google.cloud.teleport.v2.templates.common.InputBufferReader;
import com.google.cloud.teleport.v2.templates.common.ProcessingContext;
import com.google.cloud.teleport.v2.templates.dao.BaseDao;
import com.google.cloud.teleport.v2.templates.dao.DaoFactory;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Abstract class for the streaming handlers. */
public abstract class StreamingHandler {
  private static final Logger LOG = LoggerFactory.getLogger(StreamingHandler.class);
  protected ProcessingContext taskContext;

  public StreamingHandler(ProcessingContext taskContext) {
    this.taskContext = taskContext;
  }

  public void process() {
    String shardId = taskContext.getShard().getLogicalShardId();
    InputBufferReader inputBufferReader = this.getBufferReader();

    String sourceType = taskContext.getSourceType();

    try {
      Instant readStartTime = Instant.now();
      List<String> records = inputBufferReader.getRecords();
      Instant readEndTime = Instant.now();
      LOG.info(
          "Shard "
              + shardId
              + ": read "
              + records.size()
              + " records from the buffer in "
              + ChronoUnit.MILLIS.between(readStartTime, readEndTime)
              + " milliseconds");
      if (records.isEmpty()) {
        inputBufferReader.acknowledge();
        return;
      }

      String connectString = getConnectionString(taskContext, sourceType);

      BaseDao dao = null;

      if (sourceType.equals("mysql")) {
        dao =
            new DaoFactory(
                    connectString,
                    taskContext.getShard().getUserName(),
                    taskContext.getShard().getPassword())
                .getMySqlDao(shardId);
      } else if (sourceType.equals("postgres")) {
        dao =
            new DaoFactory(
                    connectString,
                    taskContext.getShard().getUserName(),
                    taskContext.getShard().getPassword())
                .getPostgreSQLDao(shardId);
      } else {
        LOG.error("Only mysql and postgresql source types are supported.");
        throw new RuntimeException(
            "Input sourceType value : "
                + sourceType
                + " is unsupported. Supported values are : 'mysql' and 'postgres'");
      }

      InputRecordProcessor.processRecords(
          records,
          taskContext.getSchema(),
          dao,
          shardId,
          taskContext.getSourceDbTimezoneOffset(),
          sourceType);
      inputBufferReader.acknowledge();
      dao.cleanup();
      LOG.info(
          "Shard " + shardId + ": Successfully processed batch of " + records.size() + " records.");
    } catch (Exception e) {
      // TODO: Error handling and retry
      /*
      If we are here, it means we have exhausted all the retries
      At this stage we dump the error records to error topic
      and either halt the pipeline  or continue
      as per the configuration
      If writing to DLQ topic also fails - write to logs
      */

      throw new RuntimeException("Failure when processing records: " + e.getMessage());
    }
  }

  public abstract InputBufferReader getBufferReader();

  private String getConnectionString(ProcessingContext taskContext, String sourceType) {
    String jdbcURLPrefix = "";
    if (sourceType.equals("mysql")) {
      jdbcURLPrefix = "jdbc:mysql://";
    } else if (sourceType.equals("postgres")) {
      jdbcURLPrefix = "jdbc:postgresql://";
    } else {
      LOG.error("Only mysql and postgresql source types are supported.");
      throw new RuntimeException(
          "Input sourceType value : "
              + sourceType
              + " is unsupported. Supported values are : 'mysql' and 'postgres'");
    }

    return jdbcURLPrefix
        + taskContext.getShard().getHost()
        + ":"
        + taskContext.getShard().getPort()
        + "/"
        + taskContext.getShard().getDbName();
  }
}
