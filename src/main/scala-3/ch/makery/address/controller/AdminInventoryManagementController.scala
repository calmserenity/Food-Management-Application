package ch.makery.address.controller

import ch.makery.address.MainApp
import ch.makery.address.model.{FoodBank, FoodItem}
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control._
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.{GridPane, Priority}
import javafx.util.StringConverter
import java.time.LocalDate
import javafx.geometry.Insets

class AdminInventoryManagementController {

  // FXML UI element injections
  @FXML private var titleLabel: Label = _
  @FXML private var foodBankComboBox: ComboBox[FoodBank] = _
  @FXML private var inventoryTableView: TableView[FoodItem] = _
  @FXML private var itemNameColumn: TableColumn[FoodItem, String] = _
  @FXML private var categoryColumn: TableColumn[FoodItem, String] = _
  @FXML private var quantityColumn: TableColumn[FoodItem, Int] = _
  @FXML private var expiryDateColumn: TableColumn[FoodItem, LocalDate] = _

  // Holds the currently selected food bank
  private var currentFoodBank: Option[FoodBank] = None

  // Initializes the controller with data from another view
  def initData(foodBank: FoodBank): Unit = {
    this.currentFoodBank = Some(foodBank)
  }

  @FXML
  def initialize(): Unit = {
    inventoryTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS)
    inventoryTableView.setEditable(false)

    // Map model properties to table columns
    itemNameColumn.setCellValueFactory(new PropertyValueFactory[FoodItem, String]("name"))
    categoryColumn.setCellValueFactory(new PropertyValueFactory[FoodItem, String]("category"))
    quantityColumn.setCellValueFactory(new PropertyValueFactory[FoodItem, Int]("quantity"))
    expiryDateColumn.setCellValueFactory(new PropertyValueFactory[FoodItem, LocalDate]("expiryDate"))

    // Populate and configure the food bank selection ComboBox
    val foodBanks = FoodBank.findAll()
    foodBankComboBox.getItems.addAll(foodBanks: _*)
    foodBankComboBox.setConverter(new StringConverter[FoodBank] {
      override def toString(fb: FoodBank): String = if (fb != null) fb.name else ""
      override def fromString(string: String): FoodBank = null
    })

    // Pre-select a food bank if one was passed in and populate the table
    currentFoodBank.foreach(fb => foodBankComboBox.setValue(fb))
    populateInventoryTable()

    // Add a listener to repopulate the table when a new food bank is selected
    foodBankComboBox.valueProperty().addListener((_, _, newValue) => {
      currentFoodBank = Option(newValue)
      populateInventoryTable()
    })
  }

  // Populates the inventory table based on the currently selected food bank
  private def populateInventoryTable(): Unit = {
    currentFoodBank match {
      case Some(fb) =>
        titleLabel.setText(s"Inventory for ${fb.name}")
        val items = FoodItem.findByFoodBank(fb.id.get)
        inventoryTableView.setItems(FXCollections.observableArrayList(items: _*))
      case None =>
        titleLabel.setText("Inventory Management")
        inventoryTableView.getItems.clear()
    }
  }

  // Handles the click event for the "Add Item" button
  @FXML
  private def handleAddItem(): Unit = {
    currentFoodBank match {
      case None =>
        // Show a warning if no food bank is selected
        val alert = new Alert(Alert.AlertType.WARNING)
        alert.initOwner(MainApp.stage)
        alert.setTitle("No Food Bank Selected")
        alert.setHeaderText(null)
        alert.setContentText("Please select a food bank from the dropdown menu first.")
        alert.showAndWait()
      case Some(foodBank) =>
        showAddItemDialog(foodBank)
    }
  }

  // Displays a dialog for adding a new item to the inventory
  private def showAddItemDialog(foodBank: FoodBank): Unit = {
    val dialog = new Dialog[FoodItem]()
    dialog.setTitle("Add New Item to Inventory")
    dialog.setHeaderText(s"Adding item to ${foodBank.name}")
    dialog.initOwner(MainApp.stage)

    val addButtonType = new ButtonType("Add Item", ButtonBar.ButtonData.OK_DONE)
    dialog.getDialogPane.getButtonTypes.addAll(addButtonType, ButtonType.CANCEL)

    val grid = new GridPane()
    grid.setHgap(10)
    grid.setVgap(10)
    grid.setPadding(new Insets(20, 150, 10, 10))

    val nameField = new TextField()
    nameField.setPromptText("Item Name")
    val categoryComboBox = new ComboBox[String]()
    categoryComboBox.getItems.addAll("Canned Food", "Produce (Vegetables)", "Produce (Fruits)", "Meat", "Dairy", "Dry Goods", "Other")
    categoryComboBox.setPromptText("Category")
    val quantityField = new TextField()
    quantityField.setPromptText("Quantity")
    val expiryDatePicker = new DatePicker()
    expiryDatePicker.setPromptText("Expiry Date (Optional)")

    grid.add(new Label("Name:"), 0, 0)
    grid.add(nameField, 1, 0)
    grid.add(new Label("Category:"), 0, 1)
    grid.add(categoryComboBox, 1, 1)
    grid.add(new Label("Quantity:"), 0, 2)
    grid.add(quantityField, 1, 2)
    grid.add(new Label("Expiry Date:"), 0, 3)
    grid.add(expiryDatePicker, 1, 3)

    dialog.getDialogPane.setContent(grid)

    // Convert the dialog result to a FoodItem when the "Add Item" button is clicked
    dialog.setResultConverter { dialogButton =>
      if (dialogButton == addButtonType) {
        val quantity = quantityField.getText.toIntOption.getOrElse(0)
        if (nameField.getText.nonEmpty && categoryComboBox.getValue != null && quantity > 0) {
          FoodItem.findByNameAndFoodBank(nameField.getText, foodBank.id.get) match {
            case Some(existingItem) =>
              // If the item already exists, update its quantity
              FoodItem.updateQuantity(existingItem.id, existingItem.quantity + quantity)
            case None =>
              // Otherwise, create a new food item
              FoodItem.create(
                foodBankId = foodBank.id.get,
                name = nameField.getText,
                category = categoryComboBox.getValue,
                quantity = quantity,
                expiryDate = Option(expiryDatePicker.getValue)
              )
          }
        }
      }
      null 
    }

    dialog.showAndWait()
    // Refresh the table to show the new or updated item
    populateInventoryTable()
  }

  // Handles the click event for the back button
  @FXML
  private def handleBackClick(): Unit = {
    MainApp.navigateBack()
  }
}