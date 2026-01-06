package ch.makery.address.controller

import ch.makery.address.model.User
import javafx.fxml.FXML
import javafx.scene.control._
import javafx.scene.layout.GridPane
import javafx.geometry.Insets

class ProfileViewController {

  // FXML UI element injections
  @FXML private var firstNameLabel: Label = _
  @FXML private var lastNameLabel: Label = _
  @FXML private var roleLabel: Label = _
  @FXML private var editButton: Button = _

  private var currentUser: Option[User] = None

  // Initializes the controller with the current user's data
  def initData(user: User): Unit = {
    currentUser = Some(user)
    updateView()
  }

  private def updateView(): Unit = {
    currentUser.foreach { user =>
      firstNameLabel.setText(user.firstName)
      lastNameLabel.setText(user.lastName)
      roleLabel.setText(user.role.capitalize)
    }
  }

  // Handles the click event for the edit button
  @FXML
  private def handleEdit(): Unit = {
    currentUser.foreach(showEditDialog)
  }

  private def showEditDialog(user: User): Unit = {
    val dialog = new Dialog[User]()
    dialog.setTitle("Edit User Information")
    dialog.setHeaderText("Please update your information.")

    val saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE)
    dialog.getDialogPane.getButtonTypes.addAll(saveButtonType, ButtonType.CANCEL)

    val grid = new GridPane()
    grid.setHgap(10)
    grid.setVgap(10)
    grid.setPadding(new Insets(20, 150, 10, 10))

    val firstNameField = new TextField()
    firstNameField.setText(user.firstName)
    val lastNameField = new TextField()
    lastNameField.setText(user.lastName)
    val passwordField = new PasswordField()
    passwordField.setPromptText("New Password (optional)")
    val confirmPasswordField = new PasswordField()
    confirmPasswordField.setPromptText("Confirm New Password")


    grid.add(new Label("First Name:"), 0, 0)
    grid.add(firstNameField, 1, 0)
    grid.add(new Label("Last Name:"), 0, 1)
    grid.add(lastNameField, 1, 1)
    grid.add(new Label("New Password:"), 0, 2)
    grid.add(passwordField, 1, 2)
    grid.add(new Label("Confirm Password:"), 0, 3)
    grid.add(confirmPasswordField, 1, 3)

    dialog.getDialogPane.setContent(grid)

    dialog.setResultConverter { dialogButton =>
      if (dialogButton == saveButtonType) {
        val newPassword = Option(passwordField.getText).filter(_.nonEmpty)
        val confirmPassword = Option(confirmPasswordField.getText).filter(_.nonEmpty)

        if (newPassword != confirmPassword) {
          showAlert("Validation Error", "Passwords do not match.")
          null // Stay in dialog
        } else {
          User.update(
            user.username,
            firstNameField.getText,
            lastNameField.getText,
            newPassword
          )
          // Create a new user instance to reflect the update
          user.copy(
            firstName = firstNameField.getText,
            lastName = lastNameField.getText
          )
        }
      } else {
        null
      }
    }

    dialog.showAndWait().ifPresent { updatedUser =>
      currentUser = Some(updatedUser)
      updateView()
    }
  }

  private def showAlert(title: String, message: String): Unit = {
    val alert = new Alert(Alert.AlertType.INFORMATION)
    alert.setTitle(title)
    alert.setHeaderText(null)
    alert.setContentText(message)
    alert.showAndWait()
  }
}