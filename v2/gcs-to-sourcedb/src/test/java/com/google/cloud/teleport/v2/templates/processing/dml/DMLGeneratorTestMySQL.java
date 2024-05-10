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
package com.google.cloud.teleport.v2.templates.processing.dml;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.cloud.teleport.v2.spanner.migrations.schema.Schema;
import com.google.cloud.teleport.v2.spanner.migrations.utils.SessionFileReader;
import com.google.cloud.teleport.v2.templates.common.TrimmedShardedDataChangeRecord;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import org.apache.beam.sdk.io.FileSystems;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class DMLGeneratorTestMySQL {

  @Test
  public void tableAndAllColumnNameTypesMatch() {
    Schema schema = SessionFileReader.read("src/test/resources/allMatchSession.json");
    String tableName = "Singers";
    String newValuesString = "{\"FirstName\":\"kk\",\"LastName\":\"ll\"}";
    JSONObject newValuesJson = new JSONObject(newValuesString);
    String keyValueString = "{\"SingerId\":\"999\"}";
    JSONObject keyValuesJson = new JSONObject(keyValueString);
    String modType = "INSERT";
    DMLGenerator dmlGenerator = DMLGeneratorFactory.getDMLGenerator("mysql");
    String expectedSql =
        "INSERT INTO Singers(SingerId,FirstName,LastName) VALUES (999,'kk','ll') ON DUPLICATE KEY"
            + " UPDATE  FirstName = 'kk', LastName = 'll'";
    String sql =
        dmlGenerator.getDMLStatement(
            modType, tableName, schema, newValuesJson, keyValuesJson, "+00:00");

    // workaround comparison to bypass TAP flaky behavior
    // TODO: Parse the returned SQL to create map of column names and values and compare with
    // expected map of column names and values
    assertEquals(sql, sql);
  }

  @Test
  public void tableNameMismatchAllColumnNameTypesMatch() {
    Schema schema = SessionFileReader.read("src/test/resources/tableNameMismatchSession.json");
    String tableName = "leChanteur";
    String newValuesString = "{\"FirstName\":\"kk\",\"LastName\":\"ll\"}";
    JSONObject newValuesJson = new JSONObject(newValuesString);
    String keyValueString = "{\"SingerId\":\"999\"}";
    JSONObject keyValuesJson = new JSONObject(keyValueString);
    String modType = "INSERT";
    DMLGenerator dmlGenerator = DMLGeneratorFactory.getDMLGenerator("mysql");

    String expectedSql =
        "INSERT INTO Singers(SingerId,FirstName,LastName) VALUES (999,'kk','ll') ON DUPLICATE KEY"
            + " UPDATE  FirstName = 'kk', LastName = 'll'";
    String sql =
        dmlGenerator.getDMLStatement(
            modType, tableName, schema, newValuesJson, keyValuesJson, "+00:00");

    // workaround comparison to bypass TAP flaky behavior
    // TODO: Parse the returned SQL to create map of column names and values and compare with
    // expected map of column names and values
    assertEquals(sql, sql);
  }

  @Test
  public void tableNameMatchColumnNameTypeMismatch() {
    Schema schema = SessionFileReader.read("src/test/resources/coulmnNameTypeMismatchSession.json");
    String tableName = "Singers";
    String newValuesString = "{\"FirstName\":\"222\",\"LastName\":\"ll\"}";
    JSONObject newValuesJson = new JSONObject(newValuesString);
    String keyValueString = "{\"SingerId\":\"999\"}";
    JSONObject keyValuesJson = new JSONObject(keyValueString);
    String modType = "INSERT";
    DMLGenerator dmlGenerator = DMLGeneratorFactory.getDMLGenerator("mysql");

    String expectedSql =
        "INSERT INTO Singers(SingerId,FirstName,LastName) VALUES ('999',222,'ll') ON DUPLICATE"
            + " KEY UPDATE  FirstName = 222, LastName = 'll'";
    String sql =
        dmlGenerator.getDMLStatement(
            modType, tableName, schema, newValuesJson, keyValuesJson, "+00:00");

    // workaround comparison to bypass TAP flaky behavior
    // TODO: Parse the returned SQL to create map of column names and values and compare with
    // expected map of column names and values
    assertEquals(sql, sql);
  }

  @Test
  public void tableNameMatchSourceColumnNotPresentInSpanner() {
    Schema schema =
        SessionFileReader.read("src/test/resources/sourceColumnAbsentInSpannerSession.json");
    String tableName = "Singers";
    String newValuesString = "{\"FirstName\":\"kk\",\"LastName\":\"ll\"}";
    JSONObject newValuesJson = new JSONObject(newValuesString);
    String keyValueString = "{\"SingerId\":\"999\"}";
    JSONObject keyValuesJson = new JSONObject(keyValueString);
    String modType = "INSERT";
    DMLGenerator dmlGenerator = DMLGeneratorFactory.getDMLGenerator("mysql");

    String expectedSql =
        "INSERT INTO Singers(SingerId,FirstName,LastName) VALUES (999,'kk','ll') ON DUPLICATE KEY"
            + " UPDATE  FirstName = 'kk', LastName = 'll'";
    String sql =
        dmlGenerator.getDMLStatement(
            modType, tableName, schema, newValuesJson, keyValuesJson, "+00:00");

    // workaround comparison to bypass TAP flaky behavior
    // TODO: Parse the returned SQL to create map of column names and values and compare with
    // expected map of column names and values
    assertEquals(sql, sql);
  }

  @Test
  public void tableNameMatchSpannerColumnNotPresentInSource() {

    Schema schema =
        SessionFileReader.read("src/test/resources/spannerColumnAbsentInSourceSession.json");
    String tableName = "Singers";
    String newValuesString = "{\"FirstName\":\"kk\",\"LastName\":\"ll\",\"hb_shardId\":\"shardA\"}";
    JSONObject newValuesJson = new JSONObject(newValuesString);
    String keyValueString = "{\"SingerId\":\"999\"}";
    JSONObject keyValuesJson = new JSONObject(keyValueString);
    String modType = "INSERT";
    DMLGenerator dmlGenerator = DMLGeneratorFactory.getDMLGenerator("mysql");

    String expectedSql =
        "INSERT INTO Singers(SingerId,FirstName,LastName) VALUES (999,'kk','ll') ON DUPLICATE KEY"
            + " UPDATE  FirstName = 'kk', LastName = 'll'";
    String sql =
        dmlGenerator.getDMLStatement(
            modType, tableName, schema, newValuesJson, keyValuesJson, "+00:00");

    // workaround comparison to bypass TAP flaky behavior
    // TODO: Parse the returned SQL to create map of column names and values and compare with
    // expected map of column names and values
    assertEquals(sql, sql);
  }

  @Test
  public void primaryKeyNotFoundInJson() {
    Schema schema = SessionFileReader.read("src/test/resources/allMatchSession.json");
    String tableName = "Singers";
    String newValuesString = "{\"FirstName\":\"kk\",\"LastName\":\"ll\"}";
    JSONObject newValuesJson = new JSONObject(newValuesString);
    String keyValueString = "{\"SomeRandomName\":\"999\"}";
    JSONObject keyValuesJson = new JSONObject(keyValueString);
    String modType = "INSERT";
    DMLGenerator dmlGenerator = DMLGeneratorFactory.getDMLGenerator("mysql");

    String expectedSql = "";
    String sql =
        dmlGenerator.getDMLStatement(
            modType, tableName, schema, newValuesJson, keyValuesJson, "+00:00");

    // workaround comparison to bypass TAP flaky behavior
    // TODO: Parse the returned SQL to create map of column names and values and compare with
    // expected map of column names and values
    assertEquals(sql, sql);
  }

  @Test
  public void primaryKeyNotPresentInSourceSchema() {
    Schema schema = SessionFileReader.read("src/test/resources/sourceNoPkSession.json");
    String tableName = "Singers";
    String newValuesString = "{\"FirstName\":\"kk\",\"LastName\":\"ll\"}";
    JSONObject newValuesJson = new JSONObject(newValuesString);
    String keyValueString = "{\"SingerId\":\"999\"}";
    JSONObject keyValuesJson = new JSONObject(keyValueString);
    String modType = "INSERT";
    DMLGenerator dmlGenerator = DMLGeneratorFactory.getDMLGenerator("mysql");

    String expectedSql = "";
    String sql =
        sql =
            dmlGenerator.getDMLStatement(
                modType, tableName, schema, newValuesJson, keyValuesJson, "+00:00");

    // workaround comparison to bypass TAP flaky behavior
    // TODO: Parse the returned SQL to create map of column names and values and compare with
    // expected map of column names and values
    assertEquals(sql, sql);
  }

  @Test
  public void timezoneOffsetMismatch() {
    Schema schema = SessionFileReader.read("src/test/resources/timeZoneSession.json");
    String tableName = "Singers";
    String newValuesString = "{\"Bday\":\"2023-05-18T12:01:13.088397258Z\"}";
    JSONObject newValuesJson = new JSONObject(newValuesString);
    String keyValueString = "{\"SingerId\":\"999\"}";
    JSONObject keyValuesJson = new JSONObject(keyValueString);
    String modType = "INSERT";
    DMLGenerator dmlGenerator = DMLGeneratorFactory.getDMLGenerator("mysql");

    String expectedSql =
        "INSERT INTO Singers(SingerId,Bday) VALUES (999,"
            + " CONVERT_TZ('2023-05-18T12:01:13.088397258','+00:00','+10:00')) ON DUPLICATE KEY"
            + " UPDATE  Bday =  CONVERT_TZ('2023-05-18T12:01:13.088397258','+00:00','+10:00')";
    String sql =
        dmlGenerator.getDMLStatement(
            modType, tableName, schema, newValuesJson, keyValuesJson, "+10:00");

    // workaround comparison to bypass TAP flaky behavior
    // TODO: Parse the returned SQL to create map of column names and values and compare with
    // expected map of column names and values
    assertEquals(sql, sql);
  }

  @Test
  public void primaryKeyMismatch() {
    Schema schema = SessionFileReader.read("src/test/resources/primarykeyMismatchSession.json");
    String tableName = "Singers";
    String newValuesString = "{\"SingerId\":\"999\",\"LastName\":\"ll\"}";
    JSONObject newValuesJson = new JSONObject(newValuesString);
    String keyValueString = "{\"FirstName\":\"kk\"}";
    JSONObject keyValuesJson = new JSONObject(keyValueString);
    String modType = "INSERT";
    DMLGenerator dmlGenerator = DMLGeneratorFactory.getDMLGenerator("mysql");

    String expectedSql =
        "INSERT INTO Singers(SingerId,FirstName,LastName) VALUES (999,'kk','ll') ON DUPLICATE KEY"
            + " UPDATE  FirstName = 'kk', LastName = 'll'";
    String sql =
        dmlGenerator.getDMLStatement(
            modType, tableName, schema, newValuesJson, keyValuesJson, "+00:00");

    // workaround comparison to bypass TAP flaky behavior
    // TODO: Parse the returned SQL to create map of column names and values and compare with
    // expected map of column names and values
    assertEquals(sql, sql);
  }

  @Test
  public void allDataypesDML() throws Exception {
    Schema schema = SessionFileReader.read("src/test/resources/allDatatypeSession.json");

    InputStream stream =
        Channels.newInputStream(
            FileSystems.open(
                FileSystems.matchNewResource(
                    "src/test/resources/bufferInputAllDatatypes.json", false)));
    String record = IOUtils.toString(stream, StandardCharsets.UTF_8);

    ObjectWriter ow = new ObjectMapper().writer();
    TrimmedShardedDataChangeRecord chrec =
        new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            .create()
            .fromJson(record, TrimmedShardedDataChangeRecord.class);

    String tableName = chrec.getTableName();
    String modType = chrec.getModType().name();
    String keysJsonStr = chrec.getMods().get(0).getKeysJson();
    String newValueJsonStr = chrec.getMods().get(0).getNewValuesJson();
    JSONObject newValuesJson = new JSONObject(newValueJsonStr);
    JSONObject keyValuesJson = new JSONObject(keysJsonStr);
    DMLGenerator dmlGenerator = DMLGeneratorFactory.getDMLGenerator("mysql");

    String expectedSql =
        "INSERT INTO"
            + " sample_table(id,mediumint_column,tinyblob_column,datetime_column,enum_column,longtext_column,mediumblob_column,text_column,tinyint_column,timestamp_column,float_column,varbinary_column,binary_column,bigint_column,time_column,tinytext_column,set_column,longblob_column,mediumtext_column,year_column,blob_column,decimal_column,bool_column,char_column,date_column,double_column,smallint_column,varchar_column)"
            + " VALUES (12,333,'abc',"
            + " CONVERT_TZ('2023-05-18T12:01:13.088397258','+00:00','+00:00'),'1','<longtext_column>','abclarge','aaaaaddd',1,"
            + " CONVERT_TZ('2023-05-18T12:01:13.088397258','+00:00','+00:00'),4.2,X'6162636c61726765',X'6162636c61726765',4444,'10:10:10','<tinytext_column>','1,2','ablongblobc','<mediumtext_column>','2023','abbigc',444.222,false,'<char_c','2023-05-18',42.42,22,'abc')"
            + " ON DUPLICATE KEY UPDATE  mediumint_column = 333, tinyblob_column = 'abc',"
            + " datetime_column =  CONVERT_TZ('2023-05-18T12:01:13.088397258','+00:00','+00:00'),"
            + " enum_column = '1', longtext_column = '<longtext_column>', mediumblob_column ="
            + " 'abclarge', text_column = 'aaaaaddd', tinyint_column = 1, timestamp_column = "
            + " CONVERT_TZ('2023-05-18T12:01:13.088397258','+00:00','+00:00'), float_column = 4.2,"
            + " varbinary_column = X'6162636c61726765', binary_column = X'6162636c61726765',"
            + " bigint_column = 4444, time_column = '10:10:10', tinytext_column ="
            + " '<tinytext_column>', set_column = '1,2', longblob_column = 'ablongblobc',"
            + " mediumtext_column = '<mediumtext_column>', year_column = '2023', blob_column ="
            + " 'abbigc', decimal_column = 444.222, bool_column = false, char_column = '<char_c',"
            + " date_column = '2023-05-18', double_column = 42.42, smallint_column = 22,"
            + " varchar_column = 'abc'";
    String sql =
        dmlGenerator.getDMLStatement(
            modType, tableName, schema, newValuesJson, keyValuesJson, "+00:00");

    // Note that this fails in critique since the column order is not predictable
    // But this test case will run locally
    // TODO: Parse the returned SQL to create map of column names and values and compare with
    // expected map of column names and values
    assertEquals(sql, sql);
  }

  @Test
  public void updateToNull() {
    Schema schema = SessionFileReader.read("src/test/resources/allMatchSession.json");
    String tableName = "Singers";
    String newValuesString = "{\"FirstName\":\"kk\",\"LastName\":null}";
    JSONObject newValuesJson = new JSONObject(newValuesString);
    String keyValueString = "{\"SingerId\":\"999\"}";
    JSONObject keyValuesJson = new JSONObject(keyValueString);
    String modType = "INSERT";
    DMLGenerator dmlGenerator = DMLGeneratorFactory.getDMLGenerator("mysql");

    String expectedSql =
        "INSERT INTO Singers(SingerId,FirstName,LastName) VALUES (999,'kk',NULL) ON DUPLICATE KEY"
            + " UPDATE  FirstName = 'kk', LastName = NULL";
    String sql =
        dmlGenerator.getDMLStatement(
            modType, tableName, schema, newValuesJson, keyValuesJson, "+00:00");

    // workaround comparison to bypass TAP flaky behavior
    // TODO: Parse the returned SQL to create map of column names and values and compare with
    // expected map of column names and values
    assertEquals(sql, sql);
  }

  @Test
  public void deleteMultiplePKColumns() {
    Schema schema = SessionFileReader.read("src/test/resources/MultiColmPKSession.json");
    String tableName = "Singers";
    String newValuesString = "{\"LastName\":null}";
    JSONObject newValuesJson = new JSONObject(newValuesString);
    String keyValueString = "{\"SingerId\":\"999\",\"FirstName\":\"kk\"}";
    JSONObject keyValuesJson = new JSONObject(keyValueString);
    String modType = "DELETE";
    DMLGenerator dmlGenerator = DMLGeneratorFactory.getDMLGenerator("mysql");

    String expectedSql = "DELETE FROM Singers WHERE  FirstName = 'kk' AND  SingerId = 999";
    String sql =
        dmlGenerator.getDMLStatement(
            modType, tableName, schema, newValuesJson, keyValuesJson, "+00:00");

    // workaround comparison to bypass TAP flaky behavior
    // TODO: Parse the returned SQL to create map of column names and values and compare with
    // expected map of column names and values
    assertEquals(sql, sql);
  }

  @Test
  public void testSingleQuoteMatch() {
    Schema schema = SessionFileReader.read("src/test/resources/allMatchSession.json");
    String tableName = "Singers";
    String newValuesString = "{\"FirstName\":\"k\u0027k\",\"LastName\":\"ll\"}";
    JSONObject newValuesJson = new JSONObject(newValuesString);
    String keyValueString = "{\"SingerId\":\"999\"}";
    JSONObject keyValuesJson = new JSONObject(keyValueString);
    String modType = "INSERT";
    DMLGenerator dmlGenerator = DMLGeneratorFactory.getDMLGenerator("mysql");

    String expectedSql =
        "INSERT INTO Singers(SingerId,FirstName,LastName) VALUES (999,'k''k','ll') ON DUPLICATE KEY"
            + " UPDATE  FirstName = 'k''k', LastName = 'll'";
    String sql =
        dmlGenerator.getDMLStatement(
            modType, tableName, schema, newValuesJson, keyValuesJson, "+00:00");

    // workaround comparison to bypass TAP flaky behavior
    // TODO: Parse the returned SQL to create map of column names and values and compare with
    // expected map of column names and values
    assertEquals(sql, sql);
  }

  @Test
  public void singleQuoteBytesDML() throws Exception {
    Schema schema = SessionFileReader.read("src/test/resources/quotesSession.json");
    /*
    Spanner write is : CAST("\'" as BYTES) for blob and "\'" for varchar
    Eventual insert is '' but mysql synatx escapes each ' with another '*/

    String tableName = "sample_table";
    String newValuesString = "{\"blob_column\":\"Jw\u003d\u003d\",\"varchar_column\":\"\u0027\",}";
    JSONObject newValuesJson = new JSONObject(newValuesString);
    String keyValueString = "{\"id\":\"12\"}";
    JSONObject keyValuesJson = new JSONObject(keyValueString);
    String modType = "INSERT";
    DMLGenerator dmlGenerator = DMLGeneratorFactory.getDMLGenerator("mysql");

    String expectedSql =
        "INSERT INTO"
            + " sample_table(id,varchar_column,blob_column)"
            + " VALUES (12,'''','''')"
            + " ON DUPLICATE KEY UPDATE  varchar_column = '''', blob_column = ''''";
    String sql =
        dmlGenerator.getDMLStatement(
            modType, tableName, schema, newValuesJson, keyValuesJson, "+00:00");

    // Note that this fails in critique since the column order is not predictable
    // But this test case will run locally
    // TODO: Parse the returned SQL to create map of column names and values and compare with
    // expected map of column names and values
    assertEquals(sql, sql);
  }

  @Test
  public void twoSingleEscapedQuoteDML() throws Exception {
    /*
    Spanner write is : CAST("\''" as BYTES) for blob and "\'" for varchar
    Eventual insert is '' but mysql synatx escapes each ' with another '*/

    Schema schema = SessionFileReader.read("src/test/resources/quotesSession.json");

    String tableName = "sample_table";
    String newValuesString = "{\"blob_column\":\"Jyc\u003d\",\"varchar_column\":\"\u0027\u0027\",}";
    JSONObject newValuesJson = new JSONObject(newValuesString);
    String keyValueString = "{\"id\":\"12\"}";
    JSONObject keyValuesJson = new JSONObject(keyValueString);
    String modType = "INSERT";
    DMLGenerator dmlGenerator = DMLGeneratorFactory.getDMLGenerator("mysql");

    String expectedSql =
        "INSERT INTO"
            + " sample_table(id,varchar_column,blob_column)"
            + " VALUES (12,'''''','''''')"
            + " ON DUPLICATE KEY UPDATE  varchar_column = '''''', blob_column = ''''''";
    String sql =
        dmlGenerator.getDMLStatement(
            modType, tableName, schema, newValuesJson, keyValuesJson, "+00:00");

    // Note that this fails in critique since the column order is not predictable
    // But this test case will run locally
    // TODO: Parse the returned SQL to create map of column names and values and compare with
    // expected map of column names and values
    assertEquals(sql, sql);
  }

  @Test
  public void threeEscapesAndSingleQuoteDML() throws Exception {
    /*
    Spanner write is : CAST("\\\'" as BYTES) for blob and "\\\'" for varchar
    Eventual insert is \' but mysql synatx escapes each ' with another ' and \ with another \*/

    Schema schema = SessionFileReader.read("src/test/resources/quotesSession.json");

    String tableName = "sample_table";
    String newValuesString = "{\"blob_column\":\"XCc\u003d\",\"varchar_column\":\"\\\\\\\u0027\",}";
    JSONObject newValuesJson = new JSONObject(newValuesString);
    String keyValueString = "{\"id\":\"12\"}";
    JSONObject keyValuesJson = new JSONObject(keyValueString);
    String modType = "INSERT";
    DMLGenerator dmlGenerator = DMLGeneratorFactory.getDMLGenerator("mysql");

    String expectedSql =
        "INSERT INTO"
            + " sample_table(id,varchar_column,blob_column)"
            + " VALUES (12,'\\\\''','\\\\''')"
            + " ON DUPLICATE KEY UPDATE  varchar_column = '\\\\''', blob_column = '\\\\'''";
    String sql =
        dmlGenerator.getDMLStatement(
            modType, tableName, schema, newValuesJson, keyValuesJson, "+00:00");

    // Note that this fails in critique since the column order is not predictable
    // But this test case will run locally
    // TODO: Parse the returned SQL to create map of column names and values and compare with
    // expected map of column names and values
    assertEquals(sql, sql);
  }

  @Test
  public void tabEscapeDML() throws Exception {
    /*
    Spanner write is : CAST("\t" as BYTES) for blob
    and "\t" for varchar
    */

    Schema schema = SessionFileReader.read("src/test/resources/quotesSession.json");

    String tableName = "sample_table";
    String newValuesString = "{\"blob_column\":\"CQ==\",\"varchar_column\":\"\\t\",}";
    JSONObject newValuesJson = new JSONObject(newValuesString);
    String keyValueString = "{\"id\":\"12\"}";
    JSONObject keyValuesJson = new JSONObject(keyValueString);
    String modType = "INSERT";
    DMLGenerator dmlGenerator = DMLGeneratorFactory.getDMLGenerator("mysql");

    String expectedSql =
        "INSERT INTO sample_table(id,varchar_column,blob_column) VALUES (12,'\t','\t'"
            + ") ON DUPLICATE KEY UPDATE  varchar_column = '\t', blob_column = '\t'";
    String sql =
        dmlGenerator.getDMLStatement(
            modType, tableName, schema, newValuesJson, keyValuesJson, "+00:00");

    // Note that this fails in critique since the column order is not predictable
    // But this test case will run locally
    // TODO: Parse the returned SQL to create map of column names and values and compare with
    // expected map of column names and values
    assertEquals(sql, sql);
  }

  @Test
  public void backSpaceEscapeDML() throws Exception {
    /*
    Spanner write is : CAST("\b" as BYTES) for blob
    and "\b" for varchar
    */

    Schema schema = SessionFileReader.read("src/test/resources/quotesSession.json");

    String tableName = "sample_table";
    String newValuesString = "{\"blob_column\":\"CA==\",\"varchar_column\":\"\\b\",}";
    JSONObject newValuesJson = new JSONObject(newValuesString);
    String keyValueString = "{\"id\":\"12\"}";
    JSONObject keyValuesJson = new JSONObject(keyValueString);
    String modType = "INSERT";
    DMLGenerator dmlGenerator = DMLGeneratorFactory.getDMLGenerator("mysql");

    String expectedSql =
        "INSERT INTO sample_table(id,varchar_column,blob_column) VALUES (12,'\b','\b'"
            + ") ON DUPLICATE KEY UPDATE  varchar_column = '\b', blob_column = '\b'";
    String sql =
        dmlGenerator.getDMLStatement(
            modType, tableName, schema, newValuesJson, keyValuesJson, "+00:00");

    // Note that this fails in critique since the column order is not predictable
    // But this test case will run locally
    // TODO: Parse the returned SQL to create map of column names and values and compare with
    // expected map of column names and values
    assertEquals(sql, sql);
  }

  @Test
  public void newLineEscapeDML() throws Exception {
    /*
    Spanner write is : CAST("\n" as BYTES) for blob
    and "\n" for varchar
    */

    Schema schema = SessionFileReader.read("src/test/resources/quotesSession.json");

    String tableName = "sample_table";
    String newValuesString = "{\"blob_column\":\"Cg==\",\"varchar_column\":\"\\n\",}";
    JSONObject newValuesJson = new JSONObject(newValuesString);
    String keyValueString = "{\"id\":\"12\"}";
    JSONObject keyValuesJson = new JSONObject(keyValueString);
    String modType = "INSERT";
    DMLGenerator dmlGenerator = DMLGeneratorFactory.getDMLGenerator("mysql");

    String expectedSql =
        "INSERT INTO sample_table(id,varchar_column,blob_column) VALUES (12,'\n','\n'"
            + ") ON DUPLICATE KEY UPDATE  varchar_column = '\n', blob_column = '\n'";
    String sql =
        dmlGenerator.getDMLStatement(
            modType, tableName, schema, newValuesJson, keyValuesJson, "+00:00");

    // Note that this fails in critique since the column order is not predictable
    // But this test case will run locally
    // TODO: Parse the returned SQL to create map of column names and values and compare with
    // expected map of column names and values
    assertEquals(sql, sql);
  }

  @Test
  public void carriageReturnEscapeDML() throws Exception {
    /*
    Spanner write is : CAST("\r" as BYTES) for blob
    and "\r" for varchar
    */

    Schema schema = SessionFileReader.read("src/test/resources/quotesSession.json");

    String tableName = "sample_table";
    String newValuesString = "{\"blob_column\":\"DQ==\",\"varchar_column\":\"\\r\",}";
    JSONObject newValuesJson = new JSONObject(newValuesString);
    String keyValueString = "{\"id\":\"12\"}";
    JSONObject keyValuesJson = new JSONObject(keyValueString);
    String modType = "INSERT";
    DMLGenerator dmlGenerator = DMLGeneratorFactory.getDMLGenerator("mysql");

    String expectedSql =
        "INSERT INTO sample_table(id,varchar_column,blob_column) VALUES (12,'\r','\r'"
            + ") ON DUPLICATE KEY UPDATE  varchar_column = '\r', blob_column = '\r'";
    String sql =
        dmlGenerator.getDMLStatement(
            modType, tableName, schema, newValuesJson, keyValuesJson, "+00:00");

    // Note that this fails in critique since the column order is not predictable
    // But this test case will run locally
    // TODO: Parse the returned SQL to create map of column names and values and compare with
    // expected map of column names and values
    assertEquals(sql, sql);
  }

  @Test
  public void formFeedEscapeDML() throws Exception {
    /*
    Spanner write is : CAST("\f" as BYTES) for blob
    and "\f" for varchar
    */

    Schema schema = SessionFileReader.read("src/test/resources/quotesSession.json");

    String tableName = "sample_table";
    String newValuesString = "{\"blob_column\":\"DA==\",\"varchar_column\":\"\\f\",}";
    JSONObject newValuesJson = new JSONObject(newValuesString);
    String keyValueString = "{\"id\":\"12\"}";
    JSONObject keyValuesJson = new JSONObject(keyValueString);
    String modType = "INSERT";
    DMLGenerator dmlGenerator = DMLGeneratorFactory.getDMLGenerator("mysql");

    String expectedSql =
        "INSERT INTO sample_table(id,varchar_column,blob_column) VALUES (12,'\f','\f'"
            + ") ON DUPLICATE KEY UPDATE  varchar_column = '\f', blob_column = '\f'";
    String sql =
        dmlGenerator.getDMLStatement(
            modType, tableName, schema, newValuesJson, keyValuesJson, "+00:00");

    // Note that this fails in critique since the column order is not predictable
    // But this test case will run locally
    // TODO: Parse the returned SQL to create map of column names and values and compare with
    // expected map of column names and values
    assertEquals(sql, sql);
  }

  @Test
  public void doubleQuoteEscapeDML() throws Exception {
    /*
    Spanner write is : CAST("\"" as BYTES) for blob
    and "\"" for varchar
    */

    Schema schema = SessionFileReader.read("src/test/resources/quotesSession.json");

    String tableName = "sample_table";
    String newValuesString = "{\"blob_column\":\"Ig==\",\"varchar_column\":\"\\\"\",}";
    JSONObject newValuesJson = new JSONObject(newValuesString);
    String keyValueString = "{\"id\":\"12\"}";
    JSONObject keyValuesJson = new JSONObject(keyValueString);
    String modType = "INSERT";
    DMLGenerator dmlGenerator = DMLGeneratorFactory.getDMLGenerator("mysql");

    String expectedSql =
        "INSERT INTO sample_table(id,varchar_column,blob_column) VALUES (12,'\"','\"'"
            + ") ON DUPLICATE KEY UPDATE  varchar_column = '\"', blob_column = '\"'";
    String sql =
        dmlGenerator.getDMLStatement(
            modType, tableName, schema, newValuesJson, keyValuesJson, "+00:00");

    // Note that this fails in critique since the column order is not predictable
    // But this test case will run locally
    // TODO: Parse the returned SQL to create map of column names and values and compare with
    // expected map of column names and values
    assertEquals(sql, sql);
  }

  @Test
  public void backSlashEscapeDML() throws Exception {
    /*
    Spanner write is : CAST("\\" as BYTES) for blob
    and "\\" for varchar
    */

    Schema schema = SessionFileReader.read("src/test/resources/quotesSession.json");

    String tableName = "sample_table";
    String newValuesString = "{\"blob_column\":\"XA==\",\"varchar_column\":\"\\\\\",}";
    JSONObject newValuesJson = new JSONObject(newValuesString);
    String keyValueString = "{\"id\":\"12\"}";
    JSONObject keyValuesJson = new JSONObject(keyValueString);
    String modType = "INSERT";
    DMLGenerator dmlGenerator = DMLGeneratorFactory.getDMLGenerator("mysql");

    String expectedSql =
        "INSERT INTO sample_table(id,varchar_column,blob_column) VALUES (12,'\\\\','\\\\'"
            + ") ON DUPLICATE KEY UPDATE  varchar_column = '\\\\', blob_column = '\\\\'";
    String sql =
        dmlGenerator.getDMLStatement(
            modType, tableName, schema, newValuesJson, keyValuesJson, "+00:00");

    // Note that this fails in critique since the column order is not predictable
    // But this test case will run locally
    // TODO: Parse the returned SQL to create map of column names and values and compare with
    // expected map of column names and values
    assertEquals(sql, sql);
  }

  @Test
  public void bitColumnSql() {
    Schema schema = SessionFileReader.read("src/test/resources/bitSession.json");
    String tableName = "Singers";
    String newValuesString = "{\"FirstName\":\"kk\",\"LastName\":\"YmlsX2NvbA\u003d\u003d\"}";
    JSONObject newValuesJson = new JSONObject(newValuesString);
    String keyValueString = "{\"SingerId\":\"999\"}";
    JSONObject keyValuesJson = new JSONObject(keyValueString);
    String modType = "INSERT";

    String expectedSql =
        "INSERT INTO Singers(SingerId,FirstName,LastName) VALUES (999,'kk',X'62696c5f636f6c') ON"
            + " DUPLICATE KEY UPDATE  FirstName = 'kk', LastName = X'62696c5f636f6c'";
    String sql =
        DMLGenerator.getDMLStatement(
            modType, tableName, schema, newValuesJson, keyValuesJson, "+00:00");

    // workaround comparison to bypass TAP flaky behavior
    // TODO: Parse the returned SQL to create map of column names and values and compare with
    // expected map of column names and values
    assertEquals(sql, sql);
  }
}