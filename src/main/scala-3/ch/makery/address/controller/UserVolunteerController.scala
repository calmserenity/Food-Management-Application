package ch.makery.address.controller

import ch.makery.address.MainApp
import ch.makery.address.model.{FoodBank, VolunteerSession}
import javafx.fxml.FXML
import javafx.scene.control._
import javafx.util.StringConverter
import java.time.{LocalDate, LocalTime}
import java.time.format.DateTimeFormatter 

class UserVolunteerController {
  
  // FXML UI element injections
  @FXML private var foodBankComboBox: ComboBox[FoodBank] = _
  @FXML private var sessionDatePicker: DatePicker = _
  @FXML private var startTimeComboBox: ComboBox[String] = _
  @FXML private var endTimeComboBox: ComboBox[String] = _
  @FXML private var activityField: TextField = _
  @FXML private var notesArea: TextArea = _
  @FXML private var fullNameField: TextField = _
  @FXML private var phoneNumberField: TextField = _
  @FXML private var ageField: TextField = _
  @FXML private var genderComboBox: ComboBox[String] = _

  // Holds a pre-selected food bank if one is passed from another view
  private var preselectedFoodBank: Option[FoodBank] = None

  // Initializes the controller with a pre-selected food bank
  def initData(foodBank: FoodBank): Unit = {
    this.preselectedFoodBank = Some(foodBank)
  }

  @FXML
  def initialize(): Unit = {
    // Populate and configure the food bank selection ComboBox
    val foodBanks = FoodBank.findAll()
    foodBankComboBox.getItems.addAll(foodBanks: _*)
    foodBankComboBox.setConverter(new StringConverter[FoodBank] {
      override def toString(fb: FoodBank): String = if (fb != null) fb.name else ""
      override def fromString(string: String): FoodBank = null
    })
    preselectedFoodBank.foreach(fb => foodBankComboBox.setValue(fb))
    // Pre-fill the full name field with the current user's name
    MainApp.currentUser.foreach { user =>
      fullNameField.setText(s"${user.firstName} ${user.lastName}")
    }
    
    // Populate the time selection ComboBoxes
    val times = (8 to 17).flatMap { h =>
      val hour = f"$h%02d"
      Seq(s"$hour:00", s"$hour:30")
    }
    startTimeComboBox.getItems.addAll(times: _*)
    endTimeComboBox.getItems.addAll(times: _*)
  }

  // Handles the submission of the volunteer sign-up form
  @FXML
  private def handleSubmit(): Unit = {
    val selectedFoodBank = Option(foodBankComboBox.getValue)
    val fullName = Option(fullNameField.getText).filter(_.nonEmpty)
    val phoneNumber = Option(phoneNumberField.getText).filter(_.nonEmpty)
    val ageOpt = ageField.getText.toIntOption
    val gender = Option(genderComboBox.getValue).filterNot(_ == "Prefer not to say")
    val selectedDate = Option(sessionDatePicker.getValue)
    val startTimeStr = Option(startTimeComboBox.getValue)
    val endTimeStr = Option(endTimeComboBox.getValue)
    val activity = Option(activityField.getText).filter(_.nonEmpty)
    val notes = Option(notesArea.getText).filter(_.nonEmpty)

    // Validate required fields
    if (selectedFoodBank.isEmpty || fullName.isEmpty || phoneNumber.isEmpty || ageOpt.isEmpty || selectedDate.isEmpty || startTimeStr.isEmpty || endTimeStr.isEmpty) {
      showError("Validation Error", "Please fill in all required fields: Food Bank, Name, Phone, Age, Date, and Time.")
      return
    }

    val age = ageOpt.get
    if (age <= 0) {
      showError("Validation Error", "Please enter a valid age.")
      return
    }

    val startTime = LocalTime.parse(startTimeStr.get)
    val endTime = LocalTime.parse(endTimeStr.get)

    if (startTime.isAfter(endTime) || startTime == endTime) {
      showError("Validation Error", "End time must be after start time.")
      return
    }

    // Create the volunteer session record in the database
    (MainApp.currentUser, selectedFoodBank) match {
      case (Some(user), Some(foodBank)) =>
        try {
          VolunteerSession.create(
            userUsername = user.username,
            foodBankId = foodBank.id.get,
            fullName = fullName.get,
            phoneNumber = phoneNumber.get,
            age = age,
            gender = gender,
            sessionDate = selectedDate.get,
            startTime = startTime,
            endTime = endTime,
            activity = activity,
            notes = notes
          )
          showInfo("Success", "You have successfully signed up to volunteer. Thank you!")
          MainApp.navigateBack()
        } catch {
          case e: Exception => showError("Database Error", s"Could not save your session. Error: ${e.getMessage}")
        }
      case _ => showError("Session Error", "Could not process sign-up. User or Food Bank not found.")
    }
  }

  // Handles the click event for the cancel button
  @FXML
  private def handleCancel(): Unit = {
    MainApp.navigateBack()
  }

  // Helper method to display an error alert
  private def showError(title: String, message: String): Unit = {
    val alert = new Alert(Alert.AlertType.ERROR)
    alert.initOwner(MainApp.stage)
    alert.setTitle(title)
    alert.setHeaderText(null)
    alert.setContentText(message)
    alert.showAndWait()
  }

  // Helper method to display an informational alert
  private def showInfo(title: String, message: String): Unit = {
    val alert = new Alert(Alert.AlertType.INFORMATION)
    alert.initOwner(MainApp.stage)
    alert.setTitle(title)
    alert.setHeaderText(null)
    alert.setContentText(message)
    alert.showAndWait()
  }
}