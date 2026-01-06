package ch.makery.address.controller

import ch.makery.address.MainApp
import ch.makery.address.model._
import javafx.beans.property.SimpleIntegerProperty
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control._
import javafx.scene.control.cell.{PropertyValueFactory, TextFieldTableCell}
import javafx.util.StringConverter
import javafx.util.converter.IntegerStringConverter

import scala.beans.BeanProperty
import scala.jdk.CollectionConverters._ 


// Wrapper class for TableView items to allow for an editable requested quantity
class RequestableItem(@BeanProperty val name: String, val maxQuantity: Int) {
  val requestedQuantity = new SimpleIntegerProperty(0)
  def getRequestedQuantity: Int = requestedQuantity.get()
  def setRequestedQuantity(value: Int): Unit = {
    if (value >= 0 && value <= maxQuantity) {
      requestedQuantity.set(value)
    }
  }
  def requestedQuantityProperty() = requestedQuantity
}

class UserRequestDonationController {
  // FXML UI element injections
  @FXML private var foodBankComboBox: ComboBox[FoodBank] = _
  @FXML private var foodItemTableView: TableView[RequestableItem] = _
  @FXML private var itemNameColumn: TableColumn[RequestableItem, String] = _
  @FXML private var quantityColumn: TableColumn[RequestableItem, java.lang.Integer] = _
  @FXML private var fullNameField: TextField = _
  @FXML private var ageField: TextField = _
  @FXML private var genderComboBox: ComboBox[String] = _
  @FXML private var deliveryToggleGroup: ToggleGroup = _
  @FXML private var pickUpRadio: RadioButton = _
  @FXML private var deliveryRadio: RadioButton = _
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
    foodBankComboBox.setConverter(new StringConverter[FoodBank] {
      override def toString(fb: FoodBank): String = if (fb != null) fb.name else ""
      override def fromString(string: String): FoodBank = null
    })
    preselectedFoodBank.foreach(fb => foodBankComboBox.setValue(fb))

    // Add a listener to populate the food item table when a food bank is selected
    foodBankComboBox.valueProperty().addListener((_, _, newValue) => {
      if (newValue != null) {
        populateFoodItemTable(newValue.id.get)
        foodItemTableView.setDisable(false)
      } else {
        foodItemTableView.getItems.clear()
        foodItemTableView.setDisable(true)
      }
    })

    // Pre-fill the full name field with the current user's name
    MainApp.currentUser.foreach(user => fullNameField.setText(s"${user.firstName} ${user.lastName}"))

    itemNameColumn.setCellValueFactory(new PropertyValueFactory[RequestableItem, String]("name"))

    // Set up the editable quantity column
    quantityColumn.setCellValueFactory(cellData => cellData.getValue.requestedQuantityProperty().asObject())
    quantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter().asInstanceOf[StringConverter[java.lang.Integer]]))
    quantityColumn.setOnEditCommit(event => {
      val item = event.getRowValue
      if (event.getNewValue != null) {
        item.setRequestedQuantity(event.getNewValue)
      }
    })
    foodItemTableView.setEditable(true)

    pickUpRadio.setSelected(true)

    // If a food bank was pre-selected, populate the table immediately
    if (preselectedFoodBank.isDefined) {
      populateFoodItemTable(preselectedFoodBank.get.id.get)
      foodItemTableView.setDisable(false)
    }
  }

  // Populates the food item table with items from the selected food bank
  private def populateFoodItemTable(foodBankId: Long): Unit = {
    val availableItems = FoodItem.findByFoodBank(foodBankId)
    val requestableItems = availableItems.map(item => new RequestableItem(item.name, item.quantity))
    foodItemTableView.setItems(FXCollections.observableArrayList(requestableItems: _*))
  }

  // Handles the submission of the donation request form
  @FXML
  private def handleSubmit(): Unit = {
    val selectedFoodBank = Option(foodBankComboBox.getValue)
    val fullName = Option(fullNameField.getText).filter(_.nonEmpty)
    val ageOpt = ageField.getText.toIntOption
    val gender = Option(genderComboBox.getValue).filterNot(_ == "Prefer not to say")
    val deliveryMethod = deliveryToggleGroup.getSelectedToggle.asInstanceOf[RadioButton].getText
    val notes = Option(notesArea.getText).filter(_.nonEmpty)

    // Filter and map the requested items from the table
    val requestedItems = foodItemTableView.getItems.filtered(_.getRequestedQuantity > 0)
      .asScala 
      .map(item => RequestedItem(item.name, item.getRequestedQuantity))
      .toList 

    // --- Validation ---

    // 1. Check for empty required fields
    if (selectedFoodBank.isEmpty || fullName.isEmpty || ageOpt.isEmpty) {
      showError("Validation Error", "Please fill in Food Bank, Full Name, and Age.")
      return
    }

    // 2. Check if any items were requested
    if (requestedItems.isEmpty) {
      showError("Validation Error", "Please request a quantity of at least one food item.")
      return
    }

    // 3. Re-check inventory at the time of submission
    val currentInventory = FoodItem.findByFoodBank(selectedFoodBank.get.id.get).map(item => item.name -> item.quantity).toMap
    val invalidRequests = requestedItems.filter(req => req.quantity > currentInventory.getOrElse(req.name, 0))

    if (invalidRequests.nonEmpty) {
      val errorDetails = invalidRequests.map(ir => s"- ${ir.name} (Requested: ${ir.quantity}, Available: ${currentInventory.getOrElse(ir.name, 0)})").mkString("\n")
      showError("Inventory Check Failed", s"The quantity for some items has changed since you loaded this page. Please adjust your request:\n\n$errorDetails")
      // Refresh the table to show the user the most up-to-date quantities
      populateFoodItemTable(selectedFoodBank.get.id.get)
      return
    }


    // --- Create Request ---
    (MainApp.currentUser, selectedFoodBank) match {
      case (Some(user), Some(foodBank)) =>
        try {
          DonationRequest.create(
            userUsername = user.username,
            foodBankId = foodBank.id.get,
            fullName = fullName.get,
            age = ageOpt.get,
            gender = gender,
            requestedItems = requestedItems,
            deliveryMethod = deliveryMethod,
            notes = notes
          )
          showInfo("Request Submitted", "Your donation request has been submitted and is now pending approval. You will be contacted shortly.")
          MainApp.navigateBack()
        } catch {
          case e: Exception => showError("Database Error", s"Could not save your request. Error: ${e.getMessage}")
        }
      case _ => showError("Session Error", "Could not process request. User or Food Bank not found.")
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