package ch.makery.address.util

import ch.makery.address.model.{FoodBank, FoodItem, User}
import scalikejdbc.*
import java.sql.SQLException
import java.time.LocalDate

// Object for managing the application's database connection and schema
object Database {
  // Database connection details
  val jdbcDriver = "org.h2.Driver"
  val jdbcUrl = "jdbc:h2:./foodforall"
  val jdbcUser = "sa"
  val jdbcPassword = ""

  // Initialize the connection pool
  Class.forName(jdbcDriver)
  ConnectionPool.singleton(jdbcUrl, jdbcUser, jdbcPassword)

  // Sets up the database schema if the tables do not already exist
  def setup(): Unit = {
    implicit val session = AutoSession

    try {
      println("Checking and setting up database schema if necessary...")

      // Check for the existence of the 'users' table to determine if setup is needed
      val tablesExist = DB.readOnly { implicit session =>
        try {
          sql"SELECT 1 FROM users LIMIT 1".map(_.long(1)).single.apply().isDefined
        } catch {
          case e: SQLException => false
        }
      }

      if (!tablesExist) {
        println("Tables not found, creating schema...")
        // Create the 'users' table
        sql"""
        CREATE TABLE users (
          username VARCHAR(255) PRIMARY KEY,
          password_hash VARCHAR(255) NOT NULL,
          role VARCHAR(50) NOT NULL DEFAULT 'Recipient',
          image_path VARCHAR(255),
          first_name VARCHAR(255) NOT NULL,
          last_name VARCHAR(255) NOT NULL
        )
        """.execute.apply()

        // Create the 'food_banks' table
        sql"""
        CREATE TABLE food_banks (
          id BIGINT AUTO_INCREMENT PRIMARY KEY,
          name VARCHAR(255) NOT NULL,
          location VARCHAR(255) NOT NULL,
          person_in_charge VARCHAR(255) NOT NULL,
          year_founded INT NOT NULL,
          phone_number VARCHAR(20),
          email VARCHAR(255),
          operating_hours VARCHAR(255),
          website VARCHAR(255),
          current_needs TEXT,
          image_path VARCHAR(255)
        )
        """.execute.apply()

        // Create the 'food_items' table with a foreign key to 'food_banks'
        sql"""
        CREATE TABLE food_items (
          id BIGINT AUTO_INCREMENT PRIMARY KEY,
          food_bank_id BIGINT NOT NULL,
          name VARCHAR(255) NOT NULL,
          category VARCHAR(100) NOT NULL,
          quantity INT NOT NULL,
          expiry_date DATE,
          image_path VARCHAR(255),
          FOREIGN KEY (food_bank_id) REFERENCES food_banks(id)
        )
        """.execute.apply()

        // Create the 'donations' table with foreign keys to 'users' and 'food_banks'
        sql"""
        CREATE TABLE donations (
          id BIGINT AUTO_INCREMENT PRIMARY KEY,
          user_username VARCHAR(255) NOT NULL,
          food_bank_id BIGINT NOT NULL,
          item_name VARCHAR(255) NOT NULL,
          category VARCHAR(100) NOT NULL,
          quantity INT NOT NULL,
          delivery_method VARCHAR(50) NOT NULL,
          expiry_date DATE,
          shelf_life_days INT,
          notes TEXT,
          donation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
          status VARCHAR(50) NOT NULL DEFAULT 'Pledged',
          FOREIGN KEY (user_username) REFERENCES users(username),
          FOREIGN KEY (food_bank_id) REFERENCES food_banks(id)
        )
        """.execute.apply()

        // Create the 'donation_requests' table with foreign keys to 'users' and 'food_banks'
        sql"""
        CREATE TABLE donation_requests (
          id BIGINT AUTO_INCREMENT PRIMARY KEY,
          user_username VARCHAR(255) NOT NULL,
          food_bank_id BIGINT NOT NULL,
          full_name VARCHAR(255) NOT NULL,
          age INT NOT NULL,
          gender VARCHAR(50),
          requested_items_json TEXT NOT NULL,
          delivery_method VARCHAR(50) NOT NULL,
          notes TEXT,
          request_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
          status VARCHAR(50) NOT NULL DEFAULT 'Pending',
          FOREIGN KEY (user_username) REFERENCES users(username),
          FOREIGN KEY (food_bank_id) REFERENCES food_banks(id)
        )
        """.execute.apply()

        // Create the 'volunteer_sessions' table with foreign keys to 'users' and 'food_banks'
        sql"""
        CREATE TABLE volunteer_sessions (
          id BIGINT AUTO_INCREMENT PRIMARY KEY,
          user_username VARCHAR(255) NOT NULL,
          food_bank_id BIGINT NOT NULL,
          full_name VARCHAR(255) NOT NULL,
          phone_number VARCHAR(20) NOT NULL,
          age INT NOT NULL,
          gender VARCHAR(50),
          session_date DATE NOT NULL,
          start_time TIME NOT NULL,
          end_time TIME NOT NULL,
          activity VARCHAR(255),
          notes TEXT,
          sign_up_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
          status VARCHAR(50) NOT NULL DEFAULT 'Pending',
          FOREIGN KEY (user_username) REFERENCES users(username),
          FOREIGN KEY (food_bank_id) REFERENCES food_banks(id)
        )
        """.execute.apply()
        
        println("Database schema created successfully.")
        
        // --- Seed Data ---
        println("Seeding database...")
        // Seed a superadmin user
        User.create("superadmin", "password", "superadmin", Some("/image/defaulticon.png"), "Super", "Admin")
        
        // Seed food bank data
        FoodBank.create("City Food Bank", "123 Main St, Anytown", "John Doe", 2005, Some("555-1234"), Some("contact@cityfoodbank.org"), Some("Mon-Fri 9am-5pm"), None, Some("Canned goods, pasta, rice"), Some("/image/foodbankpic1.png"))
        FoodBank.create("Community Pantry", "456 Elm St, Anytown", "Jane Smith", 2010, Some("555-5678"), Some("info@communitypantry.org"), Some("Tue-Sat 10am-4pm"), None, Some("Fresh produce, bread, milk"), Some("/image/foodbankpic2.png"))

        // Seed food item data
        FoodItem.create(1, "Canned Beans", "Canned Food", 100, None, Some(LocalDate.now().plusMonths(6)))
        FoodItem.create(1, "Pasta", "Dry Goods", 200, None, Some(LocalDate.now().plusYears(1)))
        FoodItem.create(2, "Apples", "Produce (Fruits)", 50, None, Some(LocalDate.now().plusDays(7)))
        FoodItem.create(2, "Milk", "Dairy", 30, None, Some(LocalDate.now().plusDays(10)))

        println("Database seeding complete.")
      } else {
        println("Database schema already exists.")
      }
    } catch {
      case e: Exception => println(s"Database setup failed: ${e.getMessage}")
    }
  }
}