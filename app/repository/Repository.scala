package repository

import models.Conversion
import play.api.db.Database

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject()(db: Database) {

  def init(): Unit = {
    db.withConnection { conn =>
      val stmt = conn.createStatement()
      stmt.executeUpdate(
        """CREATE TABLE IF NOT EXISTS conversions(
          |id INTEGER PRIMARY KEY AUTOINCREMENT,
          |amount DOUBLE,
          |target TEXT,
          |result DOUBLE
          |)""".stripMargin
      )
    }
  }

  def insert(amount: Double, target: String, result: Double): Unit = {
    db.withConnection { conn =>
      val stmt = conn.prepareStatement(
        "INSERT INTO conversions(amount, target, result) VALUES (?, ?, ?)"
      )
      stmt.setDouble(1, amount)
      stmt.setString(2, target)
      stmt.setDouble(3, result)
      stmt.executeUpdate()
    }
  }

  def all(): List[Conversion] = {
    db.withConnection { conn =>
      val rs = conn.createStatement().executeQuery("SELECT * FROM conversions ORDER BY id DESC")
      Iterator
        .continually(rs)
        .takeWhile(_.next())
        .map { r =>
          Conversion(
            r.getLong("id"),
            r.getDouble("amount"),
            r.getString("target"),
            r.getDouble("result")
          )
        }
        .toList
    }
  }
}
