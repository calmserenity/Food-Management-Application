package ch.makery.address.model

import scalikejdbc._

// Represents a single food bank record
case class FoodBank(
                     id: Option[Long],
                     name: String,
                     location: String,
                     personInCharge: String,
                     yearFounded: Int,
                     phoneNumber: Option[String],
                     email: Option[String],
                     operatingHours: Option[String],
                     website: Option[String],
                     currentNeeds: Option[String],
                     imagePath: Option[String]
                   )

// Companion object for the FoodBank model
object FoodBank {

  // Maps a database row to a FoodBank object
  private def fromResultSet(rs: WrappedResultSet): FoodBank = FoodBank(
    id = Some(rs.long("id")),
    name = rs.string("name"),
    location = rs.string("location"),
    personInCharge = rs.string("person_in_charge"),
    yearFounded = rs.int("year_founded"),
    phoneNumber = rs.stringOpt("phone_number"),
    email = rs.stringOpt("email"),
    operatingHours = rs.stringOpt("operating_hours"),
    website = rs.stringOpt("website"),
    currentNeeds = rs.stringOpt("current_needs"),
    imagePath = rs.stringOpt("image_path")
  )

  // Creates a new food bank record in the database
  def create(
              name: String,
              location: String,
              personInCharge: String,
              yearFounded: Int,
              phoneNumber: Option[String] = None,
              email: Option[String] = None,
              operatingHours: Option[String] = None,
              website: Option[String] = None,
              currentNeeds: Option[String] = None,
              imagePath: Option[String] = None
            ): Long = {
    DB.autoCommit { implicit session =>
      sql"""
        INSERT INTO food_banks (
          name, location, person_in_charge, year_founded,
          phone_number, email, operating_hours, website, current_needs, image_path
        ) VALUES (
          ${name}, ${location}, ${personInCharge}, ${yearFounded},
          ${phoneNumber}, ${email}, ${operatingHours}, ${website}, ${currentNeeds}, ${imagePath}
        )
      """.updateAndReturnGeneratedKey.apply()
    }
  }

  // Finds a single food bank by its name
  def findByName(name: String): Option[FoodBank] = {
    DB.readOnly { implicit session =>
      sql"SELECT * FROM food_banks WHERE name = ${name}"
        .map(fromResultSet) 
        .single
        .apply()
    }
  }

  // Retrieves all food banks from the database
  def findAll(): List[FoodBank] = {
    DB.readOnly { implicit session =>
      sql"SELECT * FROM food_banks ORDER BY name"
        .map(fromResultSet) 
        .list
        .apply()
    }
  }

  // Finds a single food bank by its unique ID
  def findById(id: Long): Option[FoodBank] = {
    DB.readOnly { implicit session =>
      sql"SELECT * FROM food_banks WHERE id = ${id}"
        .map(fromResultSet)
        .single
        .apply()
    }
  }

  // Updates an existing food bank record in the database
  def update(foodBank: FoodBank): Int = {
    DB.autoCommit { implicit session =>
      sql"""
        UPDATE food_banks SET
          name = ${foodBank.name},
          location = ${foodBank.location},
          person_in_charge = ${foodBank.personInCharge},
          year_founded = ${foodBank.yearFounded},
          phone_number = ${foodBank.phoneNumber},
          email = ${foodBank.email},
          operating_hours = ${foodBank.operatingHours},
          website = ${foodBank.website},
          current_needs = ${foodBank.currentNeeds},
          image_path = ${foodBank.imagePath}
        WHERE id = ${foodBank.id}
      """.update.apply()
    }
  }
}