package ch.makery.address.model

import scalikejdbc._
import java.security.MessageDigest

// Represents a single user account
case class User(username: String, passwordHash: String, role: String, imagePath: Option[String], firstName: String, lastName: String)

object User {

  // Hashes a password using SHA-256 for secure storage
  private def hashPassword(password: String): String = {
    val digest = MessageDigest.getInstance("SHA-256")
    digest.digest(password.getBytes("UTF-8")).map("%02x".format(_)).mkString
  }

  // Updates an existing user's information
  def update(username: String, firstName: String, lastName: String, password: Option[String]): Int = {
    val passwordSetFragment = password.map(p => sqls", password_hash = ${hashPassword(p)}").getOrElse(sqls"")

    DB.autoCommit { implicit session =>
      sql"""
        UPDATE users
        SET
          first_name = ${firstName},
          last_name = ${lastName}
          $passwordSetFragment
        WHERE username = ${username}
      """.update.apply()
    }
  }


  // Creates a new user record in the database with a specific role
  def create(username: String, password: String, role: String, imagePath: Option[String], firstName: String, lastName: String): Int = {
    val hashedPassword = hashPassword(password)

    DB.autoCommit { implicit session =>
      sql"""
        INSERT INTO users (username, password_hash, role, image_path, first_name, last_name)
        VALUES (${username}, ${hashedPassword}, ${role}, ${imagePath}, ${firstName}, ${lastName})
      """.update.apply()
    }
  }

  // Creates a new user record with a default role of "Recipient"
  def create(username: String, password: String, firstName: String, lastName: String): Int = {
    create(
      username = username,
      password = password,
      role = "Recipient", 
      imagePath = Some("/image/defaulticon.png"), 
      firstName = firstName,
      lastName = lastName
    )
  }

  // Finds a single user by their username
  def findByUsername(username: String): Option[User] = {
    DB.readOnly { implicit session =>
      sql"SELECT username, password_hash, role, image_path, first_name, last_name FROM users WHERE username = ${username}"
        .map(rs => User(
          username = rs.string("username"),
          passwordHash = rs.string("password_hash"),
          role = rs.string("role"),
          imagePath = rs.stringOpt("image_path"),
          firstName = rs.string("first_name"),
          lastName= rs.string("last_name")
        ))
        .single
        .apply()
    }
  }
}