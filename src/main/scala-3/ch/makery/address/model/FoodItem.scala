package ch.makery.address.model

import scalikejdbc._
import java.time.LocalDate
import scala.beans.BeanProperty 

// Represents a single food item in a food bank's inventory
case class FoodItem(
                     id: Long,
                     foodBankId: Long,

                     @BeanProperty 
                     name: String,

                     @BeanProperty 
                     category: String,

                     @BeanProperty 
                     quantity: Int,

                     @BeanProperty 
                     expiryDate: Option[LocalDate],

                     imagePath: Option[String]
                   )

object FoodItem {
  // Maps a database row to a FoodItem object
  private val fi = (rs: WrappedResultSet) => FoodItem(
    id = rs.long("id"),
    foodBankId = rs.long("food_bank_id"),
    name = rs.string("name"),
    category = rs.string("category"),
    quantity = rs.int("quantity"),
    expiryDate = rs.localDateOpt("expiry_date"),
    imagePath = rs.stringOpt("image_path")
  )

  // Creates a new food item record in the database
  def create(foodBankId: Long, name: String, category: String, quantity: Int, imagePath: Option[String] = None, expiryDate: Option[LocalDate] = None): Long = {
    DB.autoCommit { implicit session =>
      sql"""
        INSERT INTO food_items (food_bank_id, name, category, quantity, expiry_date, image_path)
        VALUES (${foodBankId}, ${name}, ${category}, ${quantity}, ${expiryDate}, ${imagePath})
      """.updateAndReturnGeneratedKey.apply()
    }
  }

  // Finds all food items for a specific food bank
  def findByFoodBank(foodBankId: Long): Seq[FoodItem] = {
    DB.readOnly { implicit session =>
      sql"SELECT * FROM food_items WHERE food_bank_id = ${foodBankId} AND quantity > 0 ORDER BY name"
        .map(fi).list.apply()
    }
  }

  // Counts the total number of food item records in the database
  def countAll(): Long = {
    DB.readOnly { implicit session =>
      sql"SELECT count(1) FROM food_items".map(rs => rs.long(1)).single.apply().getOrElse(0)
    }
  }

  // Finds a single food item by its name and food bank
  def findByNameAndFoodBank(name: String, foodBankId: Long): Option[FoodItem] = {
    DB.readOnly { implicit session =>
      sql"SELECT * FROM food_items WHERE name = ${name} AND food_bank_id = ${foodBankId}"
        .map(fi).single.apply()
    }
  }

  // Updates the quantity of a specific food item
  def updateQuantity(id: Long, newQuantity: Int): Int = {
    DB.autoCommit { implicit session =>
      sql"UPDATE food_items SET quantity = ${newQuantity} WHERE id = ${id}"
        .update.apply()
    }
  }
}