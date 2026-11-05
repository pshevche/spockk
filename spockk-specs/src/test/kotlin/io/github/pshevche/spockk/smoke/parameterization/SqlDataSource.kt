/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.pshevche.spockk.smoke.parameterization

import io.github.pshevche.spockk.fixtures.coverage.MigratedFrom
import io.github.pshevche.spockk.lang.expect
import io.github.pshevche.spockk.lang.variable
import io.github.pshevche.spockk.lang.variables
import io.github.pshevche.spockk.lang.where
import spock.lang.Shared
import spock.lang.Specification
import java.sql.Connection
import java.sql.DriverManager
import kotlin.math.max

class SqlDataSource : Specification() {

  companion object {
    fun rows(connection: Connection, query: String): List<Map<String, Int>> = connection.createStatement().use { statement ->
      statement.executeQuery(query).use { resultSet ->
        val columns = (1..resultSet.metaData.columnCount).map { resultSet.metaData.getColumnLabel(it).lowercase() }
        buildList {
          while (resultSet.next()) {
            add(columns.associateWith { resultSet.getInt(it) })
          }
        }
      }
    }
  }

  @Shared
  private val connection: Connection = DriverManager.getConnection("jdbc:h2:mem:")

  fun setupSpec() {
    connection.createStatement().use { statement ->
      // intentionally use "strange" column order
      statement.execute("create table data (id int primary key, a int, c int, b int)")
      statement.execute("insert into data values (1, 3, 7, 7), (2, 5, 5, 4), (3, 9, 9, 9)")
    }
  }

  fun cleanupSpec() {
    connection.close()
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.SqlDataSource#simple parameterization")
  fun `simple parameterization`(row: Map<String, Int>, a: Int, b: Int, c: Int) {
    expect
    max(a, b) == c

    where
    variable(row).from(rows(connection, "select * from data"))
    variable(a).from(row.getValue("a"))
    variable(b).from(row.getValue("b"))
    variable(c).from(row.getValue("c"))
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.SqlDataSource#multi-parameterization")
  fun `multi-parameterization`(a: Int, b: Int, c: Int) {
    expect
    max(a, b) == c

    where
    variables(a, b, c).from(
      rows(connection, "select a, b, c from data").map { it.getValue("a") },
      rows(connection, "select a, b, c from data").map { it.getValue("b") },
      rows(connection, "select a, b, c from data").map { it.getValue("c") }
    )
  }

  @MigratedFrom("org.spockframework.smoke.parameterization.SqlDataSource#multi-parameterization with wildcard")
  fun `multi-parameterization with wildcard`(a: Int, c: Int, b: Int) {
    expect
    max(a, b) == c

    where
    variables(a, c, b).from(
      rows(connection, "select * from data").map { it.getValue("a") },
      rows(connection, "select * from data").map { it.getValue("c") },
      rows(connection, "select * from data").map { it.getValue("b") }
    )
  }
}
