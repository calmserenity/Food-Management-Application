package ch.makery.address.controller

import ch.makery.address.MainApp
import ch.makery.address.model.{Donation, DonationRequest, VolunteerSession}
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control._
import javafx.scene.control.cell.PropertyValueFactory
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}

class UserStatusController {
  // FXML UI element injections
  @FXML private var donationsTableView: TableView[Donation] = _
  @FXML private var donatedItemColumn: TableColumn[Donation, String] = _
  @FXML private var donationDateColumn: TableColumn[Donation, LocalDateTime] = _
  @FXML private var donationStatusColumn: TableColumn[Donation, String] = _
  @FXML private var requestsTableView: TableView[DonationRequest] = _
  @FXML private var requestItemsColumn: TableColumn[DonationRequest, String] = _
  @FXML private var requestDateColumn: TableColumn[DonationRequest, LocalDateTime] = _
  @FXML private var requestStatusColumn: TableColumn[DonationRequest, String] = _
  @FXML private var volunteeringTableView: TableView[VolunteerSession] = _
  @FXML private var volunteerDateColumn: TableColumn[VolunteerSession, LocalDate] = _
  @FXML private var volunteerTimeColumn: TableColumn[VolunteerSession, String] = _
  @FXML private var volunteerStatusColumn: TableColumn[VolunteerSession, String] = _

  private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

  // Initializes the controller after FXML loading
  @FXML
  def initialize(): Unit = {
    MainApp.currentUser.foreach { user =>
      setupDonationsTable(user.username)
      setupRequestsTable(user.username)
      setupVolunteeringTable(user.username)
    }
  }

  // Sets up and populates the donations table
  private def setupDonationsTable(username: String): Unit = {
    donationsTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS)
    donatedItemColumn.setCellValueFactory(new PropertyValueFactory[Donation, String]("itemName"))
    donationDateColumn.setCellValueFactory(new PropertyValueFactory[Donation, LocalDateTime]("donationDate"))
    donationStatusColumn.setCellValueFactory(new PropertyValueFactory[Donation, String]("status"))
    donationStatusColumn.setCellFactory(_ => createStatusCell[Donation]())
    donationsTableView.setItems(FXCollections.observableArrayList(Donation.findAllByUser(username): _*))
  }

  // Sets up and populates the donation requests table
  private def setupRequestsTable(username: String): Unit = {
    requestsTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS)
    requestItemsColumn.setCellValueFactory(cellData => {
      val items = cellData.getValue.requestedItems.map(i => s"${i.name} (x${i.quantity})").mkString(", ")
      new SimpleStringProperty(items)
    })
    requestDateColumn.setCellValueFactory(new PropertyValueFactory[DonationRequest, LocalDateTime]("requestDate"))
    requestStatusColumn.setCellValueFactory(new PropertyValueFactory[DonationRequest, String]("status"))
    requestStatusColumn.setCellFactory(_ => createStatusCell[DonationRequest]())
    requestsTableView.setItems(FXCollections.observableArrayList(DonationRequest.findAllByUser(username): _*))
  }

  // Sets up and populates the volunteering table
  private def setupVolunteeringTable(username: String): Unit = {
    volunteeringTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS)
    volunteerDateColumn.setCellValueFactory(new PropertyValueFactory[VolunteerSession, LocalDate]("sessionDate"))
    volunteerTimeColumn.setCellValueFactory(cellData => {
      val session = cellData.getValue
      val timeStr = s"${session.startTime.format(timeFormatter)} - ${session.endTime.format(timeFormatter)}"
      new SimpleStringProperty(timeStr)
    })
    volunteerStatusColumn.setCellValueFactory(new PropertyValueFactory[VolunteerSession, String]("status"))
    volunteerStatusColumn.setCellFactory(_ => createStatusCell[VolunteerSession]())
    volunteeringTableView.setItems(FXCollections.observableArrayList(VolunteerSession.findAllByUser(username): _*))
  }

  // Generic helper method to create a color-coded status cell for tables
  private def createStatusCell[T](): TableCell[T, String] = {
    new TableCell[T, String] {
      override def updateItem(status: String, empty: Boolean): Unit = {
        super.updateItem(status, empty)
        if (empty || status == null) {
          setText(null)
          setGraphic(null)
        } else {
          val statusLabel = new Label(status)
          statusLabel.getStyleClass.add("status-label")
          statusLabel.getStyleClass.removeAll("status-approved", "status-pending", "status-rejected")

          status.toLowerCase match {
            case "approved" | "confirmed" => statusLabel.getStyleClass.add("status-approved")
            case "pending" | "pledged" => statusLabel.getStyleClass.add("status-pending")
            case "rejected" | "cancelled" => statusLabel.getStyleClass.add("status-rejected")
            case _ =>
          }
          setGraphic(statusLabel)
        }
      }
    }
  }

  // Handles the click event for the back button
  @FXML
  private def handleBackClick(): Unit = {
    MainApp.navigateBack()
  }
}