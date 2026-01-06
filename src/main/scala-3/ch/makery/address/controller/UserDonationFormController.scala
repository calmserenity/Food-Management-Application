package ch.makery.address.controller

import ch.makery.address.MainApp
import ch.makery.address.model.{Donation, FoodBank}
import javafx.fxml.FXML
import javafx.scene.control._
import javafx.scene.layout.VBox
import javafx.util.StringConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class UserDonationFormController {
  
  // FXML UI element injections
  @FXML private var foodBankComboBox: ComboBox[FoodBank] = _
  @FXML private var itemNameField: TextField = _
  @FXML private var categoryComboBox: ComboBox[String] = _
  @FXML private var quantityField: TextField = _
  @FXML private var conditionalFieldsBox: VBox = _
  @FXML private var expiryDateBox: VBox = _
  @FXML private var expiryDatePicker: DatePicker = _
  @FXML private var shelfLifeBox: VBox = _
  @FXML private var shelfLifeField: TextField = _
  @FXML private var deliveryToggleGroup: ToggleGroup = _
  @FXML private var dropOffRadio: RadioButton = _
  @FXML private var pickUpRadio: RadioButton = _
  @FXML private var notesArea: TextArea = _

  // Holds a pre-selected food bank if one is passed from another view
  private var preselectedFoodBank: Option[FoodBank] = None

  // Initializes the controller with a pre-selected food bank
  def initData(foodBank: FoodBank): Unit = {
    this.preselectedFoodBank = Some(foodBank)
  }

  @FXML
  def initialize(): Unit = {
    // Populate the food bank selection ComboBox
    val foodBanks = FoodBank.findAll()
    foodBankComboBox.getItems.addAll(foodBanks: _*)

    // Define how FoodBank objects are displayed as strings in the ComboBox
    foodBankComboBox.setConverter(new StringConverter[FoodBank] {
      override def toString(fb: FoodBank): String = if (fb != null) fb.name else ""
      override def fromString(string: String): FoodBank = null 
    })

    // Pre-select a food bank if one was passed in
    preselectedFoodBank.foreach { fb =>
      foodBankComboBox.setValue(fb)
    }

    // Add a listener to show/hide conditional fields based on the selected category
    categoryComboBox.valueProperty().addListener((_, _, newValue) => {
      handleCategoryChange(newValue)
    })

    // Set the default delivery method
    dropOffRadio.setSelected(true)
  }

  // Shows or hides conditional form fields based on the selected food category
  private def handleCategoryChange(category: String): Unit = {
    conditionalFieldsBox.setVisible(false)
    conditionalFieldsBox.setManaged(false)
    expiryDateBox.setVisible(false)
    expiryDateBox.setManaged(false)
    shelfLifeBox.setVisible(false)
    shelfLifeBox.setManaged(false)

    category match {
      case "Canned Food" =>
        // Show the expiry date field for canned food
        expiryDateBox.setVisible(true)
        expiryDateBox.setManaged(true)
        conditionalFieldsBox.setVisible(true)
        conditionalFieldsBox.setManaged(true)

      case null | "Other" =>
      // No conditional fields for "Other" or no selection

      case _ => 
        // Show the shelf life field for all other categories
        shelfLifeBox.setVisible(true)
        shelfLifeBox.setManaged(true)
        conditionalFieldsBox.setVisible(true)
        conditionalFieldsBox.setManaged(true)
    }
  }

  // Handles the submission of the donation form
  @FXML
  private def handleSubmit(): Unit = {
    val selectedFoodBank = Option(foodBankComboBox.getValue)
    val itemName = Option(itemNameField.getText).filter(_.nonEmpty)
    val selectedCategory = Option(categoryComboBox.getValue)
    val quantityOpt = quantityField.getText.toIntOption
    val deliveryMethod = deliveryToggleGroup.getSelectedToggle.asInstanceOf[RadioButton].getText
    val expiryDate = Option(expiryDatePicker.getValue)
    val shelfLifeOpt = shelfLifeField.getText.toIntOption
    val notes = Option(notesArea.getText).filter(_.nonEmpty)

    // Validate required fields
    if (selectedFoodBank.isEmpty || itemName.isEmpty || selectedCategory.isEmpty || quantityOpt.isEmpty) {
      showError("Validation Error", "Please fill in Food Bank, Item Name, Category, and Quantity.")
      return
    }

    // Validate category-specific fields
    selectedCategory.get match {
      case "Canned Food" | "Dairy" | "Dry Goods" if expiryDate.isEmpty =>
        showError("Validation Error", "Expiry Date is required for this category.")
        return
      case "Produce (Vegetables)" | "Produce (Fruits)" | "Meat" if shelfLifeOpt.isEmpty =>
        showError("Validation Error", "Shelf Life is required for this category.")
        return
      case _ => 
    }

    // Create the donation record in the database
    (MainApp.currentUser, selectedFoodBank) match {
      case (Some(user), Some(foodBank)) =>
        try {
          Donation.create(
            userUsername = user.username,
            foodBankId = foodBank.id.get,
            itemName = itemName.get,
            category = selectedCategory.get,
            quantity = quantityOpt.get,
            deliveryMethod = deliveryMethod,
            expiryDate = expiryDate,
            shelfLifeDays = shelfLifeOpt,
            notes = notes
          )
          showInfo("Success", "Your donation has been pledged successfully. Thank you!")
          MainApp.navigateBack()
        } catch {
          case e: Exception => showError("Database Error", s"Could not save the donation. Error: ${e.getMessage}")
        }
      case _ => showError("Session Error", "Could not process donation. User or Food Bank not found.")
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