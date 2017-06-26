package com.github.j5ik2o.scalatestplus.db

import com.wix.mysql.EmbeddedMysql
import org.scalatest.{ fixture, MustMatchers }

class FlywayWithMySQLdMixedFreeSpec extends fixture.FreeSpec with MustMatchers with FlywayWithMySQLdMixedFixtures {

  override protected val schemaConfigs: Seq[SchemaConfig] = Seq(SchemaConfig("test"))

  var mysqld: EmbeddedMysql = _

  "FlywayWithMySQLdMixedFreeSpec" - {
    "should start & stop mysql1" in new WithFlywayContext(_ => FlywayConfig(Seq("db"))) {
      println(s"context = $mySQLdContext")
      mySQLdContext mustNot be(null)
      mySQLdContext.schemaConfigs.head.name mustBe "test"
      mysqld = mySQLdContext.embeddedMysql

    }
    "should start & stop mysql2" in new WithFlywayContext(_ => FlywayConfig(Seq("db"))) {
      println(s"context = $mySQLdContext")
      mySQLdContext mustNot be(null)
      mySQLdContext.schemaConfigs.head.name mustBe "test"
      mySQLdContext.embeddedMysql mustNot be(mysqld)
    }
  }
}
