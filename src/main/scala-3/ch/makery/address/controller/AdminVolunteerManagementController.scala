package ch.makery.address.controller

import ch.makery.address.MainApp
import ch.makery.address.model.{FoodBank, VolunteerSession}
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.geometry.Pos
import javafx.scene.control._
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.{HBox, Priority}
import javafx.util.StringConverter
import java.time.format.DateTimeFormatter

class AdminVolunteerManagementController {

  // FXML UI element injections
  @FXML private var titleLabel: Label = _
  @FXML private var foodBankComboBox: ComboBox[FoodBank] = _
  @FXML private var pendingTableView: TableView[VolunteerSession] = _
  @FXML private var pendingNameColumn: TableColumn[VolunteerSession, String] = _
  @FXML private var pendingDateColumn: TableColumn[VolunteerSession, String] = _
  @FXML private var pendingTimeColumn: TableColumn[VolunteerSession, String] = _
  @FXML private var pendingActionsColumn: TableColumn[VolunteerSession, Void] = _
  @FXML private var confirmedTableView: TableView[VolunteerSession] = _
  @FXML private var confirmedNameColumn: TableColumn[VolunteerSession, String] = _
  @FXML private var confirmedDateColumn: TableColumn[VolunteerSession, String] = _
  @FXML private var confirmedTimeColumn: TableColumn[VolunteerSession, String] = _
  @FXML private var confirmedActivityColumn: TableColumn[VolunteerSession, String] = _

  // Holds the currently selected food bank
  private var currentFoodBank: Option[FoodBank] = None
  private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

  // Initializes the controller with data from another view
  def initData(foodBank: FoodBank): Unit = {
    this.currentFoodBank = Some(foodBank)
  }

  @FXML
  def initialize(): Unit = {
    pendingTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS)
    confirmedTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS)

    // Populate and configure the food bank selection ComboBox
    val foodBanks = FoodBank.findAll()
    foodBankComboBox.getItems.addAll(foodBanks: _*)
    foodBankComboBox.setConverter(new StringConverter[FoodBank] {
      override def toString(fb: FoodBank): String = if (fb != null) fb.name else ""
      override def fromString(string: String): FoodBank = null
    })

    // Pre-select a food bank if one was passed in and populate the tables
    currentFoodBank.foreach { fb =>
      foodBankComboBox.setValue(fb)
    }
    populateTables()

    // Add a listener to repopulate tables when a new food bank is selected
    foodBankComboBox.valueProperty().addListener((_, _, newValue) => {
      if (newValue != null) {
        currentFoodBank = Some(newValue)
        titleLabel.setText(s"Volunteer Schedule for ${newValue.name}")
        populateTables()
      } else {
        currentFoodBank = None
        titleLabel.setText("Volunteer Schedule")
        pendingTableView.getItems.clear()
        confirmedTableView.getItems.clear()
      }
    })
  }

  // Populates both the pending and confirmed volunteer tables
  private def populateTables(): Unit = {
    currentFoodBank match {
      case Some(fb) =>
        populatePendingTable(fb.id.get)
        populateConfirmedTable(fb.id.get)
      case None =>
        pendingTableView.getItems.clear()
        confirmedTableView.getItems.clear()
    }
  }

  // Sets up and populates the pending volunteer sessions table
  private def populatePendingTable(foodBankId: Long): Unit = {
    pendingTableView.setEditable(false)

    // Map model properties to table columns
    pendingNameColumn.setCellValueFactory(new PropertyValueFactory[VolunteerSession, String]("fullName"))
    pendingDateColumn.setCellValueFactory(new PropertyValueFactory[VolunteerSession, String]("sessionDate"))
    // Custom cell factory to format the start and end times
    pendingTimeColumn.setCellValueFactory { cellData =>
      val session = cellData.getValue
      val timeStr = s"${session.startTime.format(timeFormatter)} - ${session.endTime.format(timeFormatter)}"
      new SimpleStringProperty(timeStr)
    }

    // Create a custom cell factory for the actions column with confirm/reject buttons
    pendingActionsColumn.setCellFactory { _ =>
      new TableCell[VolunteerSession, Void] {
        private val confirmButton = new Button("Confirm")
        private val rejectButton = new Button("Reject")
        HBox.setHgrow(confirmButton, Priority.ALWAYS)
        HBox.setHgrow(rejectButton, Priority.ALWAYS)
        private val hbox = new HBox(10, confirmButton, rejectButton)
        hbox.setAlignment(Pos.CENTER)
        confirmButton.getStyleClass.add("approve-button")
        rejectButton.getStyleClass.add("reject-button")

        override def updateItem(item: Void, empty: Boolean): Unit = {
          super.updateItem(item, empty)
          if (empty) {
            setGraphic(null)
          } else {
            val session = getTableView.getItems.get(getIndex)
            confirmButton.setOnAction(_ => handleConfirm(session))
            rejectButton.setOnAction(_ => handleReject(session))
            setGraphic(hbox)
          }
        }
      }
    }
    // Fetch and display pending volunteer sessions
    pendingTableView.setItems(FXCollections.observableArrayList(VolunteerSession.findAllByFoodBankAndStatus(foodBankId, "Pending"): _*))
  }

  // Sets up and populates the confirmed volunteer sessions table
  private def populateConfirmedTable(foodBankId: Long): Unit = {
    confirmedTableView.setEditable(false)

    // Map model properties to table columns
    confirmedNameColumn.setCellValueFactory(new PropertyValueFactory[VolunteerSession, String]("fullName"))
    confirmedDateColumn.setCellValueFactory(new PropertyValueFactory[VolunteerSession, String]("sessionDate"))
    // Custom cell factory to format the start and end times
    confirmedTimeColumn.setCellValueFactory { cellData =>
      val session = cellData.getValue
      val timeStr = s"${session.startTime.format(timeFormatter)} - ${session.endTime.format(timeFormatter)}"
      new SimpleStringProperty(timeStr)
    }
    confirmedActivityColumn.setCellValueFactory(cellData => new SimpleStringProperty(cellData.getValue.activity.getOrElse("-")))

    // Fetch and display confirmed volunteer sessions
    confirmedTableView.setItems(FXCollections.observableArrayList(VolunteerSession.findAllByFoodBankAndStatus(foodBankId, "Confirmed"): _*))
  }

  // Handles the confirmation of a volunteer session
  private def handleConfirm(session: VolunteerSession): Unit = {
    VolunteerSession.updateStatus(session.id, "Confirmed")
    populateTables()
  }

  // Handles the rejection of a volunteer session
  private def handleReject(session: VolunteerSession): Unit = {
    VolunteerSession.updateStatus(session.id, "Rejected")
    populateTables()
  }

  // Handles the click event for the back button
  @FXML
  private def handleBackClick(): Unit = {
    MainApp.navigateBack()
  }
}