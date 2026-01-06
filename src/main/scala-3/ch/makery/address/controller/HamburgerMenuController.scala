package ch.makery.address.controller

import ch.makery.address.MainApp
import ch.makery.address.model.User
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import scalafx.Includes._
import scalafx.event.ActionEvent

class HamburgerMenuController {

  @FXML private var hamburgerButton: Button = _
  @FXML private var menuPane: VBox = _
  @FXML private var logoutButton: Button = _
  @FXML private var loginButton: Button = _

  @FXML private var homeButton: Button = _
  @FXML private var donateButton: Button = _
  @FXML private var requestButton: Button = _
  @FXML private var volunteerButton: Button = _
  @FXML private var approvalButton: Button = _

  @FXML private var dashboardButton: Button = _
  @FXML private var newAdminButton: Button = _
  @FXML private var donationRequestButton: Button = _
  @FXML private var volunteerRequestButton: Button = _

  private var adminButtons: List[Button] = _
  private var userButtons: List[Button] = _

  private var currentUser: Option[User] = None

  @FXML
  def initialize(): Unit = {
    adminButtons = List(dashboardButton, newAdminButton, donationRequestButton, volunteerRequestButton)
    userButtons = List(homeButton, requestButton, donateButton, volunteerButton, approvalButton)

    updateView()

    loginButton.onAction = (_: ActionEvent) => handleNavigation(MainApp.showLoginView)
    logoutButton.onAction = (_: ActionEvent) => handleLogout()

    dashboardButton.onAction = (_: ActionEvent) => handleDashboardNavigation()
    newAdminButton.onAction = (_: ActionEvent) => handleNavigation(MainApp.showNewAdminView)
    donationRequestButton.onAction = (_: ActionEvent) => handleNavigation(MainApp.showGeneralActiveRequestsView)
    volunteerRequestButton.onAction = (_: ActionEvent) => handleNavigation(MainApp.showGeneralVolunteerManagementView)

    homeButton.onAction = (_: ActionEvent) => handleDashboardNavigation()
    donateButton.onAction = (_: ActionEvent) => handleNavigation(MainApp.showGeneralDonationForm)
    volunteerButton.onAction = (_: ActionEvent) => handleNavigation(MainApp.showGeneralVolunteerView)
    requestButton.onAction = (_: ActionEvent) => handleNavigation(MainApp.showGeneralRequestForm)
    approvalButton.onAction = (_: ActionEvent) => handleNavigation(MainApp.showUserStatusView)
  }

  def setUser(user: User): Unit = {
    currentUser = Some(user)
    updateView()
  }

  private def updateView(): Unit = {
    currentUser match {
      case Some(user) =>
        val isAdmin = user.role.equalsIgnoreCase("admin") || user.role.equalsIgnoreCase("superadmin")

        setButtonState(loginButton, false)
        setButtonState(logoutButton, true)


        adminButtons.foreach(btn => setButtonState(btn, isAdmin))

        userButtons.foreach(btn => setButtonState(btn, !isAdmin))

      case None =>
        setButtonState(loginButton, true)
        setButtonState(logoutButton, false)
        (adminButtons ++ userButtons).foreach(btn => setButtonState(btn, false))
    }
  }

  @FXML
  private def toggleMenu(): Unit = {
    if (menuPane != null) {
      val isVisible = menuPane.isVisible
      menuPane.setVisible(!isVisible)
      menuPane.setManaged(!isVisible)
    }
  }

  private def handleLogout(): Unit = {
    toggleMenu()
    currentUser = None
    updateView()
    MainApp.showLoginView()
  }

  private def handleNavigation(navigateAction: () => Unit): Unit = {
    toggleMenu()
    navigateAction()
  }

  private def handleDashboardNavigation(): Unit = {
    toggleMenu()
    currentUser.foreach { user =>
      if (user.role.equalsIgnoreCase("admin") || user.role.equalsIgnoreCase("superadmin")) {
        MainApp.showAdminMainView(user)
      } else {
        MainApp.showMainView(user)
      }
    }
  }

  private def setButtonState(button: Button, isVisible: Boolean): Unit = {
    if (button != null) {
      button.setVisible(isVisible)
      button.setManaged(isVisible)
    }
  }
}