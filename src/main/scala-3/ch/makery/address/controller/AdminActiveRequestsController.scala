package ch.makery.address.controller

import ch.makery.address.MainApp
import ch.makery.address.model.{Donation, DonationRequest, FoodBank, FoodItem}
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.geometry.Pos
import javafx.scene.control._
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.{HBox, Priority}
import javafx.util.StringConverter

class AdminActiveRequestsController {

  // FXML UI element injections
  @FXML private var titleLabel: Label = _
  @FXML private var foodBankComboBox: ComboBox[FoodBank] = _
  @FXML private var donationsTableView: TableView[Donation] = _
  @FXML private var donorNameColumn: TableColumn[Donation, String] = _
  @FXML private var donatedItemColumn: TableColumn[Donation, String] = _
  @FXML private var donatedQuantityColumn: TableColumn[Donation, Int] = _
  @FXML private var donatedDeliveryColumn: TableColumn[Donation, String] = _
  @FXML private var donationActionsColumn: TableColumn[Donation, Void] = _
  @FXML private var requestsTableView: TableView[DonationRequest] = _
  @FXML private var requesterNameColumn: TableColumn[DonationRequest, String] = _
  @FXML private var requestedItemsColumn: TableColumn[DonationRequest, String] = _
  @FXML private var requestedDeliveryColumn: TableColumn[DonationRequest, String] = _
  @FXML private var requestActionsColumn: TableColumn[DonationRequest, Void] = _

  // Holds the currently selected food bank
  private var currentFoodBank: Option[FoodBank] = None

  // Initializes the controller with data from another view
  def initData(foodBank: FoodBank): Unit = {
    this.currentFoodBank = Some(foodBank)
  }

  @FXML
  def initialize(): Unit = {
    // Set table columns to resize automatically
    donationsTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS)
    requestsTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS)

    // Populate the food bank selection ComboBox
    val foodBanks = FoodBank.findAll()
    foodBankComboBox.getItems.addAll(foodBanks: _*)
    // Define how FoodBank objects are displayed as strings in the ComboBox
    foodBankComboBox.setConverter(new StringConverter[FoodBank] {
      override def toString(fb: FoodBank): String = if (fb != null) fb.name else ""
      override def fromString(string: String): FoodBank = null
    })

    // Pre-select a food bank if one was passed in
    currentFoodBank.foreach { fb =>
      foodBankComboBox.setValue(fb)
    }
    // Initial population of the tables
    populateTables()

    // Add a listener to repopulate tables when a new food bank is selected
    foodBankComboBox.valueProperty().addListener((_, _, newValue) => {
      if (newValue != null) {
        currentFoodBank = Some(newValue)
        titleLabel.setText(s"Active Requests for ${newValue.name}")
        populateTables()
      } else {
        currentFoodBank = None
        titleLabel.setText("Active Requests")
        donationsTableView.getItems.clear()
        requestsTableView.getItems.clear()
      }
    })
  }

  // Populates both the donations and requests tables based on the current food bank
  private def populateTables(): Unit = {
    currentFoodBank match {
      case Some(fb) =>
        populateDonationsTable(fb.id.get)
        populateRequestsTable(fb.id.get)
      case None =>
        // Clear tables if no food bank is selected
        donationsTableView.getItems.clear()
        requestsTableView.getItems.clear()
    }
  }

  // Sets up and populates the pledged donations table
  private def populateDonationsTable(foodBankId: Long): Unit = {
    donationsTableView.setEditable(false)

    // Map model properties to table columns
    donorNameColumn.setCellValueFactory(new PropertyValueFactory[Donation, String]("userUsername"))
    donatedItemColumn.setCellValueFactory(new PropertyValueFactory[Donation, String]("itemName"))
    donatedQuantityColumn.setCellValueFactory(new PropertyValueFactory[Donation, Int]("quantity"))
    donatedDeliveryColumn.setCellValueFactory(new PropertyValueFactory[Donation, String]("deliveryMethod"))

    // Create a custom cell factory for the actions column with approve/reject buttons
    donationActionsColumn.setCellFactory { _ =>
      new TableCell[Donation, Void] {
        private val approveButton = new Button("Approve")
        private val rejectButton = new Button("Reject")
        HBox.setHgrow(approveButton, Priority.ALWAYS)
        HBox.setHgrow(rejectButton, Priority.ALWAYS)
        private val hbox = new HBox(10, approveButton, rejectButton)
        hbox.setAlignment(Pos.CENTER)
        approveButton.getStyleClass.add("approve-button")
        rejectButton.getStyleClass.add("reject-button")

        override def updateItem(item: Void, empty: Boolean): Unit = {
          super.updateItem(item, empty)
          if (empty) {
            setGraphic(null)
          } else {
            val donation = getTableView.getItems.get(getIndex)
            approveButton.setOnAction(_ => handleApproveDonation(donation))
            rejectButton.setOnAction(_ => handleRejectDonation(donation))
            setGraphic(hbox)
          }
        }
      }
    }
    // Fetch and display pledged donations
    donationsTableView.setItems(FXCollections.observableArrayList(Donation.findAllByFoodBankAndStatus(foodBankId, "Pledged"): _*))
  }

  // Sets up and populates the pending donation requests table
  private def populateRequestsTable(foodBankId: Long): Unit = {
    requestsTableView.setEditable(false)

    // Map model properties to table columns
    requesterNameColumn.setCellValueFactory(new PropertyValueFactory[DonationRequest, String]("fullName"))
    requestedDeliveryColumn.setCellValueFactory(new PropertyValueFactory[DonationRequest, String]("deliveryMethod"))
    // Custom cell factory to format the list of requested items
    requestedItemsColumn.setCellValueFactory { cellData =>
      val items = cellData.getValue.requestedItems
      val formattedString = items.map(item => s"${item.name} (Qty: ${item.quantity})").mkString(", ")
      new SimpleStringProperty(formattedString)
    }

    // Create a custom cell factory for the actions column with approve/reject buttons
    requestActionsColumn.setCellFactory { _ =>
      new TableCell[DonationRequest, Void] {
        private val approveButton = new Button("Approve")
        private val rejectButton = new Button("Reject")
        HBox.setHgrow(approveButton, Priority.ALWAYS)
        HBox.setHgrow(rejectButton, Priority.ALWAYS)
        private val hbox = new HBox(10, approveButton, rejectButton)
        hbox.setAlignment(Pos.CENTER)
        approveButton.getStyleClass.add("approve-button")
        rejectButton.getStyleClass.add("reject-button")

        override def updateItem(item: Void, empty: Boolean): Unit = {
          super.updateItem(item, empty)
          if (empty) {
            setGraphic(null)
          } else {
            val request = getTableView.getItems.get(getIndex)
            approveButton.setOnAction(_ => handleApproveRequest(request))
            rejectButton.setOnAction(_ => handleRejectRequest(request))
            setGraphic(hbox)
          }
        }
      }
    }
    // Fetch and display pending donation requests
    requestsTableView.setItems(FXCollections.observableArrayList(DonationRequest.findAllByFoodBankAndStatus(foodBankId, "Pending"): _*))
  }

  // Handles the approval of a pledged donation
  private def handleApproveDonation(donation: Donation): Unit = {
    // Check if the donated item already exists in the inventory
    FoodItem.findByNameAndFoodBank(donation.itemName, donation.foodBankId) match {
      case Some(existingItem) =>
        // If it exists, update the quantity
        val newQuantity = existingItem.quantity + donation.quantity
        FoodItem.updateQuantity(existingItem.id, newQuantity)
      case None =>
        // If it's a new item, create a new record in the inventory
        FoodItem.create(
          foodBankId = donation.foodBankId,
          name = donation.itemName,
          category = donation.category,
          quantity = donation.quantity,
          expiryDate = donation.expiryDate
        )
    }

    // Update the donation status to 'Approved'
    Donation.updateStatus(donation.id, "Approved")

    // Refresh the tables to reflect the change
    populateTables()
  }

  // Handles the rejection of a pledged donation
  private def handleRejectDonation(donation: Donation): Unit = {
    Donation.updateStatus(donation.id, "Rejected")
    populateTables()
  }

  // Handles the approval of a donation request
  private def handleApproveRequest(request: DonationRequest): Unit = {
    var allItemsAvailable = true
    // Iterate through requested items to check for availability and deduct from inventory
    request.requestedItems.foreach { requestedItem =>
      FoodItem.findByNameAndFoodBank(requestedItem.name, request.foodBankId) match {
        case Some(inventoryItem) =>
          // Check if there is enough quantity in stock
          if (inventoryItem.quantity >= requestedItem.quantity) {
            val newQuantity = inventoryItem.quantity - requestedItem.quantity
            FoodItem.updateQuantity(inventoryItem.id, newQuantity)
          } else {
            // Not enough stock, flag the request
            allItemsAvailable = false
          }
        case None =>
          // Item not found in inventory, flag the request
          allItemsAvailable = false
      }
    }

    // Update status based on item availability
    if (allItemsAvailable) {
      DonationRequest.updateStatus(request.id, "Approved")
    } else {
      DonationRequest.updateStatus(request.id, "Rejected")
    }
    // Refresh the tables
    populateTables()
  }

  // Handles the rejection of a donation request
  private def handleRejectRequest(request: DonationRequest): Unit = {
    DonationRequest.updateStatus(request.id, "Rejected")
    populateTables()
  }

  // Handles the click event for the back button
  @FXML
  private def handleBackClick(): Unit = {
    MainApp.navigateBack()
  }
}