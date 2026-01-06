package ch.makery.address.controller

import ch.makery.address.MainApp
import ch.makery.address.model.User
import javafx.fxml.FXML
import javafx.scene.control.{Alert, PasswordField, TextField}
import javafx.scene.control.Alert.AlertType

class RegisterController {

  // FXML UI element injections
  @FXML private var firstNameField: TextField = _
  @FXML private var lastNameField: TextField = _
  @FXML private var userIdField: TextField = _
  @FXML private var passwordField: PasswordField = _
  @FXML private var validatePasswordField: PasswordField = _
  
  // Handles the submission of the registration form
  @FXML
  private def handleSubmit(): Unit = {
    val firstName = firstNameField.getText
    val lastName = lastNameField.getText
    val username = userIdField.getText
    val password = passwordField.getText
    val validatePassword = validatePasswordField.getText

    // Validate that all fields are filled
    if (firstName.isEmpty || lastName.isEmpty || username.isEmpty || password.isEmpty) {
      showError("Validation Error", "All fields are required.")
      return
    }

    // Validate that the passwords match
    if (password != validatePassword) {
      showError("Validation Error", "Passwords do not match.")
      return
    }

    // Check if the username is already taken
    if (User.findByUsername(username).isDefined) {
      showError("Registration Error", s"The username '$username' is already taken. Please choose another.")
      return
    }

    // Create the new user account
    try {
      User.create(
        username = username,
        password = password,
        firstName = firstName,
        lastName = lastName
      )

      showInfo("Success", "Account created successfully! You can now log in.")
      MainApp.showLoginView()

    } catch {
      case e: Exception =>
        showError("Database Error", s"Could not create account. Error: ${e.getMessage}")
    }
  }
  
  // Handles the click event for the cancel button
  @FXML
  private def handleCancel(): Unit = {
    MainApp.showLoginView()
  }

  // Helper method to display an error alert
  private def showError(title: String, message: String): Unit = {
    val alert = new Alert(AlertType.ERROR)
    alert.initOwner(MainApp.stage)
    alert.setTitle(title)
    alert.setHeaderText(null)
    alert.setContentText(message)
    alert.showAndWait()
  }

  // Helper method to display an informational alert
  private def showInfo(title: String, message: String): Unit = {
    val alert = new Alert(AlertType.INFORMATION)
    alert.initOwner(MainApp.stage)
    alert.setTitle(title)
    alert.setHeaderText(null)
    alert.setContentText(message)
    alert.showAndWait()
  }
}