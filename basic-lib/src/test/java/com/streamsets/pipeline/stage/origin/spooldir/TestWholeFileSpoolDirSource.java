/**
 * Copyright 2016 StreamSets Inc.
 * <p>
 * Licensed under the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.streamsets.pipeline.stage.origin.spooldir;

import com.codahale.metrics.Gauge;
import com.streamsets.pipeline.api.Field;
import com.streamsets.pipeline.api.FileRef;
import com.streamsets.pipeline.api.OnRecordError;
import com.streamsets.pipeline.api.Record;
import com.streamsets.pipeline.api.Stage;
import com.streamsets.pipeline.config.Compression;
import com.streamsets.pipeline.config.DataFormat;
import com.streamsets.pipeline.config.PostProcessingOptions;
import com.streamsets.pipeline.lib.io.fileref.FileRefUtil;
import com.streamsets.pipeline.sdk.SourceRunner;
import com.streamsets.pipeline.sdk.StageRunner;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TestWholeFileSpoolDirSource {
  private String testDir;

  @Before
  public void createTestDir() throws Exception {
    File f = new File("target", UUID.randomUUID().toString());
    Assert.assertTrue(f.mkdirs());
    testDir = f.getAbsolutePath();

  }

  private SpoolDirSource createSource() {
    SpoolDirConfigBean conf = new SpoolDirConfigBean();
    conf.dataFormat = DataFormat.WHOLE_FILE;
    conf.spoolDir = testDir;
    conf.batchSize = 10;
    conf.overrunLimit = 100;
    conf.poolingTimeoutSecs = 1;
    conf.filePattern = "*";
    conf.maxSpoolFiles = 10;
    conf.initialFileToProcess = null;
    conf.dataFormatConfig.compression = Compression.NONE;
    conf.dataFormatConfig.filePatternInArchive = "*";
    conf.errorArchiveDir = null;
    conf.postProcessing = PostProcessingOptions.NONE;
    conf.retentionTimeMins = 10;
    conf.dataFormatConfig.wholeFileMaxObjectLen = 1024;
    return new SpoolDirSource(conf);
  }

  @Test
  public void testWholeFileRecordsForFile() throws Exception {
    Files.write(Paths.get(testDir + "/source.txt"), "Sample Text 1".getBytes());

    SpoolDirSource source = createSource();
    SourceRunner runner =
        new SourceRunner.Builder(SpoolDirDSource.class, source)
            .addOutputLane("lane")
            .setOnRecordError(OnRecordError.TO_ERROR)
            .build();

    runner.runInit();
    try {
      StageRunner.Output output = runner.runProduce("", 10);
      List<Record> records = output.getRecords().get("lane");
      Assert.assertNotNull(records);
      Assert.assertEquals(1, records.size());
    } finally {
      runner.runDestroy();
    }
  }


  private void initMetrics(Stage.Context context) {
    context.createMeter(FileRefUtil.TRANSFER_THROUGHPUT_METER);
    final Map<String, Object> gaugeStatistics = new LinkedHashMap<>();
    gaugeStatistics.put(FileRefUtil.TRANSFER_THROUGHPUT, 0L);
    gaugeStatistics.put(FileRefUtil.SENT_BYTES, 0L);
    gaugeStatistics.put(FileRefUtil.REMAINING_BYTES, 0L);
    gaugeStatistics.put(FileRefUtil.COMPLETED_FILE_COUNT, 0L);
    context.createGauge(FileRefUtil.GAUGE_NAME, new Gauge<Map<String, Object>>() {
      @Override
      public Map<String, Object> getValue() {
        return gaugeStatistics;
      }
    });
  }


  @Test
  public void testWholeFileRecords() throws Exception {
    Path sourcePath = Paths.get(testDir + "/source.txt");
    Files.write(sourcePath, "Sample Text 1".getBytes());

    SpoolDirSource source = createSource();
    SourceRunner runner =
        new SourceRunner.Builder(SpoolDirDSource.class, source)
            .addOutputLane("lane")
            .setOnRecordError(OnRecordError.TO_ERROR)
            .build();

    runner.runInit();
    try {
      StageRunner.Output output = runner.runProduce("", 10);
      List<Record> records = output.getRecords().get("lane");
      Assert.assertNotNull(records);
      Assert.assertEquals(1, records.size());
      Record record = records.get(0);
      Map<String, Object> metadata = Files.readAttributes(Paths.get(testDir+"/source.txt"), "posix:*");
      Assert.assertTrue(record.has(FileRefUtil.FILE_INFO_FIELD_PATH));
      Assert.assertTrue(record.has(FileRefUtil.FILE_REF_FIELD_PATH));
      Assert.assertEquals(Field.Type.FILE_REF, record.get(FileRefUtil.FILE_REF_FIELD_PATH).getType());
      Assert.assertEquals(Field.Type.MAP, record.get(FileRefUtil.FILE_INFO_FIELD_PATH).getType());
      Assert.assertTrue(record.get(FileRefUtil.FILE_INFO_FIELD_PATH).getValueAsMap().keySet().containsAll(metadata.keySet()));

      FileRef fileRef = record.get(FileRefUtil.FILE_REF_FIELD_PATH).getValueAsFileRef();
      String targetFile = testDir + "/target.txt";
      Stage.Context context = (Stage.Context) Whitebox.getInternalState(source, "context");
      initMetrics(context);

      IOUtils.copy(
          fileRef.createInputStream(context, InputStream.class),
          new FileOutputStream(targetFile)
      );
      //Now make sure the file is copied properly,
      checkFileContent(new FileInputStream(sourcePath.toString()), new FileInputStream(targetFile));
    } finally {
      runner.runDestroy();
    }
  }


  private void checkFileContent(InputStream is1, InputStream is2) throws Exception {
    int totalBytesRead1 = 0, totalBytesRead2 = 0;
    int a = 0, b = 0;
    while (a != -1 || b != -1) {
      totalBytesRead1 = ((a = is1.read()) != -1)? totalBytesRead1 + 1 : totalBytesRead1;
      totalBytesRead2 = ((b = is2.read()) != -1)? totalBytesRead2 + 1 : totalBytesRead2;
      Assert.assertEquals(a, b);
    }
    Assert.assertEquals(totalBytesRead1, totalBytesRead2);
  }

}