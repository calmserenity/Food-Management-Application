package ch.makery.address.controller

import ch.makery.address.MainApp
import ch.makery.address.model.FoodBank
import javafx.fxml.FXML
import javafx.scene.control.{Alert, Button, Label, TextArea, TextField}
import javafx.scene.control.Alert.AlertType

class AdminEditFoodBankController {

  // FXML UI element injections
  @FXML private var nameField: TextField = _
  @FXML private var locationField: TextField = _
  @FXML private var picField: TextField = _
  @FXML private var yearFoundedField: TextField = _
  @FXML private var phoneField: TextField = _
  @FXML private var emailField: TextField = _
  @FXML private var hoursField: TextField = _
  @FXML private var needsArea: TextArea = _
  @FXML private var formTitleLabel: Label = _

  // Holds the food bank being edited, if any
  private var currentFoodBank: Option[FoodBank] = None

  // Initializes the controller, populating the form if a food bank is provided
  def initData(foodBank: Option[FoodBank]): Unit = {
    foodBank match {
      case Some(fb) =>
        // Edit mode: Populate fields with existing data
        currentFoodBank = Some(fb)
        formTitleLabel.setText("Edit Food Bank Information")
        nameField.setText(fb.name)
        locationField.setText(fb.location)
        yearFoundedField.setText(fb.yearFounded.toString)
        phoneField.setText(fb.phoneNumber.getOrElse(""))
        emailField.setText(fb.email.getOrElse(""))
        hoursField.setText(fb.operatingHours.getOrElse(""))
        needsArea.setText(fb.currentNeeds.getOrElse(""))

      case None =>
        // Create mode: Leave fields blank for new entry
        currentFoodBank = None
        formTitleLabel.setText("Add New Food Bank")
    }
  }

  // Handles the save button click
  @FXML
  private def handleSave(): Unit = {
    if (isInputValid()) {
      currentFoodBank match {
        case Some(fb) => // Update existing food bank
          val updatedFoodBank = fb.copy(
            name = nameField.getText,
            location = locationField.getText,
            personInCharge = picField.getText,
            yearFounded = yearFoundedField.getText.toInt,
            phoneNumber = Some(phoneField.getText).filter(_.nonEmpty),
            email = Some(emailField.getText).filter(_.nonEmpty),
            operatingHours = Some(hoursField.getText).filter(_.nonEmpty),
            currentNeeds = Some(needsArea.getText).filter(_.nonEmpty)
          )
          FoodBank.update(updatedFoodBank)
          showAlert(AlertType.INFORMATION, "Success", "Food bank information has been updated.")
          MainApp.showFoodBankDetailView(updatedFoodBank)

        case None => // Create new food bank
          FoodBank.create(
            name = nameField.getText,
            location = locationField.getText,
            personInCharge = picField.getText,
            yearFounded = yearFoundedField.getText.toInt,
            phoneNumber = Some(phoneField.getText).filter(_.nonEmpty),
            email = Some(emailField.getText).filter(_.nonEmpty),
            operatingHours = Some(hoursField.getText).filter(_.nonEmpty),
            currentNeeds = Some(needsArea.getText).filter(_.nonEmpty)
          )
          showAlert(AlertType.INFORMATION, "Success", s"${nameField.getText} has been added.")
          MainApp.showFoodBankListView()
      }
    }
  }

  // Handles the cancel button click
  @FXML
  private def handleCancel(): Unit = {
    MainApp.navigateBack()
  }

  // Validates the user input in the form fields
  private def isInputValid(): Boolean = {
    var errorMessage = ""
    if (nameField.getText.isEmpty) errorMessage += "Food Bank Name is required.\n"
    if (locationField.getText.isEmpty) errorMessage += "Location is required.\n"
    if (yearFoundedField.getText.matches("\\d+") == false) errorMessage += "Year Founded must be a valid number.\n"

    if (errorMessage.isEmpty) {
      true
    } else {
      showAlert(AlertType.WARNING, "Invalid Fields", errorMessage)
      false
    }
  }

  // Helper method to display an alert dialog
  private def showAlert(alertType: AlertType, title: String, message: String): Unit = {
    val alert = new Alert(alertType)
    alert.setTitle(title)
    alert.setHeaderText(null)
    alert.setContentText(message)
    alert.showAndWait()
  }
}