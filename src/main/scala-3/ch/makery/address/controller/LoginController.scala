package ch.makery.address.controller

import javafx.fxml.FXML
import javafx.scene.control.{Button, Hyperlink, PasswordField, TextField}
import scalafx.scene.control.Alert
import scalafx.Includes._
import scalafx.event.ActionEvent
import ch.makery.address.MainApp
import ch.makery.address.model.User
import java.security.MessageDigest

class LoginController {

  // FXML UI element injections
  @FXML private var userIdField: TextField = _
  @FXML private var passwordField: PasswordField = _
  @FXML private var loginButton: Button = _
  @FXML private var registerLink : Hyperlink = _

  // Initializes the controller after FXML loading
  @FXML
  def initialize(): Unit = {
    loginButton.onAction = (_: ActionEvent) => handleLogin()
    registerLink.onAction = (_: ActionEvent) => MainApp.showRegisterView()
  }

  // Handles the login button click event
  private def handleLogin(): Unit = {
    val username = userIdField.getText()
    val password = passwordField.getText()

    // Validate user input
    if (username.isEmpty || password.isEmpty) {
      showAlert(Alert.AlertType.Warning, "Validation Error", "Please enter both User ID and Password.")
      return
    }

    // Authenticate the user against the database
    User.findByUsername(username) match {
      case Some(user) =>
        val enteredPasswordHash = hashPassword(password)

        if (user.passwordHash == enteredPasswordHash) {
          println(s"Login successful for user: ${user.username}, Role: ${user.role}")
          if (user.role.equalsIgnoreCase("admin") || user.role.equalsIgnoreCase("superadmin")) {
            // Navigate to the admin dashboard
            MainApp.showAdminMainView(user)
          } else {
            // Navigate to the standard user view
            MainApp.showMainView(user)
          }
        } else {
          showAlert(Alert.AlertType.Error, "Login Failed", "Invalid password. Please try again.")
        }

      case None =>
        showAlert(Alert.AlertType.Error, "Login Failed", "No user found with that User ID.")
    }
  }

  // Hashes a password using SHA-256 for secure storage and comparison
  private def hashPassword(password: String): String = {
    val digest = MessageDigest.getInstance("SHA-256")
    digest.digest(password.getBytes("UTF-8")).map("%02x".format(_)).mkString
  }

  // Helper method to display an alert dialog
  private def showAlert(alertType: Alert.AlertType, title: String, message: String): Unit = {
    val alert = new Alert(alertType)
    alert.initOwner(loginButton.getScene.getWindow)
    alert.title = title
    alert.headerText = None
    alert.contentText = message
    alert.showAndWait()
  }
}