package ch.makery.address.model

import scalikejdbc._
import java.time.{LocalDate, LocalDateTime}
import scala.beans.BeanProperty

// Represents a single donation record
case class Donation(
                     id: Long,
                     @BeanProperty userUsername: String,
                     foodBankId: Long,
                     @BeanProperty itemName: String,
                     @BeanProperty category: String,
                     @BeanProperty quantity: Int,
                     @BeanProperty deliveryMethod: String,
                     @BeanProperty expiryDate: Option[LocalDate],
                     shelfLifeDays: Option[Int],
                     notes: Option[String],
                     @BeanProperty donationDate: LocalDateTime,
                     @BeanProperty status: String
                   )

object Donation {

  // Maps a database row to a Donation object
  private val d = (rs: WrappedResultSet) => Donation(
    id = rs.long("id"),
    userUsername = rs.string("user_username"),
    foodBankId = rs.long("food_bank_id"),
    itemName = rs.string("item_name"),
    category = rs.string("category"),
    quantity = rs.int("quantity"),
    deliveryMethod = rs.string("delivery_method"),
    expiryDate = rs.localDateOpt("expiry_date"),
    shelfLifeDays = rs.intOpt("shelf_life_days"),
    notes = rs.stringOpt("notes"),
    donationDate = rs.localDateTime("donation_date"),
    status = rs.string("status")
  )

  // Creates a new donation record in the database
  def create(
              userUsername: String,
              foodBankId: Long,
              itemName: String,
              category: String,
              quantity: Int,
              deliveryMethod: String,
              expiryDate: Option[LocalDate],
              shelfLifeDays: Option[Int],
              notes: Option[String]
            ): Long = {
    DB.autoCommit { implicit session =>
      sql"""
        INSERT INTO donations (
          user_username, food_bank_id, item_name, category, quantity,
          delivery_method, expiry_date, shelf_life_days, notes
        ) VALUES (
          ${userUsername}, ${foodBankId}, ${itemName}, ${category}, ${quantity},
          ${deliveryMethod}, ${expiryDate}, ${shelfLifeDays}, ${notes}
        )
      """.updateAndReturnGeneratedKey.apply()
    }
  }

  // Finds all donations for a specific food bank with a given status
  def findAllByFoodBankAndStatus(foodBankId: Long, status: String): Seq[Donation] = {
    DB.readOnly { implicit session =>
      sql"SELECT * FROM donations WHERE food_bank_id = ${foodBankId} AND status = ${status} ORDER BY donation_date DESC"
        .map(d).list.apply()
    }
  }


  // Finds all donations made by a specific user
  def findAllByUser(username: String): Seq[Donation] = {
    DB.readOnly { implicit session =>
      sql"SELECT * FROM donations WHERE user_username = ${username} ORDER BY donation_date DESC"
        .map(d).list.apply()
    }
  }

  // Updates the status of a specific donation
  def updateStatus(id: Long, newStatus: String): Int = {
    DB.autoCommit { implicit session =>
      sql"UPDATE donations SET status = ${newStatus} WHERE id = ${id}"
        .update.apply()
    }
  }
}