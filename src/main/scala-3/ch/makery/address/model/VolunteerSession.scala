package ch.makery.address.model

import scalikejdbc._
import java.time.{LocalDate, LocalTime, LocalDateTime}
import scala.beans.BeanProperty

// Represents a single volunteer session record
case class VolunteerSession(
                             id: Long,
                             userUsername: String,
                             foodBankId: Long,

                             @BeanProperty
                             fullName: String,

                             phoneNumber: String,
                             age: Int,
                             gender: Option[String],

                             @BeanProperty
                             sessionDate: LocalDate,

                             @BeanProperty
                             startTime: LocalTime,

                             @BeanProperty
                             endTime: LocalTime,

                             activity: Option[String],
                             notes: Option[String],
                             signUpDate: LocalDateTime,
                             @BeanProperty
                             status: String
                           )

object VolunteerSession {
  // Maps a database row to a VolunteerSession object
  private val vs = (rs: WrappedResultSet) => VolunteerSession(
    id = rs.long("id"),
    userUsername = rs.string("user_username"),
    foodBankId = rs.long("food_bank_id"),
    fullName = rs.string("full_name"),
    phoneNumber = rs.string("phone_number"),
    age = rs.int("age"),
    gender = rs.stringOpt("gender"),
    sessionDate = rs.localDate("session_date"),
    startTime = rs.localTime("start_time"),
    endTime = rs.localTime("end_time"),
    activity = rs.stringOpt("activity"),
    notes = rs.stringOpt("notes"),
    signUpDate = rs.localDateTime("sign_up_date"),
    status = rs.string("status")
  )

  // Creates a new volunteer session record in the database
  def create(
              userUsername: String,
              foodBankId: Long,
              fullName: String,
              phoneNumber: String,
              age: Int,
              gender: Option[String],
              sessionDate: LocalDate,
              startTime: LocalTime,
              endTime: LocalTime,
              activity: Option[String],
              notes: Option[String]
            ): Long = {
    DB.autoCommit { implicit session =>
      sql"""
        INSERT INTO volunteer_sessions (
          user_username, food_bank_id, full_name, phone_number, age, gender,
          session_date, start_time, end_time, activity, notes
        ) VALUES (
          ${userUsername}, ${foodBankId}, ${fullName}, ${phoneNumber}, ${age}, ${gender},
          ${sessionDate}, ${startTime}, ${endTime}, ${activity}, ${notes}
        )
      """.updateAndReturnGeneratedKey.apply()
    }
  }

  // Finds all volunteer sessions for a specific food bank with a given status
  def findAllByFoodBankAndStatus(foodBankId: Long, status: String): Seq[VolunteerSession] = {
    DB.readOnly { implicit session =>
      sql"SELECT * FROM volunteer_sessions WHERE food_bank_id = ${foodBankId} AND status = ${status} ORDER BY session_date, start_time"
        .map(vs).list.apply()
    }
  }

  // Updates the status of a specific volunteer session
  def updateStatus(id: Long, newStatus: String): Int = {
    DB.autoCommit { implicit session =>
      sql"UPDATE volunteer_sessions SET status = ${newStatus} WHERE id = ${id}"
        .update.apply()
    }
  }

  // Finds all volunteer sessions for a specific user
  def findAllByUser(username: String): Seq[VolunteerSession] = {
    DB.readOnly { implicit session =>
      sql"SELECT * FROM volunteer_sessions WHERE user_username = ${username} ORDER BY session_date DESC"
        .map(vs).list.apply()
    }
  }
}
