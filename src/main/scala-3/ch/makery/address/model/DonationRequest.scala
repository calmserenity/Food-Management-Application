package ch.makery.address.model

import scalikejdbc._
import java.time.LocalDateTime
import upickle.default._
import scala.beans.BeanProperty

// Represents a single item within a donation request
case class RequestedItem(name: String, quantity: Int)
// Provides a JSON reader/writer for the RequestedItem case class
implicit val requestedItemRW: ReadWriter[RequestedItem] = macroRW

// Represents a single donation request record
case class DonationRequest(
                            id: Long,
                            userUsername: String,
                            foodBankId: Long,
                            @BeanProperty fullName: String,
                            age: Int,
                            gender: Option[String],
                            requestedItems: List[RequestedItem],
                            @BeanProperty deliveryMethod: String,
                            notes: Option[String],
                            @BeanProperty requestDate: LocalDateTime,
                            @BeanProperty status: String
                          )

object DonationRequest {

  // Maps a database row to a DonationRequest object
  private val dr = (rs: WrappedResultSet) => DonationRequest(
    id = rs.long("id"),
    userUsername = rs.string("user_username"),
    foodBankId = rs.long("food_bank_id"),
    fullName = rs.string("full_name"),
    age = rs.int("age"),
    gender = rs.stringOpt("gender"),
    requestedItems = read[List[RequestedItem]](rs.string("requested_items_json")), 
    deliveryMethod = rs.string("delivery_method"),
    notes = rs.stringOpt("notes"),
    requestDate = rs.localDateTime("request_date"),
    status = rs.string("status")
  )

  // Creates a new donation request record in the database
  def create(
              userUsername: String,
              foodBankId: Long,
              fullName: String,
              age: Int,
              gender: Option[String],
              requestedItems: List[RequestedItem],
              deliveryMethod: String,
              notes: Option[String]
            ): Long = {
    // Serialize the list of requested items to a JSON string for storage
    val requestedItemsJson = write(requestedItems)

    DB.autoCommit { implicit session =>
      sql"""
        INSERT INTO donation_requests (
          user_username, food_bank_id, full_name, age, gender,
          requested_items_json, delivery_method, notes
        ) VALUES (
          ${userUsername}, ${foodBankId}, ${fullName}, ${age}, ${gender},
          ${requestedItemsJson}, ${deliveryMethod}, ${notes}
        )
      """.updateAndReturnGeneratedKey.apply()
    }
  }

  // Finds all donation requests for a specific food bank with a given status
  def findAllByFoodBankAndStatus(foodBankId: Long, status: String): Seq[DonationRequest] = {
    DB.readOnly { implicit session =>
      sql"SELECT * FROM donation_requests WHERE food_bank_id = ${foodBankId} AND status = ${status} ORDER BY request_date DESC"
        .map(dr).list.apply()
    }
  }

  
  // Finds all donation requests made by a specific user
  def findAllByUser(username: String): Seq[DonationRequest] = {
    DB.readOnly { implicit session =>
      sql"SELECT * FROM donation_requests WHERE user_username = ${username} ORDER BY request_date DESC"
        .map(dr).list.apply()
    }
  }

  // Updates the status of a specific donation request
  def updateStatus(id: Long, newStatus: String): Int = {
    DB.autoCommit { implicit session =>
      sql"UPDATE donation_requests SET status = ${newStatus} WHERE id = ${id}"
        .update.apply()
    }
  }
}