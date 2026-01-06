package ch.makery.address.controller

import ch.makery.address.MainApp
import ch.makery.address.model.User
import javafx.fxml.FXML
import javafx.scene.control.Hyperlink
import scalafx.stage.Stage
import scalafx.Includes._

class UserMainViewController {

  // FXML UI element injection
  @FXML
  private var foodBankLink: Hyperlink = _

  // Holds the main application stage
  private var mainStage: Stage = _
  // Holds the currently logged-in user
  private var currentUser: Option[User] = _

  // Initializes the controller after FXML loading
  @FXML
  def initialize(): Unit = {
    foodBankLink.onAction = _ => {
      MainApp.showUserFoodBankListView()
    }
  }

  // Initializes the user session
  def initSession(stage: Stage, user: User): Unit = {
    mainStage = stage
    currentUser = Some(user)
    println(s"MainView (Hero Section) session initialized for user: ${user.username}")
  }
}