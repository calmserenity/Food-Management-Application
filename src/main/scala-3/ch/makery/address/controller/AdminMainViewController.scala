package ch.makery.address.controller

import ch.makery.address.MainApp
import javafx.fxml.FXML

class AdminMainViewController {

  // Handles the click event for the food bank list card
  @FXML
  private def handleFoodBankListClick(): Unit = {
    println("Food Bank List card clicked!")
    MainApp.showFoodBankListView()
  }

  // Handles the click event for the donation requests card
  @FXML
  private def handleDonationRequestClick(): Unit = {
    MainApp.showGeneralActiveRequestsView()
  }
  
  // Handles the click event for the volunteer requests card
  @FXML
  private def handleVolunteerRequestClick(): Unit = {
    MainApp.showGeneralVolunteerManagementView()
  }
}
