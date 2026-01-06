package ch.makery.address.controller

import ch.makery.address.model.User
import javafx.fxml.FXML
import javafx.scene.control.{Button, Label, PasswordField, TextField, Alert}
import javafx.scene.control.Alert.AlertType

class NewAdminController {

  // FXML UI element injections
  @FXML private var userIdField: TextField = _
  @FXML private var passwordField: PasswordField = _
  @FXML private var validatePasswordField: PasswordField = _
  @FXML private var firstNameField: TextField = _
  @FXML private var lastNameField: TextField = _
  @FXML private var submitButton: Button = _

  // Handles the submission of the new admin form
  @FXML
  private def handleSubmit(): Unit = {
    val firstName = firstNameField.getText
    val lastName = lastNameField.getText
    val username = userIdField.getText
    val password = passwordField.getText
    val validatePassword = validatePasswordField.getText

    // Validate that all fields are filled
    if (username.isEmpty || password.isEmpty || firstName.isEmpty || lastName.isEmpty) {
      showAlert("Validation Error", "All fields are required.")
      return
    }

    // Validate that the passwords match
    if (password != validatePassword) {
      showAlert("Validation Error", "Passwords do not match.")
      return
    }

    // Check if the username is already taken
    if (User.findByUsername(username).isDefined) {
      showAlert("Creation Failed", s"An account with the username '$username' already exists.")
    } else {
      // Create the new admin user
      User.create(
        username = username,
        password = password,
        role = "admin",
        imagePath = Some("/image/defaulticon.png"),
        firstName = firstName,
        lastName = lastName
      )
      showAlert("Success", s"Admin account '$username' created successfully.")
      clearFields()
    }
  }

  // Helper method to display an alert dialog
  private def showAlert(title: String, message: String): Unit = {
    val alert = new Alert(AlertType.INFORMATION)
    alert.setTitle(title)
    alert.setHeaderText(null)
    alert.setContentText(message)
    alert.showAndWait()
  }

  // Clears all input fields in the form
  private def clearFields(): Unit = {
    firstNameField.clear()
    lastNameField.clear()
    userIdField.clear()
    passwordField.clear()
    validatePasswordField.clear()
  }
}